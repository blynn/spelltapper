import logging
import random
from datetime import datetime

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from google.appengine.ext.db import Key

class Move(db.Model):
  move = db.StringProperty()
  para = db.StringProperty()
  charm_hand = db.StringProperty()
  charm_gesture = db.StringProperty()
  has_move = db.IntegerProperty()
  has_para = db.IntegerProperty()
  has_charm = db.IntegerProperty()

class Game(db.Model):
  name = db.StringProperty()
  ctime = db.DateTimeProperty(auto_now_add=True)
  mtime = db.DateTimeProperty(auto_now_add=True)
  received_count = db.IntegerProperty()
  ready_count = db.IntegerProperty()
  finished = db.StringProperty()

class NewGame(db.Model):
  ctime = db.DateTimeProperty(auto_now_add=True)
  nonce = db.StringProperty()
  level = db.StringProperty()

# For anonymous duels.
class Anon(db.Model):
  nonce = db.StringProperty()
  ctime = db.DateTimeProperty(auto_now_add=True)

class MainPage(webapp.RequestHandler):
  def get(self):
    if "" == self.request.query_string:
      self.response.out.write("1")
      return
    cmd = self.request.get("c")
    if "n" == cmd:  # New game.
      level = self.request.get("b")
      if "" == level:
	self.response.out.write("Error: No level supplied.")
	return
      name = self.request.get("a")

      if "" == name:
        def handle_anon():
	  anon = db.get(Key.from_path("Anon", "meh"))
	  if not anon:
	    anon = Anon(key_name="meh", nonce="%X" % random.getrandbits(64))
	    anon.put()
	    name = anon.nonce
	  else:
	    name = anon.nonce
	    anon.delete()
	db.run_in_transaction(handle_anon)

      # Use a 64-bit random ID to make it harder to interfere with existing
      # games.

      def add_player():
	ng = db.get(Key.from_path("NewGame", "n:" + name))
	if not ng:
	  ng = NewGame(key_name="n:" + name, level=level,
	      nonce="%X" % random.getrandbits(64), player_count=0)
	  ng.put()
	  return 0, ng.level, ng.nonce
	else:
	  ng.delete()
	  return 1, ng.level, ng.nonce

      player_i, s1, s2 = db.run_in_transaction(add_player)
      self.response.out.write(unicode(player_i) + s1 + s2)
      logging.info("Response: " + unicode(player_i) + ":" + s1 + "  " + s2)
      if 1 == player_i:
	gameid = "g:" + s2
	# TODO: Although unlikely, check game with this nonce does not exist.
	game = Game(key_name = gameid,
		    name = name,
		    received_count = 0,
		    ready_count = 0)
	game.put()

	for i in range(2):
	  moveid = "m:" + s2 + unicode(i)
	  move = Move(key_name = moveid,
		      has_charm = 0,
		      has_para = 0,
		      has_move = 0)
	  move.put()
      return;

    # Not a request for a new game.
    gamename = self.request.get("g")
    game = Game.get_by_key_name("g:" + gamename)

    if "s" == cmd:  # Await for start of game.
      if not game:
	self.response.out.write("-")
      else:
	self.response.out.write("OK")
      return

    if not game:
      logging.error("No such game: " + gamename)
      self.response.out.write("Error: No such game.")
      return

    gamekey = game.key()
    game.mtime = datetime.now()
    # Ignore race for once. The winner of this race will be
    # close enough to the right value of the current time for my purposes.
    game.put()
    playerid = self.request.get("i")
    if ('0' != playerid) and ('1' != playerid):
      self.response.out.write("Error: Bad player ID.")
      return
    def CommandFinish():
      self.response.out.write("OK")
      if "" == game.finished:
	def set_finished():
	  game = db.get(gamekey)
	  game.finished = playerid
	  game.put()
	db.run_in_transaction(set_finished)
      elif playerid != game.finished:
	# Delete this game.
	for i in range(2):
	  moveid = "m:" + gamename + unicode(i)
	  Move.get_by_key_name(moveid).delete()
	game.delete()
    def CommandMove():
      a = self.request.get("a")
      if "" == a:
	self.response.out.write("Error: Bad move.")
	return
      logging.info("Got move " + gamename + ":" + playerid + " " + a)
      moveid = "m:" + gamename + playerid
      move = Move.get_by_key_name(moveid)
      if not move:
	logging.error("Missing move!")
	self.response.out.write("Error: Missing move.")
	return

      if 1 == move.has_move:
	logging.error("Move sent twice.")
	self.response.out.write("Error: Move sent twice.")
	return

      move.has_move = 1
      move.move = a
      move.put()
      def increment_received_count():
	game = db.get(gamekey)
	if 2 == game.received_count:
	  logging.error("received_count > 2!")
	game.received_count = game.received_count + 1
	game.put()
      db.run_in_transaction(increment_received_count)
      self.response.out.write("OK")
    def CommandGetMove():
      if 2 == game.received_count:
	moveid = "m:" + gamename + unicode(1 - int(playerid))
	move = Move.get_by_key_name(moveid)
	if not move:
	  self.response.out.write('Error: Cannot find move!')
	else:
	  self.response.out.write(move.move)
	  if 1 == move.has_move:
	    move.has_move = 0
	    move.has_charm = 0
	    move.has_para = 0
	    move.put()
	    def increment_ready_count():
	      game = db.get(gamekey)
	      game.ready_count = game.ready_count + 1
	      # All players ready for next turn?
	      if (2 == game.ready_count):
		game.ready_count = 0
		game.received_count = 0
	      game.put()
	    db.run_in_transaction(increment_ready_count)
      else:
	self.response.out.write('-')
      return
    def CommandSetPara():
      target = self.request.get("a")
      if "" == target:
	self.response.out.write("Error: Bad paralysis target.")
	return
      if "0" == target:
	targetid = playerid
      else:
	targetid = unicode(1 - int(playerid))

      gesture = self.request.get("b")
      if "" == gesture:
	self.response.out.write("Error: Bad paralysis gesture.")
	return
      moveid = "m:" + gamename + targetid
      logging.info("SetPara " + moveid)
      move = Move.get_by_key_name(moveid)
      if not move:
	self.response.out.write('Error: Cannot find move!')
	return

      if (1 == move.has_para):
	self.response.out.write("Error: Already received paralysis.")
	return

      move.para = gesture
      move.has_para = 1;
      move.put()
      self.response.out.write("OK")
      return
    def CommandGetPara():
      target = self.request.get("a")
      if "" == target:
	self.response.out.write("Error: Bad paralysis target.")
	return

      if "0" == target:
	targetid = playerid
      else:
	targetid = unicode(1 - int(playerid))

      moveid = "m:" + gamename + targetid
      move = Move.get_by_key_name(moveid)
      if not move:
	self.response.out.write('Error: Cannot find move!')
	return
      if 0 == move.has_para:
	self.response.out.write("-")
      else:
	self.response.out.write(move.para)
      return
    def CommandSetCharm():
      # This is unnecessary as we always assume target is opponent.
      target = self.request.get("a")
      if "" == target:
	self.response.out.write("Error: Bad charm target.")
	return
      s = self.request.get("b")
      if "" == s:
	self.response.out.write("Error: Bad charm choices.")
	return
      moveid = "m:" + gamename + unicode(1 - int(playerid))
      logging.info("Charm " + moveid)
      move = Move.get_by_key_name(moveid)
      if not move:
	self.response.out.write('Error: Cannot find move!')
	return
      if (1 == move.has_charm):
	self.response.out.write("Error: Already received charm.")
	return;

      move.charm_hand = s[0]
      move.charm_gesture = s[1]
      move.has_charm = 1
      move.put()
      self.response.out.write("OK")
      return
    def CommandGetCharmHand():
      moveid = "m:" + gamename + playerid
      move = Move.get_by_key_name(moveid)
      if not move:
	self.response.out.write('Error: Cannot find move!')
	return

      if 0 == move.has_charm:
	self.response.out.write("-")
      else:
	self.response.out.write(move.charm_hand)
      return
    def CommandGetCharmGesture():
      moveid = "m:" + gamename + playerid
      move = Move.get_by_key_name(moveid)
      if not move:
	self.response.out.write('Error: Cannot find move!')
	return
      if 0 == move.has_charm:
	self.response.out.write("-")
      else:
	self.response.out.write(move.charm_gesture)
      return
    def CommandBad():
      self.response.out.write("Error: Bad command.")
      return
    {'m' : CommandMove,
     'g' : CommandGetMove,
     'p' : CommandSetPara,
     'q' : CommandGetPara,
     'f' : CommandFinish,
     'C' : CommandSetCharm,
     'H' : CommandGetCharmHand,
     'G' : CommandGetCharmGesture,
    }.get(cmd, CommandBad)()

application = webapp.WSGIApplication(
                                     [('/', MainPage)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()

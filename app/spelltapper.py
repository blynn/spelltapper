import logging
import random
from datetime import datetime

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db
from google.appengine.ext.db import Key

class Psych(db.Model):
  id = db.StringProperty()
  para = db.StringProperty()
  charm_hand = db.StringProperty()
  charm_gesture = db.StringProperty()
  has_para = db.IntegerProperty()
  has_charm = db.IntegerProperty()
  ctime = db.DateTimeProperty(auto_now_add=True)

class Move(db.Model):
  id = db.StringProperty()
  move = db.StringProperty()
  ctime = db.DateTimeProperty(auto_now_add=True)
  is_ready = db.IntegerProperty()

class Game(db.Model):
  name = db.StringProperty()
  id = db.StringProperty()
  ctime = db.DateTimeProperty(auto_now_add=True)
  mtime = db.DateTimeProperty(auto_now_add=True)
  level = db.StringProperty()
  received_count = db.IntegerProperty()
  ready_count = db.IntegerProperty()
  finished = db.StringProperty()
  player_count = db.IntegerProperty()

class NewGame(db.Model):
  nonce = db.StringProperty()
  player_count = db.IntegerProperty()

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
      # Use a 64-bit random ID to make it harder to interfere with existing
      # games.
      NewGame.get_or_insert(
	  "n:" + name, nonce=hex(random.getrandbits(64)), player_count=0)


      gameid = "g:" + name
      gamekey = Key.from_path("Game", gameid)
      def get_game():
	game = db.get(gamekey)
	if not game:
	  game = Game(key_name = gameid,
		      name = name,
		      player_count = 0,
		      received_count = 0,
		      ready_count = 0,
		      level = level)
	  game.put()
	return game
      game = db.run_in_transaction(get_game)
      if 2 == game.player_count:
	self.response.out.write('Error: Game "' +
	    name + '" already in progress.')
	return

      self.response.out.write(unicode(game.player_count) + game.level + name)
      def add_game_player(gamekey):
	game = db.get(gamekey)
	game.player_count = game.player_count + 1
	game.put()
      db.run_in_transaction(add_game_player, game.key())
      return;

    gameid = self.request.get("g")
    game = Game.get_by_key_name("g:" + gameid)
    if not game:
      logging.error("No such game: " + gameid)
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
    def NextTurn():
      for i in range(2):
	moveid = unicode(i) + gameid
	# TODO: Fix Psych races.
	psy = Psych.gql("WHERE id = :1", moveid).get()
	if None != psy:
	  psy.delete()
      def zero_counts():
	game = db.get(gamekey)
	game.ready_count = 0
	game.received_count = 0
	game.put()
      db.run_in_transaction(zero_counts)
    def CommandStart():
      if 2 == game.player_count:
	self.response.out.write("OK")
      else:
	self.response.out.write("-")
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
	move = Move.gql("WHERE id = :1", '0' + gameid).get()
	if (None != move): move.delete();
	move = Move.gql("WHERE id = :1", '1' + gameid).get()
	if (None != move): move.delete();
	psy = Psych.gql("WHERE id = :1", '0' + gameid).get()
	if (None != psy): psy.delete();
	psy = Psych.gql("WHERE id = :1", '1' + gameid).get()
	if (None != psy): psy.delete();
	game.delete()
    def CommandMove():
      a = self.request.get("a")
      if "" == a:
	self.response.out.write("Error: Bad move.")
	return
      logging.info("Got move " + gameid + ":" + playerid + " " + a)
      moveid = playerid + gameid
      move = Move.gql("WHERE id = :1", moveid).get()
      if None != move:
	if 0 == move.is_ready:
	  logging.error("Error: Move sent twice.")
	  self.response.out.write("Error: Move sent twice.")
	move.delete()

      move = Move()
      move.id = moveid
      move.is_ready = 0
      move.move = a
      move.put()
      self.response.out.write("OK")
      def increment_received_count():
	game = db.get(gamekey)
	if 2 == game.received_count:
	  logging.error("received_count > 2!")
	game.received_count = game.received_count + 1
	game.put()
      db.run_in_transaction(increment_received_count)
    def CommandGetMove():
      if 2 == game.received_count:
	moveid = unicode(1 - int(playerid)) + gameid
	move = Move.gql("WHERE id = :1", moveid).get()
	if None == move:
	  self.response.out.write('Error: Cannot find move!')
	else:
	  self.response.out.write(move.move)
	  if 0 == move.is_ready:
	    move.is_ready = 1
	    move.put()
	    def increment_ready_count():
	      game = db.get(gamekey)
	      game.ready_count = game.ready_count + 1
	      game.put()
	    db.run_in_transaction(increment_ready_count)
	    # All players ready for next turn?
	    if 2 == db.get(gamekey).ready_count:
	      logging.info("NextTurn()")
	      NextTurn()
      else:
	self.response.out.write('-')
      return
    def CommandSetPara():
      target = self.request.get("a")
      if "" == target:
	self.response.out.write("Error: Bad paralysis target.")
	return
      gesture = self.request.get("b")
      if "" == gesture:
	self.response.out.write("Error: Bad paralysis gesture.")
	return
      moveid = unicode(1 - int(target)) + gameid
      psy = Psych.gql("WHERE id = :1", moveid).get()
      if None != psy:
	if (1 == psy.has_para):
	  self.response.out.write("Error: Already received paralysis.")
	  return;
      else:
	psy = Psych()
	psy.has_charm = 0;
	psy.id = moveid
      psy.para = gesture
      psy.has_para = 1;
      psy.put()
      self.response.out.write("OK")
      return
    def CommandGetPara():
      target = self.request.get("a")
      if "" == target:
	self.response.out.write("Error: Bad paralysis target.")
	return
      moveid = target + gameid
      psy = Psych.gql("WHERE id = :1", moveid).get()
      if None == psy or 0 == psy.has_para:
	self.response.out.write("-")
      else:
	self.response.out.write(psy.para)
      return
    def CommandSetCharm():
      target = self.request.get("a")
      if "" == target:
	self.response.out.write("Error: Bad charm target.")
	return
      s = self.request.get("b")
      if "" == s:
	self.response.out.write("Error: Bad charm choices.")
	return
      moveid = unicode(1 - int(target)) + gameid
      psy = Psych.gql("WHERE id = :1", moveid).get()
      if None != psy:
	if (1 == psy.has_charm):
	  self.response.out.write("Error: Already received charm.")
	  return;
      else:
	psy = Psych()
	psy.has_para = 0
	psy.id = moveid
      psy.charm_hand = s[0]
      psy.charm_gesture = s[1]
      psy.has_charm = 1
      psy.put()
      self.response.out.write("OK")
      return
    def CommandGetCharmHand():
      moveid = playerid + gameid
      psy = Psych.gql("WHERE id = :1", moveid).get()
      if None == psy or 0 == psy.has_charm:
	self.response.out.write("-")
      else:
	self.response.out.write(psy.charm_hand)
      return
    def CommandGetCharmGesture():
      moveid = playerid + gameid
      psy = Psych.gql("WHERE id = :1", moveid).get()
      if None == psy or 0 == psy.has_charm:
	self.response.out.write("-")
      else:
	self.response.out.write(psy.charm_gesture)
      return
    def CommandBad():
      self.response.out.write("Error: Bad command.")
      return
    {'m' : CommandMove,
     'g' : CommandGetMove,
     'p' : CommandSetPara,
     'q' : CommandGetPara,
     's' : CommandStart,
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

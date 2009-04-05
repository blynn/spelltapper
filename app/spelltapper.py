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
  ctime = db.DateTimeProperty(auto_now_add=True)
  para = db.StringProperty()
  charm_hand = db.StringProperty()
  charm_gesture = db.StringProperty()
  has_para = db.IntegerProperty()
  has_charm = db.IntegerProperty()

class Game(db.Model):
  name = db.StringProperty()
  chicken = db.IntegerProperty()
  ctime = db.DateTimeProperty(auto_now_add=True)
  atime = db.DateTimeProperty(auto_now_add=True)
  now_turn = db.IntegerProperty()
  received_count = db.IntegerProperty()
  level = db.IntegerProperty()
  finished = db.StringProperty()

class Duel(db.Model):
  chicken = db.IntegerProperty()
  ctime = db.DateTimeProperty(auto_now_add=True)
  atime = db.DateTimeProperty(auto_now_add=True)
  now_turn = db.IntegerProperty()
  received_count = db.IntegerProperty()
  level = db.IntegerProperty()
  finished = db.StringProperty()

class NewGame(db.Model):
  ctime = db.DateTimeProperty(auto_now_add=True)
  nonce = db.StringProperty()
  level = db.IntegerProperty()

class Account(db.Model):
  ctime = db.DateTimeProperty(auto_now_add=True)
  nonce = db.StringProperty()
  level = db.IntegerProperty()

class User(db.Model):
  ctime = db.DateTimeProperty(auto_now_add=True)
  atime = db.DateTimeProperty(auto_now_add=True)
  name = db.StringProperty()
  level = db.IntegerProperty()
  state = db.IntegerProperty()
  arg = db.StringProperty()
  duel = db.StringProperty()

# For anonymous duels.
class Anon(db.Model):
  nonce = db.StringProperty()
  ctime = db.DateTimeProperty(auto_now_add=True)

class MainPage(webapp.RequestHandler):
  def get(self):
    if "" == self.request.query_string:
      self.response.out.write("2")
      return
    cmd = self.request.get("c")
    if "l" == cmd:  # Login.
      name = self.request.get("a")
      b = self.request.get("b")
      if "" == b:
	logging.error("Error: No level supplied.")
	return
      level = int(b)

      logging.info("login: " + name)
      # TODO: Handle empty names, spaces at the beginning or end of names, etc.
      def handle_login():
	acct = db.get(Key.from_path("Account", "n:" + name))
	if not acct:
	  acct = Account(key_name="n:" + name, level=level,
	              nonce="%X" % random.getrandbits(64))
	  acct.put()
	  return acct.nonce
	else:
	  return ""
      nonce = db.run_in_transaction(handle_login)
      if "" == nonce:
	self.response.out.write("Error: Name already in use.")
      else:
	user = User(key_name="n:" + nonce, name=name, state=0, arg="")
	user.put()
	self.response.out.write(nonce)
      return

    if "r" == cmd:  # Lobby refresh.
      nonce = self.request.get("i")
      def heartbeat():
	user = db.get(Key.from_path("User", "n:" + nonce))
	if not user: return None
	user.atime = datetime.now()
	user.put()
	return user
      user = db.run_in_transaction(heartbeat)
      if not user:
	self.response.out.write("Error: No such user ID.")
	return
      users = db.GqlQuery("SELECT * FROM User")
      for u in users:
	self.response.out.write(u.name + '\n')
	self.response.out.write(unicode(u.state) + '\n')
	self.response.out.write(u.arg + '\n')
	"""
	TODO: Remove users after 12 seconds without a heartbeat, also when
	disconnect command is received
	"""
	#self.response.out.write(unicode((user.atime - u.atime).seconds) + '\n')
      return

    if "n" == cmd:  # New duel.
      a = self.request.get("a")
      if "" == a:
	logging.error("Error: No level supplied.")
	return
      level = int(a)
      if level < 1 or level > 5:
	logging.error("Error: Bad level.")
	return
      nonce = self.request.get("i")
      def heartbeat():
	user = db.get(Key.from_path("User", "n:" + nonce))
	if not user: return
	user.atime = datetime.now()
	if 0 == user.state:
	  user.state = 1
	  user.arg = a
	user.put()
      db.run_in_transaction(heartbeat)
      return

    if "N" == cmd:  # Accept duel.
      a = self.request.get("a")
      if "" == a:
	logging.error("Error: No opponent supplied.")
	return
      nonce = self.request.get("i")

      duelid = "%X" % random.getrandbits(64)

      def mark_user():
	origuser = user = db.get(Key.from_path("User", "n:" + nonce))
	if not user:
	  return None, None, -1
	origuser.atime = user.atime = datetime.now()
	if user.state == 2:
	  return None, None, -2
	user.state = 2
	user.arg = a
	user.duel = duelid
	user.put()
	return origuser, user, 0
      origuser, user, status = db.run_in_transaction(mark_user)

      if -1 == status:
	self.response.out.write("Error: No such user ID.")
	return

      if -2 == status:
	logging.warning("Already dueling. Ignoring.")
	return

      acct = db.get(Key.from_path("Account", "n:" + a))
      if not acct:
	origuser.put()
	self.response.out.write("Error: Opponent unavailable.");
	return

      def accept_duel():
	opp = db.get(Key.from_path("User", "n:" + acct.nonce))
	if not opp: return ""
	if 1 != opp.state: return ""
	opp.state = 2
	level = opp.arg
	opp.arg = user.name
	opp.duel = duelid
	opp.put()
	return level
      level = db.run_in_transaction(accept_duel)
      if "" == level:
	self.response.out.write("Error: Opponent unavailable.")
	origuser.put()
	logging.error("accept_duel failed.")
	return

      duel = Duel(key_name = duelid,
		  level = level,
		  now_turn = 0,
		  received_count = 0)
      duel.put()
      self.response.out.write(duelid)
      logging.info("Response: " + duelid)
      return

    # (end if "n" == cmd)
    if "X" == cmd:  # Cancel start of game.
      name = self.request.get("a")
      logging.info("Canceling game: '" + name + "'")
      if "" == name:
        def cancel_anon():
	  anon = db.get(Key.from_path("Anon", "meh"))
	  if anon:
	    anon.delete()
	db.run_in_transaction(cancel_anon)
	return

      def cancel_named():
	ng = db.get(Key.from_path("NewGame", "n:" + name))
	if ng:
	  ng.delete()
      db.run_in_transaction(cancel_named)
      return

    gamename = self.request.get("g")
    game = Game.get_by_key_name("g:" + gamename)

    if "s" == cmd:  # Await for start of game.
      if not game:
	self.response.out.write("-")
      else:
	self.response.out.write(unicode(game.level))
	logging.info(gamename + " level = " + unicode(game.level))
      return

    if not game:
      logging.error("No such game: " + gamename)
      self.response.out.write("Error: No such game.")
      return

    gamekey = game.key()
    playerid = self.request.get("i")
    if ('0' != playerid) and ('1' != playerid):
      logging.error("Bad player ID.")
      self.response.out.write("Error: Bad player ID.")
      return
    def CommandFinish():
      logging.info("Game " + gamename + " finished.")
      self.response.out.write("OK")
      if "" == game.finished:
	def set_finished():
	  game = db.get(gamekey)
	  game.finished = playerid
	  game.put()
	db.run_in_transaction(set_finished)
      elif playerid != game.finished:  # TODO: This should be string comparison.
	# Delete this game.
	game.delete()  # TODO: Also delete moves.
    def CommandSetMove():
      turn_index = self.request.get("j")
      if "" == turn_index:
	logging.error("Error: No turn index.")
	return
      a = self.request.get("a")
      if "" == a:
	logging.error("Error: Bad move.")
	return
      logging.info("SetMove " + gamename + ":" + turn_index +
	  ":" + playerid + " " + a)
      moveid = "m:" + gamename + turn_index + playerid

      move = Move.get_by_key_name(moveid)
      if move:
	logging.warning("Move sent twice: ignored.")
	self.response.out.write("OK")
	return
      else:
	move = Move(key_name = moveid,
		    has_charm = 0,
		    has_para = 0);

      move.move = a
      move.put()

      turn_int = int(turn_index)
      def increment_received_count():
	game = db.get(gamekey)
        if game.now_turn == turn_int:
	  if 2 == game.received_count:
	    logging.error("received_count > 2!")
	  else:
	    game.received_count = game.received_count + 1
	elif game.now_turn == turn_int - 1:
	  if 2 > game.received_count:
	    logging.error("incrementing turn though received_count < 2!")
	  game.now_turn = turn_int
	  game.received_count = 1
	elif game.now_turn > turn_int:
	  logging.error("received ancient move!")
	elif game.now_turn < turn_int - 1:
	  logging.error("received future move!")
	game.put()
      db.run_in_transaction(increment_received_count)
      logging.info("rcount " + unicode(db.get(gamekey).received_count))
      self.response.out.write("OK")
    
    def CommandGetMove():
      if game.chicken:
	self.response.out.write('CHICKEN')
	# TODO: Destroy this game.
	return
      turn_index = self.request.get("j")
      if "" == turn_index:
	logging.error("Error: No turn index.")
	return
      turn_int = int(turn_index)
      if game.now_turn > turn_int or (game.now_turn == turn_int and 2 == game.received_count):
	logging.info("GetMove " + gamename + ":" + turn_index +
	    ":" + playerid + " " + unicode(game.received_count))
	moveid = "m:" + gamename + turn_index + unicode(1 - int(playerid))
	move = Move.get_by_key_name(moveid)
	if not move:
	  logging.error('Error: Cannot find move!')
	else:
	  self.response.out.write(move.move)
      else:
	self.response.out.write('-')
      return
    def CommandSetPara():
      turn_index = self.request.get("j")
      if "" == turn_index:
	logging.error("Error: No turn index.")
	return
      target = self.request.get("a")
      if "" == target:
	logging.error("Error: Bad paralysis target.")
	return
      if "0" == target:
	targetid = playerid
      else:
	targetid = unicode(1 - int(playerid))

      gesture = self.request.get("b")
      if "" == gesture:
	logging.error("Error: Bad paralysis gesture.")
	return
      moveid = "m:" + gamename + turn_index + targetid
      logging.info("SetPara " + moveid)
      move = Move.get_by_key_name(moveid)
      if not move:
	logging.error('Error: Cannot find move!')
	return

      if (1 == move.has_para):
	logging.error("Error: Already received paralysis.")
	return

      def put_para(key):
	move = db.get(key)
	move.para = gesture
	move.has_para = 1;
	move.put()
      db.run_in_transaction(put_para, move.key())
      self.response.out.write("OK")
      return
    def CommandGetPara():
      turn_index = self.request.get("j")
      if "" == turn_index:
	logging.error("Error: No turn index.")
	return
      target = self.request.get("a")
      if "" == target:
	logging.error("Error: Bad paralysis target.")
	return

      if "0" == target:
	targetid = playerid
      else:
	targetid = unicode(1 - int(playerid))

      moveid = "m:" + gamename + turn_index + targetid
      move = Move.get_by_key_name(moveid)
      if not move:
	logging.error('Error: Cannot find move!')
	return
      if 0 == move.has_para:
	self.response.out.write("-")
      else:
	self.response.out.write(move.para)
      return
    def CommandSetCharm():
      turn_index = self.request.get("j")
      if "" == turn_index:
	logging.error("Error: No turn index.")
	return
      # This is unnecessary as we always assume target is opponent.
      target = self.request.get("a")
      if "" == target:
	self.response.out.write("Error: Bad charm target.")
	return
      s = self.request.get("b")
      if "" == s:
	self.response.out.write("Error: Bad charm choices.")
	return
      logging.info("SetCharm " + gamename + ":" + playerid + " " + target + " " + s)
      moveid = "m:" + gamename + turn_index + unicode(1 - int(playerid))
      logging.info("Charm " + moveid)
      move = Move.get_by_key_name(moveid)
      if not move:
	self.response.out.write('Error: Cannot find move!')
	return
      if (1 == move.has_charm):
	self.response.out.write("Error: Already received charm.")
	return;

      def put_charm(key):
	move = db.get(key)
	move.charm_hand = s[0]
	move.charm_gesture = s[1]
	move.has_charm = 1
	move.put()
      db.run_in_transaction(put_charm, move.key())
      self.response.out.write("OK")
      return
    def CommandGetCharmHand():
      turn_index = self.request.get("j")
      if "" == turn_index:
	logging.error("Error: No turn index.")
	return
      moveid = "m:" + gamename + turn_index + playerid
      move = Move.get_by_key_name(moveid)
      if not move:
	logging.error('Error: Cannot find move!')
	return

      if 0 == move.has_charm:
	self.response.out.write("-")
      else:
	self.response.out.write(move.charm_hand)
      return
    def CommandGetCharmGesture():
      turn_index = self.request.get("j")
      if "" == turn_index:
	logging.error("Error: No turn index.")
	return
      moveid = "m:" + gamename + turn_index + playerid
      move = Move.get_by_key_name(moveid)
      if not move:
	logging.error('Error: Cannot find move!')
	return
      if 0 == move.has_charm:
	self.response.out.write("-")
      else:
	self.response.out.write(move.charm_gesture)
      return
    def CommandDisconnect():
      def set_chicken():
	game = db.get(gamekey)
	game.chicken = 1
	game.put()
      db.run_in_transaction(set_chicken)
      logging.info(gamename + ":" + playerid + " flees!")
      self.response.out.write("Chicken!")
      return
    def CommandBad():
      logging.error("Error: Bad command.")
      return
    {'m' : CommandSetMove,
     'g' : CommandGetMove,
     'p' : CommandSetPara,
     'q' : CommandGetPara,
     'f' : CommandFinish,
     'C' : CommandSetCharm,
     'H' : CommandGetCharmHand,
     'G' : CommandGetCharmGesture,
     'D' : CommandDisconnect,
    }.get(cmd, CommandBad)()

application = webapp.WSGIApplication(
                                     [('/', MainPage)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()

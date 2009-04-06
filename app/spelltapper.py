import urllib
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

class Duel(db.Model):
  chicken = db.IntegerProperty()
  ctime = db.DateTimeProperty(auto_now_add=True)
  now_turn = db.IntegerProperty()
  received_count = db.IntegerProperty()
  level = db.StringProperty()

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
  """
  States:
   0 I'm idle.
   1 I propose a duel.
   2 Somebody accepted my challenge.
   3 I acknowledge someone's acceptance.
   4 I accepted somebody's challenge.
   9 I fled a duel.
  """

  arg = db.StringProperty()
  duel = db.StringProperty()

class MainPage(webapp.RequestHandler):
  def get(self):
    if "" == self.request.query_string:
      self.response.out.write("2")
      return
    cmd = self.request.get("c")

    def logoff(userkey):
      def del_user(userkey):
	user = db.get(userkey)
	if not user:
	  return None
	user.delete()
	return user
      u = db.run_in_transaction(del_user, userkey)
      if None == u:
	logging.error("User already deleted.")
	return
      def del_acct():
	acct = db.get(Key.from_path("Account", "n:" + u.name))
	if not acct:
	  logging.error("Missing account for user.")
	  return
	acct.delete()
      db.run_in_transaction(del_acct)

    if "l" == cmd:  # Login.
      name = urllib.unquote(self.request.get("a"))
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

    if "L" == cmd:  # Logoff.
      nonce = self.request.get("i")
      logoff(Key.from_path("User", "n:" + nonce))

    if "r" == cmd:  # Lobby refresh.
      nonce = self.request.get("i")
      def heartbeat():
	user = db.get(Key.from_path("User", "n:" + nonce))
	if not user: return False, None
	user.atime = datetime.now()
	# Someone accepted the duel.
	if 2 == user.state:
	  user.state = 3
	  user.put()
	  return True, user
	user.put()
	return False, user
      flag, user = db.run_in_transaction(heartbeat)
      if not user:
	self.response.out.write("Error: No such user ID.")
	return
      if flag:
	self.response.out.write("\n" + user.arg + "\n" + user.duel)
	return
      users = db.GqlQuery("SELECT * FROM User")
      for u in users:
	self.response.out.write(u.name + '\n')
	self.response.out.write(unicode(u.state) + '\n')
	self.response.out.write(u.arg + '\n')

	if 0 == u.state or 1 == u.state:
	  if user.atime > u.atime and (user.atime - u.atime).seconds >= 12:
	    logging.info(u.name + " timeout: " + unicode((user.atime - u.atime).seconds))
	    logoff(u.key())
	elif 9 == u.state:
	  # Players who leave duels cannot reconnect for a little while.
	  if user.atime > u.atime and (user.atime - u.atime).seconds >= 16:
	    logging.info(u.name + " timeout: " + unicode((user.atime - u.atime).seconds))
	    logoff(u.key())

	# TODO: Uptime user.atime in SetMove and lower timeout to a few minutes.
	elif user.atime > u.atime and (user.atime - u.atime).seconds >= 2048:
	    logging.info(u.name + " timeout: " + unicode((user.atime - u.atime).seconds))
	    logoff(u.key())
      return

    if "n" == cmd:  # New duel.
      logging.info("New duel.")
      a = self.request.get("a")
      if "" == a:
	logging.error("No level supplied.")
	self.response.out.write("Error: No level supplied.")
	return
      level = int(a)
      if level < 1 or level > 5:
	logging.error("Bad level.")
	self.response.out.write("Error: Bad level.")
	return
      nonce = self.request.get("i")
      def new_duel():
	user = db.get(Key.from_path("User", "n:" + nonce))
	if not user: return -2
	user.atime = datetime.now()
	if 0 == user.state:
	  user.state = 1
	  user.arg = a
	  user.put()
	  return 0
	user.put()
	return -1
      status = db.run_in_transaction(new_duel)
      if -2 == status:
	logging.error("No such user.")
	self.response.out.write("Error: No such user.")
      elif -1 == status:
	logging.error("User already started duel.")
	self.response.out.write("Error: Already started duel.")
      else:
	self.response.out.write("OK")
      return

    if "N" == cmd:  # Accept duel.
      logging.info("Accept duel.")
      a = self.request.get("a")
      if "" == a:
	logging.error("Error: No opponent supplied.")
	return
      nonce = self.request.get("i")

      duelid = "%X" % random.getrandbits(64)

      def mark_user():
	user = db.get(Key.from_path("User", "n:" + nonce))
	if not user:
	  return 0, "", None, -1
	user.atime = datetime.now()
	origstate = user.state
	origarg = user.arg
	# Can't accept a duel if you were advertising one and someone just
	# accepted (but you don't know yet). Also can't accept a duel if
	# already in one.
	if 1 != user.state and 0 != user.state:
	  return 0, "", None, -2
	user.state = 4
	user.arg = a
	user.duel = duelid
	user.put()
	return origstate, origarg, user, 0
      origstate, origarg, user, status = db.run_in_transaction(mark_user)

      if -1 == status:
	self.response.out.write("Error: No such user ID.")
	return

      if -2 == status:
	logging.warning("Already dueling. Ignoring.")
	return

      def restore():
        def restore_state_arg(i, s):
	  user = db.get(Key.from_path("User", "n:" + nonce))
	  if user:
	    user.state = i
	    user.arg = s
	    user.put()
	db.run_in_transaction(restore_state_arg, origstate, origarg)
	return

      acct = db.get(Key.from_path("Account", "n:" + a))
      if not acct:
	restore()
	self.response.out.write("Error: Opponent unavailable.")
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
	restore()
	logging.error("accept_duel failed.")
	return

      duel = Duel(key_name = "g:" + duelid,
		  level = level,
		  now_turn = 0,
		  received_count = 0)
      duel.put()
      self.response.out.write(duelid)
      logging.info("Response: " + duelid)
      return

    gamename = self.request.get("g")

    if "f" == cmd:
      logging.info("Game " + gamename + " finished.")
      nonce = self.request.get("i")
      def restate_user():
	user = db.get(Key.from_path("User", "n:" + nonce))
	if not user:
	  return None
	user.atime = datetime.now()
	user.state = 0
	user.put()
	return user
      user = db.run_in_transaction(restate_user)
      if not user:
	self.response.out.write("Error: No such user ID.")
      else:
	self.response.out.write("OK")
      def del_game():
	game = Duel.get_by_key_name("g:" + gamename)
	if game:
	  game.delete()  # TODO: Also delete moves.
      db.run_in_transaction(del_game)
      return

    game = Duel.get_by_key_name("g:" + gamename)

    if not game:
      logging.error("No such game: " + gamename)
      self.response.out.write("Error: No such game.")
      return
    gamekey = game.key()
    playerid = self.request.get("i")

    if "D" == cmd:
      def set_chicken():
	game = db.get(gamekey)
	if game:
	  game.chicken = 1
	  game.put()
      db.run_in_transaction(set_chicken)
      logging.info(gamename + ":" + playerid + " flees!")
      def chicken_user():
	user = db.get(Key.from_path("User", "n:" + playerid))
	if not user:
	  return None
	user.atime = datetime.now()
	user.state = 9
	user.put()
	return user
      db.run_in_transaction(chicken_user)
      #logoff(Key.from_path("User", "n:" + playerid))
      self.response.out.write("Chicken!")
      return

    if ('0' != playerid) and ('1' != playerid):
      logging.error("Bad player ID.")
      self.response.out.write("Error: Bad player ID.")
      return
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
		    has_para = 0)

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
	move.has_para = 1
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
	return

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
    def CommandBad():
      logging.error("Error: Bad command.")
      return
    {'m' : CommandSetMove,
     'g' : CommandGetMove,
     'p' : CommandSetPara,
     'q' : CommandGetPara,
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

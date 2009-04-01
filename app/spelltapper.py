import random
from datetime import datetime

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db

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
  player_i = db.IntegerProperty()
  received_count = db.IntegerProperty()
  ready_count = db.IntegerProperty()
  finished = db.StringProperty()

class MainPage(webapp.RequestHandler):
  def get(self):
    if "" == self.request.query_string:
      self.response.out.write("1")
      return
    cmd = self.request.get("c")
    if "n" == cmd:  # New game.
      version = self.request.get("v")
      if version != '1':
	self.response.out.write("Error: Please update Spelltapper.")
	return
      level = self.request.get("b")
      if "" == level:
	self.response.out.write("Error: No level supplied.")
	return
      name = self.request.get("a")
      game = Game.gql("WHERE name = :1", name).get()
      if None == game:
	game = Game()
	game.name = name
	game.player_i = 0
	game.received_count = 0
	game.ready_count = 0
	game.level = level
	nonce = ''
	for i in range(16):
	  nonce += random.choice('0123456789ABCDEF')
	game.id = nonce
	game.put()  # TODO: Fix race.
	self.response.out.write('0' + game.level + nonce)
      else:
	if 0 == game.player_i:
	  game.player_i = 1
	  if "" == game.name:
	    game.name = game.id
	  game.put()  # TODO: Fix race.
	  self.response.out.write('1' + game.level + game.id)
	else:  # TODO: If 10 mins have elapsed since mtime, erase old game.
	  self.response.out.write('Error: Game "' +
	      name + '" already in progress.')
      return;

    gameid = self.request.get("g")
    game = Game.gql("WHERE id = :1", gameid).get()
    if None == game:
      self.response.out.write("Error: No such game.")
      return
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
	move = Move.gql("WHERE id = :1", moveid).get()
	move.delete()
	psy = Psych.gql("WHERE id = :1", moveid).get()
	if None != psy:
	  psy.delete()
      game.ready_count = 0
      game.received_count = 0
      game.put()  # TODO: Is a race possible here?
    def CommandStart():
      if 1 == game.player_i:
	self.response.out.write("OK")
      else:
	self.response.out.write("-")
    def CommandFinish():
      self.response.out.write("OK")
      if "" == game.finished:
	def set_finished(game):
	  game.finished = playerid
	  game.put()
	db.run_in_transaction(set_finished, game)
      elif playerid != game.finished:
	# Delete this game.
	move = Move.gql("WHERE id = :1", '0' + gameid).get()
	if (None != move): move.delete();
	move = Move.gql("WHERE id = :1", '1' + gameid).get()
	if (None != move): move.delete();
	psy = Psych.gql("WHERE id = :1", '0' + gameid).get()
	if (None != move): move.delete();
	psy = Psych.gql("WHERE id = :1", '1' + gameid).get()
	if (None != move): move.delete();
	game.delete()
    def CommandMove():
      a = self.request.get("a")
      if "" == a:
	self.response.out.write("Error: Bad move.")
	return
      moveid = playerid + gameid
      move = Move.gql("WHERE id = :1", moveid).get()
      if None == move:
	move = Move()
	move.id = moveid
	move.is_ready = 0
	move.move = a
	move.put()
	self.response.out.write("OK")
        def increment_received_count(game):
	  game.received_count = game.received_count + 1
	  game.put()
	db.run_in_transaction(increment_received_count, game)
      else:
	self.response.out.write("Error: Already received move.")
      return
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
	    def increment_ready_count(game):
	      game.ready_count = game.ready_count + 1
	      game.put()
	    db.run_in_transaction(increment_ready_count, game)
	    # All players ready for next turn?
	    if 2 == game.ready_count:
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

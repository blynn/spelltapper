import random

from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db


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
  player_i = db.IntegerProperty()
  received_count = db.IntegerProperty()
  ready_count = db.IntegerProperty()

class MainPage(webapp.RequestHandler):
  def get(self):
    if "" == self.request.query_string:
      self.response.out.write("1")
      return
    name = self.request.get("n")
    if name != "":
      version = self.request.get("v")
      if version != '1':
	self.response.out.write("Error: Please update Spelltapper.")
	return
      game = Game.gql("WHERE name = :1", name).get()
      if None == game:
	game = Game()
	game.name = name
	game.player_i = 0
	game.received_count = 0
	game.ready_count = 0
	s = ''
	for i in range(8):
	  s += random.choice('0123456789ABCDEF')
	game.id = s
	game.put()
	self.response.out.write('0' + s)
      else:
	if 0 == game.player_i:
	  game.player_i = 1
	  game.put()
	  self.response.out.write('1' + game.id)
	else:
	  self.response.out.write('Error: Game "' +
	      name + '" already in progress.')
    else:
      gameid = self.request.get("g")
      game = Game.gql("WHERE id = :1", gameid).get()
      if None == game:
	self.response.out.write("Error: No such game.")
	return
      playerid = self.request.get("i")
      if ('0' != playerid) and ('1' != playerid):
	self.response.out.write("Error: Bad player ID.")
	return
      cmd = self.request.get("c")
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
	  game.received_count = game.received_count + 1
	  game.put()
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
	else:
	  self.response.out.write('-')
	return
      def CommandReady():
	moveid = playerid + gameid
	move = Move.gql("WHERE id = :1", moveid).get()
	if None == move:
	  self.response.out.write("Error: Cannot be ready without moving.")
	else:
	  if 0 == move.is_ready:
	    move.is_ready = 1
	    move.put()
	    game.ready_count = game.ready_count + 1
	    # All players ready for next turn?
	    if 2 == game.ready_count:
	      for i in range(2):
		moveid = unicode(i) + gameid
		move = Move.gql("WHERE id = :1", moveid).get()
		move.delete()
	      game.ready_count = 0
	      game.received_count = 0
	    game.put()
	  self.response.out.write("OK")
	return
      def CommandBad():
	self.response.out.write("Error: Bad command.")
	return
      {'m' : CommandMove,
       'g' : CommandGetMove,
       'r' : CommandReady}.get(cmd, CommandBad)()

application = webapp.WSGIApplication(
                                     [('/', MainPage)],
                                     debug=True)

def main():
  run_wsgi_app(application)

if __name__ == "__main__":
  main()

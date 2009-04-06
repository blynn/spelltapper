package com.benlynn.spelltapper;

import android.util.Log;
import android.view.View;
import android.os.Handler;
import android.os.Message;
import android.os.Looper;

class Lobby extends SpellTapMachine {
  Lobby(SpellTap st) {
    super(st);
    handler = new LobbyHandler();
  }

  void go_back() {
    is_live = false;
    Fry.logout();
    spelltap.lobby_view.setVisibility(View.GONE);
    spelltap.goto_town();
  }
  void run() {
    is_live = true;
    Player.level = Player.true_level;
    LobbyView.has_created_duel = false;
    heartbeat();
  }

  void heartbeat() {
    if (!is_live) return;
    Fry.send_beat();
    handler.sendEmptyMessageDelayed(CMD_BEAT, 4096);
  }

  static void set_list(String s) {
    handler.sendMessage(Message.obtain(handler, CMD_SET_LIST, s));
  }
  static void kick_off() {
    handler.sendEmptyMessage(CMD_DISCONNECT);
  }
  static void duel1() {
    handler.sendEmptyMessage(CMD_DUEL1);
  }
  static void duel0() {
    handler.sendEmptyMessage(CMD_DUEL0);
  }

  static void create_duel(int level) {
    Fry.send_new_duel(level);
  }

  static void accept_duel(String s) {
    Fry.send_accept_duel(s);
  }

  class LobbyHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch(msg.what) {
	case CMD_SET_LIST:
	  if (!is_live) return;
	  spelltap.lobby_view.set_list((String) msg.obj);
	  return;
	case CMD_BEAT:
	  heartbeat();
	  return;
	case CMD_DISCONNECT:
	  go_back();
	  spelltap.narrate(R.string.got_disconnected);
	  return;
	case CMD_DUEL0:
	  Fry.netid = 0;
	  Tubes.netid = "0";
	  Tubes.gameid = Fry.duelid;
	  is_live = false;
	  spelltap.lobby_view.setVisibility(View.GONE);
	  spelltap.mainview.new_net_game();
	  return;
	case CMD_DUEL1:
	  Fry.netid = 1;
	  Tubes.netid = "1";
	  Tubes.gameid = Fry.duelid;
	  is_live = false;
	  spelltap.lobby_view.setVisibility(View.GONE);
	  spelltap.mainview.new_net_game();
	  return;
      }
    }
  }

  static LobbyHandler handler;
  static final int CMD_SET_LIST = 1;
  static final int CMD_BEAT = 2;
  static final int CMD_DISCONNECT = 3;
  static final int CMD_DUEL0 = 4;
  static final int CMD_DUEL1 = 5;
  static boolean is_live;
}

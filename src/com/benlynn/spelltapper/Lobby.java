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
    beat_level = 0;
    LobbyView.level = Player.true_level;
    LobbyView.has_created_duel = false;
    heartbeat();
  }

  void heartbeat() {
    if (!is_live) return;
    Fry.send_beat(beat_level);
    handler.sendEmptyMessageDelayed(CMD_BEAT, 4096);
  }

  static void set_list(String s) {
    handler.sendMessage(Message.obtain(handler, CMD_SET_LIST, s));
  }

  static void create_duel(int level) {
    beat_level = level;
  }

  static void accept_duel(String s) {
  }

  class LobbyHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch(msg.what) {
	case CMD_SET_LIST:
	  spelltap.lobby_view.set_list((String) msg.obj);
	  return;
	case CMD_BEAT:
	  heartbeat();
	  return;
      }
    }
  }

  static int beat_level;
  static LobbyHandler handler;
  static final int CMD_SET_LIST = 1;
  static final int CMD_BEAT = 2;
  static boolean is_live;
}

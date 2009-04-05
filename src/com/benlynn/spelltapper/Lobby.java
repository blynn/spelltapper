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
    Fry.logout();
    spelltap.lobby_view.setVisibility(View.GONE);
    spelltap.goto_town();
  }
  void run() {
  }

  static void set_list(String s) {
    handler.sendMessage(Message.obtain(handler, CMD_SET_LIST, s));
  }

  class LobbyHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch(msg.what) {
	case CMD_SET_LIST:
	  spelltap.lobby_view.set_list((String) msg.obj);
	  return;
      }
    }
  }

  static LobbyHandler handler;
  static final int CMD_SET_LIST = 1;
}

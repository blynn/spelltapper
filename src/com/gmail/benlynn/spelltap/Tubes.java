package com.gmail.benlynn.spelltap;

import java.io.*;
import java.net.*;

import android.util.Log;
import android.os.Handler;
import android.os.Message;

class Tubes extends SpellTapMachine {
  Tubes(SpellTap st) { super(st); }
  abstract class Machine { abstract void run(); }
  void run() {
    spelltap.mainview.set_state_netduel();
    spelltap.goto_mainframe();
  }

  static String reply;
  static class NetThread extends Thread {
    NetThread(String msg) {
      message = msg;
      cmon_handler = new CmonHandler();
    }
    public void run() {
      reply = send(message);
      if (null == reply) {
	cmon_handler.sendEmptyMessageDelayed(0, 3000);
      } else if ('-' == reply.charAt(0)) {
	cmon_handler.sendEmptyMessageDelayed(0, 3000);
      } else {
	MainView.net_handler.sendEmptyMessage(0);
      }
    }
    String message;
    class CmonHandler extends Handler {
      @Override
      public void handleMessage(Message msg) {
	Log.i("Cmon", "retrying");
	reply = send_retry();
	if (null == reply) {
	  sendEmptyMessageDelayed(0, 3000);
	} else if ('-' == reply.charAt(0)) {
	  sendEmptyMessageDelayed(0, 3000);
	} else {
	  MainView.net_handler.sendEmptyMessage(0);
	}
      }
    }
  }
  static Handler cmon_handler;

  static int newgame() {
    String r = send("N");
    if (null == r) {
      return 1;
    }
    netid = r.charAt(0) - 'a';
    return 0;
  }

  static String send_retry() {
    String s = (char) ('a' + netid) + "-";
    return send(s);
  }

  static void send_move(String move) {
    String s = (char) ('a' + netid) + move;
    net_thread = new NetThread(s);
    net_thread.run();
  }
  static Thread net_thread;

  private static String send(String msg) {
    Socket sock = null;
    PrintWriter out = null;
    BufferedReader in = null;

    try {
      sock = new Socket();
      sock.bind(null);
      sock.connect(new InetSocketAddress("192.168.1.101", 3333), 2000);
      out = new PrintWriter(sock.getOutputStream(), true);
      // TODO(blynn): Lose the BufferedReader and PrintWriter.
      in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
      out.println(msg);
      String response = in.readLine();
      out.close();
      in.close();
      sock.close();
      Log.i("Tubes", response);
      return response;
    } catch (UnknownHostException e) {
      Log.e("Tubes", "UnknownHostException");
      return null;
    } catch (IOException e) {
      Log.e("Tubes", "IOException");
      return null;
    }
  }

  static int netid;
}

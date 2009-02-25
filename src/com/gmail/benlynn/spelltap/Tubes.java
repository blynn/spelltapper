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
      retry_handler = cmon_handler = new CmonHandler();
    }
    public void run() {
      reply = send(message);
      cmon_handler.handle_reply();
    }
    class CmonHandler extends Handler {
      @Override
      public void handleMessage(Message msg) {
	Log.i("Cmon", "retrying");
	if ('H' == message.charAt(1)) {
	  reply = send(message);
	} else if ('G' == message.charAt(1)) {
	  reply = send(message);
	} else {
	  reply = send_retry();
	}
	handle_reply();
      }
      public void handle_reply() {
	if (null == reply) {
	  sendEmptyMessageDelayed(0, 3000);
	} else if ('-' == reply.charAt(0)) {
	  sendEmptyMessageDelayed(0, 3000);
	} else {
	  MainView.net_handler.sendEmptyMessage(0);
	}
      }
    }
    String message;
    CmonHandler cmon_handler;
  }
  static Handler retry_handler;

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
    String s = (char) ('a' + netid) + "M" + move;
    net_thread = new NetThread(s);
    net_thread.run();
  }
  static void send_set_charm(int hand, int gesture) {
    String s = (char) ('a' + netid) + "C" + (char) ('0' + hand) +
        (char) ('0' + gesture);
    net_thread = new NetThread(s);
    net_thread.run();
  }
  static void send_get_charm_gesture() {
    String s = (char) ('a' + netid) + "G";
    net_thread = new NetThread(s);
    net_thread.run();
  }
  static void send_get_charm_hand() {
    String s = (char) ('a' + netid) + "H";
    net_thread = new NetThread(s);
    net_thread.run();
  }
  static Thread net_thread;

  private static String send(String msg) {
    Socket sock = null;
    DataOutputStream out = null;
    DataInputStream in = null;
    byte[] buf = new byte[16];

    try {
      sock = new Socket();
      sock.bind(null);
      sock.connect(new InetSocketAddress("192.168.1.101", 3333), 2000);
      out = new DataOutputStream(sock.getOutputStream());
      in = new DataInputStream(sock.getInputStream());
      out.writeBytes(msg);
      int count = in.read(buf, 0, 16);
      Log.i("Count = ", "" + count);
      if (count < 1) return null;
      String response = new String(buf, 0, count);
      Log.i("resp = ", "" + response);
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

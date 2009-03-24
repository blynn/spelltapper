package com.benlynn.spelltapper;

import java.io.*;
import java.net.*;

import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

class Tubes extends SpellTapMachine {
  Tubes(SpellTap st) {
    super(st);
    is_abandoned = false;
    Log.i("Tubes", "cons");
    net_thread = null;
  }
  abstract class Machine { abstract void run(); }
  void run() {
    spelltap.netconfig.setVisibility(View.VISIBLE);
  }

  void go_back() {
    cancel();
  }

  static void cancel() {
    spelltap.netconfig.setVisibility(View.GONE);
    spelltap.goto_town();
  }

  static class NetconfigOk implements View.OnClickListener {
    NetconfigOk() {}
    public void onClick(View v) {
      server = server_edittext.getText().toString();
      boolean badport = false;
      try {
	port = new Integer(port_edittext.getText().toString());
      } catch(Exception e) {
	badport = true;
      }
      if (badport) {
	spelltap.narrate(R.string.badport);
      } else if (0 != ping()) {
	spelltap.narrate(R.string.servererror);
      } else {
        spelltap.mainview.new_game();
      }
    }
  }

  static class NetconfigCancel implements View.OnClickListener {
    NetconfigCancel() {}
    public void onClick(View v) {
      cancel();
    }
  }

  static void init(Button i_ok, Button i_cancel,
      EditText i_server, EditText i_port) {
    ok_button = i_ok;
    cancel_button = i_cancel;
    server_edittext = i_server;
    port_edittext = i_port;
    server_edittext.setText(server);
    port_edittext.setText(Integer.toString(port));
    ok_button.setOnClickListener(new NetconfigOk());
    cancel_button.setOnClickListener(new NetconfigCancel());
  }

  static String reply;
  static class NetThread extends Thread {
    NetThread(String msg) {
      message = msg;
      retry_handler = cmon_handler = new CmonHandler();
    }
    public void run() {
      is_abandoned = false;
      reply = send(message);
      cmon_handler.handle_reply();
    }
    class CmonHandler extends Handler {
      @Override
      public void handleMessage(Message msg) {
	if (is_abandoned) return;
	if ('-' == message.charAt(1)) {
	  reply = send_retry();
	} else {
	  reply = send(message);
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

  static int ping() {
    String r = send("p");
    if (null == r) return 1;
    if (!r.equals("p")) return 1;
    return 0;
  }

  static String send_retry() {
    String s = (char) ('a' + netid) + "-";
    return send(s);
  }
  static void net_send(String s) {
    if (null != net_thread) {
      Log.e("Tubes", "Bug! net_thread != null.");
    }
    net_thread = new NetThread(s);
    net_thread.run();
  }

  static void send_move(String move) {
    net_send((char) ('a' + netid) + "M" + move);
  }
  static void send_set_para(int target, int hand) {
    net_send((char) ('a' + netid) + "P" + (char) ('0' + target) +
        (char) ('0' + hand));
  }
  static void send_set_charm(int hand, int gesture) {
    net_send((char) ('a' + netid) + "C" + (char) ('0' + hand) +
        (char) ('0' + gesture));
  }
  static void send_get_charm_gesture() {
    net_send( (char) ('a' + netid) + "G");
  }
  static void send_get_para(int target) {
    net_send((char) ('a' + netid) + "Q" +(char) ('0' + target));
  }
  static void send_get_charm_hand() {
    net_send((char) ('a' + netid) + "H");
  }

  private static String send(String msg) {
    Socket sock = null;
    DataOutputStream out = null;
    DataInputStream in = null;
    byte[] buf = new byte[16];

    try {
      sock = new Socket();
      sock.bind(null);
      sock.connect(new InetSocketAddress(server, port), 2000);
      out = new DataOutputStream(sock.getOutputStream());
      in = new DataInputStream(sock.getInputStream());
      out.writeBytes(msg);
      int count = in.read(buf, 0, 16);
      if (count < 1) return null;
      String response = new String(buf, 0, count);
      //Log.i("Tubes", "response: " + response);
      out.close();
      in.close();
      sock.close();
      //Log.i("Tubes", response);
      return response;
    } catch (UnknownHostException e) {
      Log.e("Tubes", "UnknownHostException");
      return null;
    } catch (IOException e) {
      Log.e("Tubes", "IOException");
      return null;
    }
  }

  static void load_bundle(Bundle bun) {
    server = bun.getString(ICE_SERVER);
    port = bun.getInt(ICE_PORT);
  }

  static void save_bundle(Bundle bun) {
    bun.putString(ICE_SERVER, server);
    bun.putInt(ICE_PORT, port);
  }

  static final String ICE_SERVER = "game-server";
  static final String ICE_PORT = "game-port";

  static int netid;
  static int state;
  static Button ok_button;
  static Button cancel_button;
  static String server = "192.168.1.101";
  static int port = 3333;
  static EditText server_edittext;
  static EditText port_edittext;
  static boolean is_abandoned;
  static Thread net_thread;
}

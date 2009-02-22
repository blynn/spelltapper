package com.gmail.benlynn.spelltap;

import java.io.*;
import java.net.*;

import android.util.Log;

class Tubes extends SpellTapMachine {
  Tubes(SpellTap st) { super(st); }
  abstract class Machine { abstract void run(); }
  void run() {
    spelltap.mainview.set_state_netduel();
    spelltap.goto_mainframe();
  }

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

  static String send_move(String move) {
    String s = (char) ('a' + netid) + move;
    return send(s);
  }

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

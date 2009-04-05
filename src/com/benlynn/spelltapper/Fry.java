package com.benlynn.spelltapper;

import java.io.*;
import java.net.*;

import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.HttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.DefaultHttpClient;

class Fry extends Thread {
  // These methods typically called from another thread.
  Fry() {
    is_logged_in = false;
    inbuf = new byte[1024];
    client = new DefaultHttpClient();
  }
  public void run() {
    Looper.prepare();
    handler = new FryHandler();
    Looper.loop();
  }
  public void quit() {
    handler.sendEmptyMessage(CMD_QUIT);
  }
  static void login(String i_username, int i_level) {
    if (is_logged_in) {
      Log.e("Fry", "Logging in twice!");
    }
    username = i_username;
    userlevel = i_level;
    handler.sendEmptyMessage(CMD_LOGIN);
  }
  static void logout() {
    handler.sendEmptyMessage(CMD_LOGOUT);
  }
  static void send_beat() {
    handler.sendEmptyMessage(CMD_BEAT);
  }

  // Called from this thread.
  class FryHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
	case CMD_LOGIN:
          send("?c=l&a=" + username + "&b=" + userlevel);
	  if (null == reply || reply.startsWith("Error: ")) {
	    Tubes.login_error(reply);
	  } else {
	    is_logged_in = true;
	    userid = reply;
	    Tubes.login_ok();
	  }
	  return;
	case CMD_LOGOUT:
	  if (!is_logged_in) {
	    Log.e("Fry", "Logging out without logging in!");
	  }
          send("?c=L&i=" + userid);
	  return;
	case CMD_BEAT:
          send("?c=r&i=" + userid);
	  Lobby.set_list(reply);
	  return;
	case CMD_QUIT:
	  Looper.myLooper().quit();
	  return;
      }
    }
  }

  private static void send(String msg) {
    String url = server + msg;
    Log.i("Fry URL", url);
    HttpGet request = new HttpGet(url);
    try {
      HttpResponse response = client.execute(request);
      if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
	reply = null;
	return;
      }
      InputStream in = response.getEntity().getContent();
      // TODO: Use HttpEntity.getContentLength().
      int count = in.read(inbuf, 0, 1024);
      if (count < 0) {
	reply = null;
	return;
      }
      in.close();
      reply = new String(inbuf, 0, count);
      if (reply.startsWith("Error: ")) {
	Log.e("Fry Error", reply);
	reply = null;
	return;
      }
      Log.i("Fry Reply", reply);
    } catch (UnknownHostException e) {
      Log.e("Tubes", "UnknownHostException");
      reply = null;
    } catch (IOException e) {
      Log.e("Tubes", "IOException");
      reply = null;
    }
  }

  static byte[] inbuf;
  static boolean is_logged_in;
  static final int STATE_IDLE = 0;
  static final int CMD_LOGIN = 256;
  static final int CMD_LOGOUT = 257;
  static final int CMD_BEAT = 258;
  static final int CMD_QUIT = 259;
  static FryHandler handler;
  static String reply;
  static String username;
  static int userlevel;
  static String server = "http://10.latest.spelltap.appspot.com/";
  static HttpClient client;
  static String userid;
}

package com.gmail.benlynn.waver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MainView extends View {
  public MainView(Context context, AttributeSet attrs) {
    super(context, attrs);
    msg = "Welcome to Spell Tapper";
    paint = new Paint();
    paint.setARGB(255, 255, 255, 255);
    state = 0;
    gestname = new String[9];
    choice = new int[2];
    lastchoice = new int[2];
    hist = new int[128][2];
    histi = 0;
    histstart = new int[2];
    histstart[1] = histstart[0] = 0;
    choice[0] = choice[1] = flattenxy(0, 0);
    lastchoice[0] = lastchoice[1] = choice[0];
    // Use Unicode arrows as well? e.g. \u2191?
    put_gest("Snap", -1, -1);
    put_gest("Knife", 0, -1);
    put_gest("Digit", 1, -1);
    put_gest("Clap", 1, 0);
    put_gest("Wave", -1, 1);
    put_gest("Palm", 0, 1);
    put_gest("Fingers", 1, 1);
    bmplayer = BitmapFactory.decodeResource(getResources(), R.drawable.wiz);
    bmdummy = BitmapFactory.decodeResource(getResources(), R.drawable.dummy);
  }

  private static int flattenxy(int x, int y) {
    return (x + 1) + (y + 1) * 3;
  }

  private void put_gest(String s, int x, int y) {
    int n = flattenxy(x, y);
    gestname[n] = s;
  }

  static Paint paint;
  static String msg;
  static float x0, y0, x1, y1;
  static int state;
  static String gestname[];
  static int choice[];
  static int lastchoice[];
  static int hist[][], histi, histstart[];
  static final int ylower = 128 + 144 + 4 * 4;
  static final int okbutx0 = 256, okbutx1 = 256 + 64 - 1;
  static final int okbuty0 = ylower + 16 + 50 + 25 , okbuty1= okbuty0 + 25 - 1;
  static final int STAB = flattenxy(0, -1);
  static Bitmap bmplayer, bmdummy;

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Paint boxpaint = new Paint();
    boxpaint.setARGB(255, 32, 32, 64);

    // Spell icon.
    int x = 0;
    int y = 64 + 48 + 2 * 4;
    Bitmap bmstab = BitmapFactory.decodeResource(getResources(),
	R.drawable.stab);
    canvas.drawBitmap(bmstab, x, y, paint);

    // Enemy avatar.
    x = 130;
    y = 0;
    canvas.drawBitmap(bmdummy, x, y, paint);

    // Player.
    y = ylower - 64;
    canvas.drawBitmap(bmplayer, x, y, paint);

    // Player history.
    y = ylower - 4;
    x = 0;
    String s = "";
    for (int i = histstart[0]; i < histi; i++) {
      s += " " + gestname[hist[i][0]].charAt(0);
    }
    canvas.drawText(s, x, y, paint);
    s = "";
    for (int i = histstart[1]; i < histi; i++) {
      s += " " + gestname[hist[i][1]].charAt(0);
    }
    x = 160 + 32;
    canvas.drawText(s, x, y, paint);

    // Gesture area.
    y = ylower;
    canvas.drawRect(0, y, 320, 480, boxpaint);

    // Status line.
    y = ylower + 16;
    canvas.drawText(msg, 0, y - 4, paint);

    // Spell choice row 1
    //canvas.drawRect(0, y, 50 - 1, y + 50 - 1, paint);
    canvas.drawBitmap(bmstab, 1, y + 1, paint);

    // Spell choice row 2
    y += 50;
    canvas.drawRect(0, y, 50 - 1, y + 50 - 1, paint);

    // End-turn button.
    canvas.drawRect(okbutx0, okbuty0, okbutx1, okbuty1, paint);

    y += 50 + 16 - 4;

    s = gestname[choice[0]];
    if (null == s) s = "(nothing)";
    canvas.drawText("Left Hand: " + s, 0, y, paint);
    s = gestname[choice[1]];
    if (null == s) s = "(nothing)";
    canvas.drawText("Right Hand: " + s, 160, y, paint);

    canvas.drawText("Stab", 0, y + 16, paint);

    switch(state) {
      case 0:
	break;
      case 2:
	canvas.drawLine(x0, y0, x1, y1, paint);
	break;
    }
  }

  private static boolean in_ok_button(float x, float y) {
    return x >= okbutx0 && x <= okbutx1 && y >= okbuty0 && y <= okbuty1;
  }
  static boolean okstate;

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
	x0 = event.getX();
	y0 = event.getY();
	if (y0 < ylower) {
	  return false;
	}
	okstate = MainView.in_ok_button(x0, y0);
	state = 1;
	return true;
      case MotionEvent.ACTION_UP:
	x1 = event.getX();
	y1 = event.getY();
	float dx = x1 - x0;
	float dy = y1 - y0;
	if (dx * dx + dy * dy < 32 * 32) {
	  if (okstate && MainView.in_ok_button(x1 ,y1)) {
	    end_turn();
	    return true;
	  }
	} else {
	  int dirx, diry;
	  int h;
	  dirx = dx > 0 ? 1 : -1;
	  diry = dy > 0 ? 1 : -1;
	  if (Math.abs(dy) > Math.abs(dx) * 2) {
	    dirx = 0;
	  } else if (Math.abs(dx) > Math.abs(dy) * 2) {
	    diry = 0;
	  }
	  if (x0 < 160) {
	    h = 0;
	  } else {
	    h = 1;
	    dirx *= -1;
	  }
	  choice[h] = flattenxy(dirx, diry);
	  String s = gestname[choice[h]];
	  if (null == s) {
	    choice[h] = flattenxy(0, 0);
	  }
	}
	handle_new_choice();
	return true;
    }
    return false;
  }

  private void end_turn() {
    msg = "(next turn)";
    hist[histi][0] = choice[0];
    hist[histi][1] = choice[1];
    // Stabs and null gestures break combos.
    if (histi > 0) {
      if (hist[histi - 1][0] == STAB) histstart[0] = histi;
      if (hist[histi - 1][1] == STAB) histstart[1] = histi;
    }
    if (choice[0] == STAB) histstart[0] = histi;
    if (choice[1] == STAB) histstart[1] = histi;
    histi++;
    if (gestname[choice[0]] == null) histstart[0] = histi;
    if (gestname[choice[1]] == null) histstart[1] = histi;
    if (histi > histstart[0] + 6) histstart[0]++;
    if (histi > histstart[1] + 6) histstart[1]++;
    arena.animate();
    invalidate();
  }
  static Arena arena;
  void set_arena(Arena a) {
    arena = a;
  }

  private void handle_new_choice() {
    state = 2;
    for (int h = 0; h < 2; h++) {
      if (lastchoice[h] != choice[h]) {
	msg = "spell search " + Integer.toString(h);
      }
    }
    lastchoice[0] = choice[0];
    lastchoice[1] = choice[1];
    invalidate();
  }
}

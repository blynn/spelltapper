package com.gmail.benlynn.waver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;

public class MainView extends View {
  public MainView(Context context, AttributeSet attrs) {
    super(context, attrs);
    msg = "somemsg";
    paint = new Paint();
    paint.setARGB(255, 255, 255, 255);
    state = 0;
    gestArr = new String[9];
    choice = new int[2];
    lastchoice = new int[2];
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
    put_gest("(nothing)", 0, 0);
  }

  private static int flattenxy(int x, int y) {
    return (x + 1) + (y + 1) * 3;
  }

  private void put_gest(String s, int x, int y) {
    int n = flattenxy(x, y);
    gestArr[n] = s;
  }

  static Paint paint;
  static String msg;
  static float x0, y0, x1, y1;
  static int state;
  static String gestArr[];
  static int choice[];
  static int lastchoice[];
  static final int ylower = 128 + 144 + 4 * 4;
  static final int okbutx0 = 256, okbutx1 = 256 + 64 - 1;
  static final int okbuty0 = ylower + 16 + 50 + 25 , okbuty1= okbuty0 + 25 - 1;

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    Paint boxpaint = new Paint();
    boxpaint.setARGB(255, 32, 32, 64);

    // Spell icon.
    int x = 0;
    int y = 64 + 48 + 2 * 4;
    canvas.drawRect(x, y, x + 48 - 1, y + 48 - 1, boxpaint);

    // Enemy avatar.
    x = 130;
    y = 0;
    canvas.drawRect(x, y, x + 64 - 1, y + 64 - 1, boxpaint);

    // Gesture area.
    y = ylower;
    canvas.drawRect(0, y, 320, 480, boxpaint);

    // Status line.
    y = ylower + 16;
    canvas.drawText(msg, 0, y - 4, paint);

    // Spell choice row 1
    //canvas.drawRect(0, y, 50 - 1, y + 50 - 1, paint);
    Bitmap bmstab = BitmapFactory.decodeResource(getResources(),
	R.drawable.stab);
    canvas.drawBitmap(bmstab, 1, y + 1, paint);

    // Spell choice row 2
    y += 50;
    canvas.drawRect(0, y, 50 - 1, y + 50 - 1, paint);

    // End-turn button.
    canvas.drawRect(okbutx0, okbuty0, okbutx1, okbuty1, paint);

    y += 50 + 16 - 4;

    switch(state) {
      case 0:
	break;
      case 2:
	canvas.drawLine(x0, y0, x1, y1, paint);
	break;
    }
    canvas.drawText("Left Hand: " + gestArr[choice[0]], 0, y, paint);
    canvas.drawText("Right Hand: " + gestArr[choice[1]], 160, y, paint);
    canvas.drawText("Stab", 0, y + 16, paint);
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
	  String s = gestArr[choice[h]];
	  if (null == s) {
	    choice[h] = flattenxy(0, 0);
	  } else {
	    char ch = s.charAt(0);
	    if (ch == 'K' && choice[h] == choice[1 - h]) {
	      choice[1 - h] = flattenxy(0, 0);
	    }
	  }
	}
	handle_new_choice();
	return true;
    }
    return false;
  }

  private void end_turn() {
    msg = "(next turn)";
    invalidate();
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

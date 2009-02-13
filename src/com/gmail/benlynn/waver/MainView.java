package com.gmail.benlynn.waver;

import android.content.Context;
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
    choice = new char[2];
    choice[0] = choice[1] = '0';
    put_gest("Snap", -1, -1);
    put_gest("Knife", 0, -1);
    put_gest("Digit", 1, -1);
    put_gest("Clap", 1, 0);
    put_gest("Wave", -1, 1);
    put_gest("Palm", 0, 1);
    put_gest("Fingers", 1, 1);
  }

  private void put_gest(String s, int x, int y) {
    int n = (x + 1) + (y + 1) * 3;
    gestArr[n] = s;
  }

  private String get_gest(int x, int y) {
    int n = (x + 1) + (y + 1) * 3;
    return gestArr[n];
  }

  static Paint paint;
  static String msg;
  static float x0, y0, x1, y1;
  static int state;
  static String gestArr[];
  static char choice[];

  static final int ylower = 128 + 144 + 4 * 4;

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.drawText(msg, 50, 50, paint);
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
    canvas.drawText("Some status message", 0, y - 4, paint);

    // Spell choice row 1
    canvas.drawRect(0, y, 50 - 1, y + 50 - 1, paint);

    // Spell choice row 2
    y += 50;
    canvas.drawRect(0, y, 50 - 1, y + 50 - 1, paint);

    y += 50 + 16 - 4;

    switch(state) {
      case 0:
	break;
      case 2:
	canvas.drawLine(x0, y0, x1, y1, paint);
	break;
    }
    canvas.drawText("Left: " + choice[0], 0, y, paint);
    canvas.drawText("Right: " + choice[1], 160, y, paint);
    canvas.drawText("Stab", 0, y + 16, paint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    // Log.i("Wave", "got event " + event.getAction());
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:

	x0 = event.getX();
	y0 = event.getY();
	if (y0 < ylower) {
	  return false;
	}
	state = 1;
	return true;
      case MotionEvent.ACTION_UP:
	x1 = event.getX();
	y1 = event.getY();
	float dx = x1 - x0;
	float dy = y1 - y0;
	if (dx * dx + dy * dy < 32 * 32) {
	  msg = "too short";
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
	  if (x0 < 120) {
	    h = 0;
	  } else {
	    h = 1;
	    dirx *= -1;
	  }
	  String s = get_gest(dirx, diry);
	  if (null == s) {
	    choice[h] = '0';
	  } else {
	    choice[h] = s.charAt(0);
	    if (choice[0] == 'K' && choice[1] == 'K') {
	      choice[1 - h] = '0';
	    }
	  }
	}
	state = 2;
	invalidate();
	return true;
    }
    return false;
  }
}

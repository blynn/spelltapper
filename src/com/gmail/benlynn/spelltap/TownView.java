package com.gmail.benlynn.spelltap;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.util.Log;
import android.os.Handler;
import android.os.Message;

public class TownView extends View {
  public TownView(Context context, AttributeSet attrs) {
    super(context, attrs);
    paint = new Paint();
    fgpaint = new Paint();
    fgpaint.setARGB(255, 255, 255, 255);
    bgpaint = new Paint();
    bgpaint.setARGB(255, 0, 0, 191);
    bmplayer = BitmapFactory.decodeResource(getResources(), R.drawable.wiz);
    bmacademy = BitmapFactory.decodeResource(getResources(), R.drawable.academy);
    xplayer = 160 - 32;
    yplayer = 240 - 32;
    machine = new FirstMachine();
    is_animating = false;
  }

  void narrate(int string_constant) {
    narrator.setVisibility(View.GONE);
    narrator.setVisibility(View.VISIBLE);
    narratortext.setText(string_constant);
    ui_state = STATE_NARRATE;
  }

  void narrate_off() {
    narrator.setVisibility(View.GONE);
  }

  abstract class Machine {
    abstract void run();
  }

  class FirstMachine extends Machine {
    FirstMachine() {
      state = 0;
    }
    void run() {
      for(;;) switch(state) {
	case 0:
	  ui_state = STATE_NARRATE;
	  state = 1;
	  return;
	case 1:
	  narrate_off();
	  ui_state = STATE_ON_TAP;
	  state = 2;
	  count = 0;
	  return;
	case 2:
	  if (choice != 0) {
	    count++;
	    if (count == 3) {
	      narrate(R.string.academynag);
	      state = 1;
	      count = 0;
	    }
	  } else {
	    ui_state = STATE_NORMAL;
	    machine = new NormalMachine();
	  }
	  return;
      }
    }
    int state;
    int count;
  }

  class NormalMachine extends Machine {
    void run() {
      if (location == 0) {
	// TODO: Fade screen.
	setVisibility(View.GONE);
	mainframe.setVisibility(View.VISIBLE);
      }
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    canvas.drawRect(0, 0, 128, 16, bgpaint);
    canvas.drawText("Academy", 5, 16 - 4, fgpaint);
    canvas.drawBitmap(bmacademy, 0, 16, paint);

    canvas.drawBitmap(bmplayer, xplayer, yplayer, paint);
  }

  static float x0, y0, x1, y1;
  static int choice;
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (is_animating) return false;
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
	if (STATE_NARRATE == ui_state) return true;
	x0 = event.getX();
	y0 = event.getY();
	choice = -1;
	if (x0 < 128 && y0 < 96) {
	  choice = 0;
	}
	return true;
      case MotionEvent.ACTION_UP:
	if (STATE_NARRATE == ui_state) {
	  machine.run();
	  return true;
	}
	x1 = event.getX();
	y1 = event.getY();
	if (choice == 0 && x1 < 128 && y1 < 96) {
	} else choice = -1;
	if (STATE_ON_TAP == ui_state) machine.run();
	if (choice != -1) travel();
	return true;
    }
    return false;
  }

  private void update() {
    xplayer += xdelta;
    yplayer += ydelta;
    if (frame < frame_max) {
      frame++;
      anim_handler.sleep(delay);
    } else {
      is_animating = false;
      machine.run();
    }
  }

  private RefreshHandler anim_handler = new RefreshHandler();
  class RefreshHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      TownView.this.update();
      TownView.this.invalidate();
    }

    public void sleep(long delayMillis) {
      this.removeMessages(0);
      sendEmptyMessageDelayed(0, delayMillis);
    }
  }

  void travel() {
    location = choice;
    is_animating = true;
    xtarget = 64 - 32;
    ytarget = 96;
    ydelta = (ytarget - yplayer) / frame_max;
    xdelta = (xtarget - xplayer) / frame_max;
    frame = 0;
    anim_handler.sleep(delay);
  }

  static boolean is_animating;
  static Paint paint;
  static Paint fgpaint, bgpaint;
  static Bitmap bmplayer;
  static Bitmap bmacademy;
  static float xplayer, yplayer;
  static float xtarget, ytarget;
  static float xdelta, ydelta;
  static Machine machine;
  static final int STATE_NORMAL = 0;
  static final int STATE_NARRATE = 1;
  static final int STATE_ON_TAP = 2;
  static int ui_state;
  static View narrator;
  static TextView narratortext;
  static int delay = 32;
  static int frame_max = 24;
  static int frame;
  static int location = -1;
  static View mainframe;
}

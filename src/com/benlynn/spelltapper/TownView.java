package com.benlynn.spelltapper;

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
    xplayer = 160 - 32;
    yplayer = 240 - 32;
    location = -1;
    ui_state = STATE_NORMAL;
    machine = new FirstMachine();
    is_animating = false;

    place_list = new Place[SpellTap.PLACE_TOWN_COUNT];
    put_place(SpellTap.PLACE_SCHOOL, "Academy", 0, 0, R.drawable.academy);
    put_place(SpellTap.PLACE_DOJO, "Training Hall",
	320 - 128, 0, R.drawable.training);
    put_place(SpellTap.PLACE_PIT, "Arena", 0, 96 + 64, R.drawable.arena);
    put_place(SpellTap.PLACE_NET, "Tavern",
	320 - 128, 96 + 64, R.drawable.tavern);
    unlock(SpellTap.PLACE_SCHOOL);
    for (int i = 0; i < SpellTap.PLACE_TOWN_COUNT; i++) {
      if (null == place_list[i]) {
	Log.e("TownView", "null Place remains.");
      }
    }
  }

  void put_place(int i, String name, int x, int y, int bitmapid) {
    Place p = new Place(name, x, y, bitmapid);
    place_list[i] = p;
  }

  void unlock(int i) {
    place_list[i].is_locked = false;
  }

  void narrate(int string_constant) {
    spelltap.narrate(string_constant);
  }

  void show_tip(int string_constant) {
    spelltap.show_tip(string_constant);
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
	  narrate(R.string.town0);
	  state = 1;
	  return;
	case 1:
	  narrate(R.string.town1);
	  state = 2;
	  return;
	case 2:
	  ui_state = STATE_ON_TAP;
	  show_tip(R.string.academynag);
	  state = 3;
	  return;
	case 3:
	  if (SpellTap.PLACE_SCHOOL == choice) {
	    if (SpellTap.PLACE_SCHOOL == location) {
	      spelltap.tip_off();
	      ui_state = STATE_NORMAL;
	      travel();
	    } else {
	      show_tip(R.string.academynag2);
	    }
	  }
	  return;
      }
    }
    int state;
    int count;
  }

  class NormalMachine extends Machine {
    void run() {}
  }
  void set_state_first() { machine = new FirstMachine(); }
  void set_state_normal() { machine = new NormalMachine(); }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    for (int i = 0; i < SpellTap.PLACE_TOWN_COUNT; i++) {
      Place p = place_list[i];
      if (!p.is_locked) {
	canvas.drawRect(p.x, p.y, p.x + 128, p.y + 16, bgpaint);
	canvas.drawText(p.name, p.x + 5, p.y + 16 - 4, fgpaint);
	canvas.drawBitmap(p.bitmap, p.x, p.y + 16, paint);
      }
    }
    canvas.drawBitmap(bmplayer, xplayer, yplayer, paint);
  }

  static float x0, y0, x1, y1;
  static int choice;
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (is_animating) return false;
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
	x0 = event.getX();
	y0 = event.getY();
	choice = -1;
	for (int i = 0; i < SpellTap.PLACE_TOWN_COUNT; i++) {
	  Place p = place_list[i];
	  if (!p.is_locked) {
	    if (x0 >= p.x && x0 < p.x + 128 && y0 >= p.y && y0 < p.y + 96) {
	      choice = i;
	      break;
	    }
	  }
	}
	return true;
      case MotionEvent.ACTION_UP:
	x1 = event.getX();
	y1 = event.getY();
	if (choice != -1) {
	  // If a tap landed on a building, it must leave on the
	  // same building to count.
	  Place p = place_list[choice];
	  if (!(x0 >= p.x && x0 < p.x + 128 && y0 >= p.y && y0 < p.y + 96)) {
	    choice = -1;
	  }
	}
	if (STATE_ON_TAP == ui_state) {
	  machine.run();
	}
	if (choice != -1) {
	  if (choice != location) animate_move();
	  else travel();
	}
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
      xplayer = xtarget;
      yplayer = ytarget;
      is_animating = false;
      location = choice;
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

  void animate_move() {
    Place p = place_list[choice];
    is_animating = true;
    xtarget = p.x + 64 - 32;
    ytarget = p.y + 96 - 40;
    ydelta = (ytarget - yplayer) / frame_max;
    xdelta = (xtarget - xplayer) / frame_max;
    frame = 0;
    anim_handler.sleep(delay);
  }

  void travel() {
    // TODO: Fade screen.
    setVisibility(View.GONE);
    spelltap.set_place(location);
  }

  class Place {
    Place(String i_name, int i_x, int i_y, int bitmapid){
      name = i_name;
      x = i_x;
      y = i_y;
      bitmap = BitmapFactory.decodeResource(getResources(), bitmapid);
      is_locked = true;
    }
    String name;
    int x, y;
    Bitmap bitmap;
    boolean is_locked;
  }

  void set_spelltap(SpellTap i_spelltap) {
    spelltap = i_spelltap;
    stmach = new Kludge(spelltap);
  }
  class Kludge extends SpellTapMachine {
    Kludge(SpellTap st) { super(st); }
    void run() { machine.run(); }
    void go_back() {
      Log.e("TownView", "BACK should have been handled already.");
    }
  }
  static SpellTapMachine stmach;

  static Place place_list[];
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
  static final int STATE_ON_TAP = 1;
  static int ui_state;
  static int delay = 24;
  static int frame_max = 24;
  static int frame;
  static int location;
  static SpellTap spelltap;
}

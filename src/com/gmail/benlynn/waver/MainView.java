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
  static Paint paint, selpaint;
  static String msg;
  static float x0, y0, x1, y1;
  static int state;
  static String gestname[];
  static int choice[];  // Gesture choice.
  static int lastchoice[];
  static int hist[][], histi, histstart[];
  static final int ylower = 128 + 144 + 4 * 4;
  // TODO: Use Rect.
  static final int okbutx0 = 256, okbutx1 = 256 + 64 - 1;
  static final int okbuty0 = ylower + 16 + 50 + 25 , okbuty1= okbuty0 + 25 - 1;
  static final int KNIFE = flattenxy(0, -1);
  static final int NO_GESTURE = flattenxy(0, 0);
  static Bitmap bmplayer, bmdummy;
  static String spell_text[];
  static int ready_spell_count[];
  static Spell[][] ready_spell;
  static Spell[] spell_list;
  static int spell_list_count;
  static int spell_choice[];

  public MainView(Context context, AttributeSet attrs) {
    super(context, attrs);
    msg = "This is Spell Tap";
    paint = new Paint();
    paint.setARGB(255, 255, 255, 255);
    selpaint = new Paint();
    selpaint.setARGB(255, 127, 255, 127);
    state = 0;
    gestname = new String[9];
    choice = new int[2];
    lastchoice = new int[2];
    hist = new int[128][2];
    histi = 0;
    histstart = new int[2];
    histstart[1] = histstart[0] = 0;
    choice[0] = choice[1] = NO_GESTURE;
    lastchoice[0] = lastchoice[1] = choice[0];
    ready_spell_count = new int[2];
    ready_spell = new Spell[4][2];
    ready_spell_count[0] = ready_spell_count[1] = 0;
    spell_choice = new int[2];
    spell_choice[0] = spell_choice[1] = 0;
    spell_text = new String[2];
    spell_text[0] = "";
    spell_text[1] = "";
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
    stab_spell = new Spell("Stab", "K", R.drawable.stab);
    spell_list = new Spell[64];
    spell_list_count = 0;
    add_spell("Shield", "P", R.drawable.shield);
    add_spell("Missile", "SD", R.drawable.missile);
  }

  public void add_spell(String name, String gesture, int bitmapid) {
    Spell sp = new Spell(name, gesture, bitmapid);
    spell_list[spell_list_count] = sp;
    spell_list_count++;
  }

  static Spell stab_spell;

  private static int flattenxy(int x, int y) {
    return (x + 1) + (y + 1) * 3;
  }

  private void put_gest(String s, int x, int y) {
    int n = flattenxy(x, y);
    gestname[n] = s;
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    int x, y;
    Paint boxpaint = new Paint();
    boxpaint.setARGB(255, 32, 32, 64);

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

    // Spell icons.
    x = 0;
    y = 64 + 48 + 2 * 4;
    for (int h = 0; h < 2; h++) {
      if (ready_spell_count[h] > 0) {
	canvas.drawBitmap(ready_spell[spell_choice[h]][h].bitmap, x, y, paint);
      }
      x = 320 - 48 - 1;
    }

    // Gesture area.
    y = ylower;
    canvas.drawRect(0, y, 320, 480, boxpaint);

    // Gesture and spell text.
    y = ylower + 16 - 4;
    s = gestname[choice[0]];
    if (null == s) s = "(nothing)";
    canvas.drawText("Left Hand: " + s, 0, y, paint);
    s = gestname[choice[1]];
    if (null == s) s = "(nothing)";
    canvas.drawText("Right Hand: " + s, 160, y, paint);

    canvas.drawText(spell_text[0], 0, y + 16, paint);
    canvas.drawText(spell_text[1], 160, y + 16, paint);

    // Spell choice row 1
    x = 0;
    y = ylower + 32;
    for (int h = 0; h < 2; h++) {
      for (int i = 0; i < ready_spell_count[h]; i++) {
	if (i == spell_choice[i]) {
	  canvas.drawRect(x, y, x + 50, y + 50, selpaint);
	}
	canvas.drawBitmap(ready_spell[i][h].bitmap, x + 1, y + 1, paint);
	x += 50;
      }
      x = 160;
    }

    // Spell choice row 2
    /*
    y += 50;
    canvas.drawRect(0, y, 50 - 1, y + 50 - 1, paint);
    */

    // End-turn button.
    canvas.drawRect(okbutx0, okbuty0, okbutx1, okbuty1, paint);

    // Status line.
    y = ylower + 32 + 2 * 50 + 16;
    canvas.drawText(msg, 0, y - 4, paint);

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
	  if (null == s) choice[h] = NO_GESTURE;
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
      if (hist[histi - 1][0] == KNIFE) histstart[0] = histi;
      if (hist[histi - 1][1] == KNIFE) histstart[1] = histi;
    }
    if (choice[0] == KNIFE) histstart[0] = histi;
    if (choice[1] == KNIFE) histstart[1] = histi;
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
    for (int h = 0; h < 2; h++) if (lastchoice[h] != choice[h]) {
      ready_spell_count[h] = 0;
      if (choice[h] == NO_GESTURE) continue;
      if (choice[h] == KNIFE) {
	if (choice[1 - h] == KNIFE) {
	  spell_text[h] = "(only one knife)";
	} else {
	  add_ready_spell(h, stab_spell);
	}
      } else {
	if (lastchoice[h] == KNIFE && choice[1 - h] == KNIFE) {
	  ready_spell_count[1 - h] = 0;
	  add_ready_spell(1 - h, stab_spell);
	}
	spell_text[h] = "";
	for (int i = 0; i < spell_list_count; i++) {
	  String g = spell_list[i].gesture;
	  int k = g.length();
	  if (k > histi - histstart[h] + 1) continue;
	  k--;
	  Log.i("M", "" + g + ":" + gestname[choice[h]]);
	  if (g.charAt(k) != gestname[choice[h]].charAt(0)) continue;
	  k--;
	  int k2 = histi - 1;
	  while (k >= 0) {
	    Log.i("M", "" + g + ":" + gestname[hist[k2][h]]);
	    if (g.charAt(k) != gestname[hist[k2][h]].charAt(0)) {
	      break;
	    }
	    k2--;
	    k--;
	  }
	  if (0 > k) {
	    // At last we have a match.
	    add_ready_spell(h, spell_list[i]);
	  }
	}
      }
    }
    lastchoice[0] = choice[0];
    lastchoice[1] = choice[1];
    invalidate();
  }

  public void add_ready_spell(int h, Spell sp) {
    ready_spell[ready_spell_count[h]][h] = sp;
    ready_spell_count[h]++;
    spell_text[h] = sp.name;
  }

  public class Spell {
    public Spell(String new_name, String new_gest, int bitmapid) {
      name = new_name;
      gesture = new_gest;
      bitmap = BitmapFactory.decodeResource(getResources(), bitmapid);
    }

    Bitmap bitmap;
    String name;
    String gesture;
  }
}

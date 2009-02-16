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
import android.os.Handler;
import android.os.Message;

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
  static final int yicon = 64 + 48 + 2 * 4;
  // TODO: Use Rect.
  static final int okbutx0 = 256, okbutx1 = 256 + 64 - 1;
  static final int okbuty0 = ylower + 16 + 50 + 25 , okbuty1= okbuty0 + 25 - 1;
  static final int KNIFE = flattenxy(0, -1);
  static final int NO_GESTURE = flattenxy(0, 0);
  static String spell_text[];
  static int ready_spell_count[];
  static Spell[][] ready_spell;
  static Spell[] spell_list;
  static int spell_list_count;
  static int spell_choice[];
  static int spell_target[];
  static int being_list_count;
  static Being being_list[];
  static Arena arena;
  static ArrowView arrow_view;
  void set_arena(Arena a) {
    arena = a;
  }
  void set_arrow_view(ArrowView a) {
    arrow_view = a;
  }

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
    stab_spell = new StabSpell();
    spell_list = new Spell[64];
    spell_list_count = 0;
    add_spell(new ShieldSpell());
    add_spell(new MissileSpell());
    add_spell(new CauseLightWoundsSpell());

    being_list = new Being[16];
    being_list_count = 0;

    being_list_count++;
    being_list[0] = new Being(160 - 32, ylower - 64, R.drawable.wiz);
    being_list[0].w = 64;
    being_list[0].h = 64;
    being_list[0].midw = 32;
    being_list[0].midh = 32;
    being_list_count++;
    being_list[1] = new Being(160 - 32, 0, R.drawable.dummy);
    being_list[1].w = 64;
    being_list[1].h = 64;
    being_list[1].midw = 32;
    being_list[1].midh = 32;

    spell_target = new int[2];
    exec_queue = new SpellCast[16];

    arena.being_list = being_list;
    arena.being_list_count = being_list_count;
  }

  public void add_spell(Spell sp) {
    spell_list[spell_list_count] = sp;
    spell_list_count++;
  }

  static StabSpell stab_spell;

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
    boxpaint.setARGB(255, 32, 32, 32);

    // Arena handles avatars.

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
    y = yicon;
    arrow_view.clear_arrows();
    for (int h = 0; h < 2; h++) {
      if (ready_spell_count[h] > 0) {
	Spell sp = ready_spell[spell_choice[h]][h];
	canvas.drawBitmap(sp.bitmap, x, y, paint);
	spell_text[h] = sp.name;
	if (spell_target[h] >= 0) {
	  Being b = being_list[spell_target[h]];
	  arrow_view.add_arrow(x + 24, y + 24, b.x + b.midw, b.y + b.midh);
	  arrow_view.invalidate();
	}
      }
      x = 320 - 48 - 1;
    }

    // Gesture area.
    y = ylower;
    canvas.drawRect(0, y, 320, 480, boxpaint);

    // Gesture and spell text.
    y = ylower + 16 - 4;
    s = gestname[choice[0]];
    if (null == s) s = ""; //"(nothing)";
    canvas.drawText("Left Hand: " + s, 0, y, paint);
    s = gestname[choice[1]];
    if (null == s) s = ""; //"(nothing)";
    canvas.drawText("Right Hand: " + s, 160, y, paint);

    canvas.drawText(spell_text[0], 0, y + 16, paint);
    canvas.drawText(spell_text[1], 160, y + 16, paint);

    // Spell choice row 1
    x = 0;
    y = ylower + 32;
    for (int h = 0; h < 2; h++) {
      for (int i = 0; i < ready_spell_count[h]; i++) {
	if (i == spell_choice[h]) {
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
  }

  private static boolean in_ok_button(float x, float y) {
    return x >= okbutx0 && x <= okbutx1 && y >= okbuty0 && y <= okbuty1;
  }
  static boolean okstate;

  private void choose_spell(int h, int i) {
    // Assumes i is a valid choice for hand h.
    spell_choice[h] = i;
    spell_target[h] = ready_spell[i][h].target;
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
	x0 = event.getX();
	y0 = event.getY();
	if (y0 < ylower) {
	  if (y0 >= yicon && y0 < yicon + 48) {
	    if (x0 < 48) {
	      state = 2;
	      return true;
	    } else if (x0 >= 320 - 48) {
	      state = 3;
	      return true;
	    }
	  }
	  state = 0;
	  return false;
	}
	okstate = MainView.in_ok_button(x0, y0);
	state = 1;
	return true;
      case MotionEvent.ACTION_UP:
	x1 = event.getX();
	y1 = event.getY();
	if (2 == state || 3 == state) {
	  for(int i = 0; i < being_list_count; i++) {
	    Being b = being_list[i];
	    if (x1 >= b.x && y1 >= b.y && x1 < b.x + 64 && y1 < b.y + 64) {
	      spell_target[state - 2] = i;
	      arrow_view.invalidate();
	      return true;
	    }
	  }
	  spell_target[state - 2] = -1;
	  arrow_view.invalidate();
	  return true;
	}
	float dx = x1 - x0;
	float dy = y1 - y0;
	if (dx * dx + dy * dy < 32 * 32) {
	  if (okstate && MainView.in_ok_button(x1, y1)) {
	    end_turn();
	    return true;
	  }
	  if (y1 >= ylower + 32 && y1 < ylower + 32 + 50) {
	    // Could be choosing a ready spell.
	    for (int h = 0; h < 2; h++) {
	      int i;
	      if (h == 1) {
		if (x1 < 160) break;
		i = ((int) x1 - 160) / 50;
	      } else {
		i = ((int) x1) / 50;
	      }
	      if (i >= 0 && i < ready_spell_count[h]) {
		choose_spell(h, i);
		invalidate();
		return true;
	      }
	    }
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
	  if (null == gestname[choice[h]]) choice[h] = NO_GESTURE;
	  if (choice[h] != lastchoice[h]) {
	    handle_new_choice(h);
	  }
	  return true;
	}
    }
    return false;
  }

  class SpellCast {
    SpellCast(Spell init_spell, int init_source, int init_target) {
      spell = init_spell;
      source = init_source;
      target = init_target;
    }
    Spell spell;
    int target;
    int source;
  }
  static int exec_queue_count;
  static SpellCast[] exec_queue;

  private void end_turn() {
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

    exec_queue_count = 0;
    for (int h = 0; h < 2; h++) {
      if (0 == ready_spell_count[h]) continue;
      if (-1 == spell_choice[h]) continue;
      SpellCast sc = new SpellCast(
	  ready_spell[spell_choice[h]][h], 0, spell_target[h]);
      exec_queue[exec_queue_count] = sc;
      exec_queue_count++;
    }

    // TODO: Clear gestures, spell text.
    ready_spell_count[0] = 0;
    ready_spell_count[1] = 0;
    invalidate();

    exec_cursor = 0;
    next_spell();
  }

  static int exec_cursor;
  public void next_spell() {
    Log.i("M", "Next spell");
    Log.i("M", Integer.toString(exec_cursor) + " " + Integer.toString(exec_queue_count));
    if (exec_cursor < exec_queue_count) {
    //Log.i("M", Integer.toString(exec_cursor) + " " + Integer.toString(exec_queue_count));
      SpellCast sc = exec_queue[exec_cursor];
      sc.spell.execute(sc.source, sc.target);
      exec_cursor++;
    } else {
      // TODO: Re-enable gestures.
      Log.i("M", "Setup next turn");
    }
  }

  private void handle_new_choice(int h) {
    ready_spell_count[h] = 0;
    if (choice[h] == KNIFE) {
      if (choice[1 - h] == KNIFE) {
	spell_text[h] = "(only one knife)";
      } else {
	add_ready_spell(h, stab_spell);
      }
    } else if (choice[h] != NO_GESTURE) {
      if (lastchoice[h] == KNIFE && choice[1 - h] == KNIFE) {
	ready_spell_count[1 - h] = 0;
	add_ready_spell(1 - h, stab_spell);
	choose_spell(1 - h, 0);
      }
      spell_text[h] = "";
      for (int i = 0; i < spell_list_count; i++) {
	String g = spell_list[i].gesture;
	int k = g.length();
	if (k > histi - histstart[h] + 1) continue;
	k--;
	if (g.charAt(k) != gestname[choice[h]].charAt(0)) continue;
	k--;
	int k2 = histi - 1;
	while (k >= 0) {
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
    if (ready_spell_count[h] > 0) {
      choose_spell(h, 0);
    }
    lastchoice[0] = choice[0];
    lastchoice[1] = choice[1];
    invalidate();
  }

  public void add_ready_spell(int h, Spell sp) {
    ready_spell[ready_spell_count[h]][h] = sp;
    ready_spell_count[h]++;
  }

  abstract public class Spell {
    public void init(String init_name, String init_gest, int bitmapid,
        int def_target) {
      name = init_name;
      gesture = init_gest;
      bitmap = BitmapFactory.decodeResource(getResources(), bitmapid);
      target = def_target;
    }

    abstract public void cast(int init_source, int init_target);
    Bitmap bitmap;
    String name;
    String gesture;
    int target;
    int state;
    boolean is_finished;  // Set this to true before calling last animation.
    int cast_source, cast_target;

    public void execute(int init_source, int init_target) {
      state = 0;
      is_finished = false;
      arena.set_notify_me(done_handler);
      cast_source = init_source;
      cast_target = init_target;
      cast(cast_source, cast_target);
    }

    public void stub_finish() {
      is_finished = true;
      done_handler.sendEmptyMessage(0);
    }
    private DoneHandler done_handler = new DoneHandler();
    class DoneHandler extends Handler {
      @Override
      public void handleMessage(Message msg) {
	if (is_finished) {
	  MainView.this.next_spell();
	} else {
	  state++;
	  cast(cast_source, cast_target);
	}
      }
    }
  }

  public class ShieldSpell extends Spell {
    ShieldSpell() {
      init("Shield", "P", R.drawable.shield, 0);
    }
    public void cast(int source, int target) {
      Log.i("SpellCast", "TODO: Shield");
      stub_finish();
    }
  }

  public class StabSpell extends Spell {
    StabSpell() {
      init("Stab", "K", R.drawable.stab, 1);
    }
    public void cast(int source, int target) {
      switch(state) {
	case 0:
	  arena.animate_move(source, target);
	  return;
	case 1:
	  arena.animate_move_damage(target, 1);
	  return;
	case 2:
	  is_finished = true;
	  arena.animate_move_back();
	  return;
      }
    }
  }

  public class MissileSpell extends Spell {
    MissileSpell() {
      init("Missile", "SD", R.drawable.missile, 1);
    }
    public void cast(int source, int target) {
      switch(state) {
	case 0:
	  arena.animate_bullet(source, target);
	  return;
	case 1:
	  is_finished = true;
	  arena.animate_damage(target, 1);
	  return;
      }
    }
  }

  public class CauseLightWoundsSpell extends Spell {
    CauseLightWoundsSpell() {
      init("Cause Light Wounds", "WFP", R.drawable.wound, 1);
    }
    public void cast(int source, int target) {
      Log.i("SpellCast", "TODO: Cure Light");
      stub_finish();
    }
  }

  public class Being {
    public Being(int posx, int posy, int bitmapid) {
      x = posx;
      y = posy;
      bitmap = BitmapFactory.decodeResource(getResources(), bitmapid);
      status = 0;
      shield = 0;
    }
    Bitmap bitmap;
    int x, y;
    int life;
    int max_life;
    int status;
    int target;
    int shield;
    int w, h;
    int midw, midh;
  }
}

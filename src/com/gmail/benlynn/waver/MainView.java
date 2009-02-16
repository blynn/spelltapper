// TODO: Log, spellbook, character sheet.
// Title menu, save state
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
import android.widget.TextView;
import android.os.Handler;
import android.os.Message;

public class MainView extends View {
  static Paint paint, selpaint;
  static Paint boxpaint;
  static Paint status_paint;
  static String msg;
  static float x0, y0, x1, y1;
  static Tutorial tut;
  static int main_state;
  static final int STATE_SPEECH = 128;
  static final int STATE_GESTURE_ONLY = 129;
  static final int STATE_NORMAL = 0;
  static final int STATE_BUSY = 1;
  static final int STATE_LDRAG = 2;
  static final int STATE_RDRAG = 3;
  static String gestname[];
  static int choice[];  // Gesture choice.
  static int lastchoice[];
  static int hist[][], histi, histstart[];
  static final int ylower = 128 + 144 + 4 * 4;
  static final int ystatus = ylower + 32 + 2 * 50 + 16 - 4;
  static final int yicon = 64 + 48 + 2 * 4;
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
  static TextView speech_box;
  void set_arena(Arena a) {
    arena = a;
    arena.being_list = being_list;
    arena.being_list_count = being_list_count;
  }
  void set_arrow_view(ArrowView a) {
    arrow_view = a;
  }
  void set_speech_box(TextView a) {
    speech_box = a;
    tut.run();
  }

  abstract class Tutorial {
    abstract void run();
  }

  class KnifeTutorial extends Tutorial {
    KnifeTutorial() {
      put_gest("Knife", 0, -1);
      state = 0;
      count = 0;
    }
    void run() {
      for(;;) switch(state) {
	case 0:
	  arena.setVisibility(View.GONE);
	  arrow_view.setVisibility(View.GONE);
	  speech_box.setVisibility(View.VISIBLE);
	  speech_box.setText(R.string.howtoknife);
	  main_state = STATE_SPEECH;
	  state = 1;
	  return;
	case 1:
	  speech_box.setText(R.string.howtoknife2);
	  main_state = STATE_SPEECH;
	  state = 2;
	  return;
	case 2:
	  speech_box.setVisibility(View.GONE);
	  clear_choices();
	  invalidate();
	  main_state = STATE_GESTURE_ONLY;
	  state = 3;
	  return;
	case 3:
	  speech_box.setVisibility(View.VISIBLE);
	  if (choice[0] == KNIFE || choice[1] == KNIFE) {
	    count++;
	    switch(count) {
	    case 3:
	      speech_box.setText(R.string.howtoknifepass3);
	      tut = new NoTutorial();
	      break;
	    case 2:
	      speech_box.setText(R.string.howtoknifepass2);
	      state = 2;
	      break;
	    case 1:
	      speech_box.setText(R.string.howtoknifepass1);
	      state = 2;
	      break;
	    }
	    main_state = STATE_SPEECH;
	  } else {
	    state = 1;
	    break;
	  }
	  return;
      }
    }
    int state;
    int count;
  }

  class NoTutorial extends Tutorial {
    NoTutorial() {}
    void run() {
      clear_choices();
      speech_box.setVisibility(View.GONE);
      arena.setVisibility(View.VISIBLE);
      arrow_view.setVisibility(View.VISIBLE);
      put_gest("Snap", -1, -1);
      put_gest("Knife", 0, -1);
      put_gest("Digit", 1, -1);
      put_gest("Clap", 1, 0);
      put_gest("Wave", -1, 1);
      put_gest("Palm", 0, 1);
      put_gest("Fingers", 1, 1);
      get_ready(); 
      invalidate();
      main_state = STATE_NORMAL;
    }
  }

  void clear_choices() {
    choice[1] = choice[0] = NO_GESTURE;
    lastchoice[0] = lastchoice[1] = choice[0];
    ready_spell_count[0] = ready_spell_count[1] = 0;
    spell_text[0] = spell_text[1] = "";
  }

  public MainView(Context context, AttributeSet attrs) {
    super(context, attrs);
    paint = new Paint();
    paint.setARGB(255, 255, 255, 255);
    selpaint = new Paint();
    selpaint.setARGB(255, 127, 255, 127);
    boxpaint = new Paint();
    boxpaint.setARGB(255, 63, 0, 63);
    status_paint = new Paint();
    status_paint.setARGB(255, 95, 63, 95);
    gestname = new String[9];
    choice = new int[2];
    lastchoice = new int[2];
    hist = new int[128][2];
    histi = 0;
    histstart = new int[2];
    histstart[1] = histstart[0] = 0;
    ready_spell_count = new int[2];
    ready_spell = new Spell[4][2];
    spell_choice = new int[2];
    spell_choice[0] = spell_choice[1] = 0;
    spell_text = new String[2];
    clear_choices();
    stab_spell = new StabSpell();
    spell_list = new Spell[64];
    spell_list_count = 0;
    add_spell(new ShieldSpell());
    add_spell(new MissileSpell());
    add_spell(new CauseLightWoundsSpell());

    being_list = new Being[16];
    being_list_count = 0;

    being_list_count++;
    being_list[0] = new Being("Player", 160 - 32, ylower - 64, R.drawable.wiz);
    being_list[0].w = 64;
    being_list[0].h = 64;
    being_list[0].midw = 32;
    being_list[0].midh = 32;
    being_list[0].life = 5;
    being_list[0].life_max = 5;
    being_list_count++;
    being_list[1] = new Being("The Dummy", 160 - 32, 0, R.drawable.dummy);
    being_list[1].w = 64;
    being_list[1].h = 64;
    being_list[1].midw = 32;
    being_list[1].midh = 32;
    being_list[1].life = 3;
    being_list[1].life_max = 3;

    spell_target = new int[2];
    exec_queue = new SpellCast[16];

    tut = new KnifeTutorial();
    msg = "";
  }

  public void get_ready() {
    main_state = STATE_NORMAL;
    print("Tap this line to confirm moves.");
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

    // Arena class handles avatars and status line.

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

    // Status line highlight.
    canvas.drawRect(0, ystatus, 320, 480, status_paint);

    // Gesture and spell text.
    y = ylower + 16 - 4;
    s = gestname[choice[0]];
    if (null == s) s = ""; //"(nothing)";
    else s += " (" + s.charAt(0) + ")";
    canvas.drawText("Left Hand: " + s, 0, y, paint);
    s = gestname[choice[1]];
    if (null == s) s = ""; //"(nothing)";
    else s += " (" + s.charAt(0) + ")";
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
	Log.i("M", "D");
	if (STATE_BUSY == main_state) return false;
	if (STATE_SPEECH == main_state) return true;
	x0 = event.getX();
	y0 = event.getY();
	if (y0 < ylower) {
	  if (STATE_GESTURE_ONLY == main_state) {
	    tut.run();
	    return false;
	  }
	  // Check for spell retargeting drag.
	  if (y0 >= yicon && y0 < yicon + 48) {
	    if (x0 < 48) {
	      main_state = 2;
	      return true;
	    } else if (x0 >= 320 - 48) {
	      main_state = 3;
	      return true;
	    }
	  }
	  return false;
	}
	okstate = y0 > ystatus;
	return true;
      case MotionEvent.ACTION_UP:
	Log.i("M", "U");
	if (STATE_BUSY == main_state) return false;
	if (STATE_SPEECH == main_state) {
	  tut.run();
	  return true;
	}
	x1 = event.getX();
	y1 = event.getY();
	if (2 == main_state || 3 == main_state) {
	  for(int i = 0; i < being_list_count; i++) {
	    Being b = being_list[i];
	    if (x1 >= b.x && y1 >= b.y && x1 < b.x + 64 && y1 < b.y + 64) {
	      spell_target[main_state - 2] = i;
	      arrow_view.invalidate();
	      return true;
	    }
	  }
	  spell_target[main_state - 2] = -1;
	  arrow_view.invalidate();
	  return true;
	}
	float dx = x1 - x0;
	float dy = y1 - y0;
	if (dx * dx + dy * dy < 32 * 32) {
	  if (STATE_GESTURE_ONLY == main_state) {
	    tut.run();
	    return true;
	  }
	  if (okstate && y1 > ystatus) {
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
	  if (STATE_GESTURE_ONLY == main_state) tut.run();
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
    main_state = STATE_BUSY;
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

    ready_spell_count[0] = 0;
    ready_spell_count[1] = 0;
    choice[0] = NO_GESTURE;
    choice[1] = NO_GESTURE;
    spell_text[0] = "";
    spell_text[1] = "";

    exec_cursor = 0;
    // TODO: Print message and delay if there are no spells.
    // Or maybe flash the screen and make a sound unconditionally to get
    // attention; in multiplayer, there can be a delay while waiting for
    // opponent.
    next_spell();
  }

  public void print(String s) {
    msg = s;
  }

  static int exec_cursor;
  public void next_spell() {
    if (exec_cursor < exec_queue_count) {
      SpellCast sc = exec_queue[exec_cursor];
      String s = "";
      String srcname = being_list[sc.source].name;
      String tgtname = null;
      if (sc.target != -1) {
	tgtname = being_list[sc.target].name;
      }
      if (sc.spell == stab_spell) {
	if (0 == sc.source) {
	  s += "You stab ";
	} else {
	  s += tgtname + " stabs ";
	}
      } else {
	if (0 == sc.source) {
	  s += "You cast ";
	} else {
	  s += tgtname + " casts ";
	}
	s += sc.spell.name + " on ";
      }
      if (0 == sc.target) {
	if (0 == sc.source) {
	  s += "yourself.";
	} else {
	  s += "you.";
	}
      } else if (-1 == sc.target) {
	s += "thin air!";
      } else {
	s += tgtname + ".";
      }
      print(s);
      sc.spell.execute(sc.source, sc.target);
      exec_cursor++;
    } else {
      get_ready();
      invalidate();
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
                          // Or call finish_spell() [it's slower].
    int cast_source, cast_target;

    public void execute(int init_source, int init_target) {
      state = 0;
      is_finished = false;
      arena.set_notify_me(done_handler);
      cast_source = init_source;
      cast_target = init_target;
      cast(cast_source, cast_target);
    }

    public void finish_spell() {
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
      switch(state) {
	case 0:
	  arena.animate_shield(target);
	  return;
	case 1:
	  if (target != -1) {
	    Being b = being_list[target];
	    if (0 == b.shield) b.shield = 1;
	  }
	  finish_spell();
	  return;
      }
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
      switch(state) {
	case 0:
	  arena.animate_spell(target, bitmap);
	  return;
	case 1:
	  is_finished = true;
	  if (-1 != target) {
	    hurt(target, 2);
	    print("Cause Light Wounds deals 2 damage.");
	  }
	  arena.animate_damage(target, 2);
	  return;
      }
    }
  }

  void hurt(int target, int amount) {
    Being b = being_list[target];
    b.life -= amount;
  }

  public class Being {
    public Being(String init_name, int posx, int posy, int bitmapid) {
      x = posx;
      y = posy;
      bitmap = BitmapFactory.decodeResource(getResources(), bitmapid);
      status = 0;
      shield = 0;
      name = init_name;
    }
    Bitmap bitmap;
    String name;
    int x, y;
    int life;
    int life_max;;
    int status;
    int target;
    int shield;
    int w, h;
    int midw, midh;
  }
}

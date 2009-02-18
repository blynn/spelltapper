// TODO: Log, spellbook, character sheet.
// Title menu, save state, victory/defeat screen with stats.
// Don't retarget if player taps on already-selected ready spell.
// Resize event.
package com.gmail.benlynn.spelltap;

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

  // If 0 or 1, represents left or right spell icon, otherwise represents
  // controlled monster.
  static int drag_i;

  static boolean is_animating;
  static BeingPosition being_pos[];

  static final int STATE_NORMAL = 0;
  static final int STATE_BUSY = 1;
  // Special states for tutorials.
  static final int STATE_SPEECH = 128;
  static final int STATE_GESTURE_ONLY = 129;
  static final int STATE_ON_END_ROUND = 130;

  static String gestname[];
  static int choice[];  // Gesture choice.
  static int lastchoice[];
  static History hist, opphist;
  static final int ylower = 128 + 144 + 4 * 4;
  static final int ystatus = ylower + 32 + 2 * 50 + 16 - 4;
  static final int yicon = 64 + 48 + 2 * 4;
  static final int GESTURE_SNAP = flattenxy(-1, -1);
  static final int GESTURE_KNIFE = flattenxy(0, -1);
  static final int GESTURE_DIGIT = flattenxy(1, -1);
  static final int GESTURE_WAVE = flattenxy(-1, 1);
  static final int GESTURE_PALM = flattenxy(0, 1);
  static final int GESTURE_FINGERS = flattenxy(1, 1);
  static final int GESTURE_NONE = flattenxy(0, 0);
  static String spell_text[];
  static int ready_spell_count[];
  static Spell[][] ready_spell;
  static Spell[] spell_list;
  static int spell_list_count;
  static int spell_choice[];
  static int spell_target[];
  static int being_list_count;
  static Being being_list[];
  static int winner;
  static MonsterAttack monatt[];

  static Arena arena;
  static ArrowView arrow_view;
  static TextView speech_box;
  static View speech_layout;
  void set_arena(Arena a) {
    arena = a;
  }
  void set_arrow_view(ArrowView a) {
    arrow_view = a;
  }
  void set_speech_box(TextView a) {
    speech_box = a;
  }

  static SpellTapMove oppmove;
  class SpellTapMove {
    SpellTapMove() {
      gest = new int[2];
      spell = new int[2];
      spell_target = new int[2];
      //monster_target = new int[16];
    }
    int gest[];
    int spell[];
    int spell_target[];
    //int monster_target[];
  }

  void jack_says(int string_constant) {
    speech_layout.setVisibility(View.VISIBLE);
    speech_box.setText(string_constant);
    main_state = STATE_SPEECH;
  }

  void jack_shutup(int new_state) {
    speech_layout.setVisibility(View.GONE);
    main_state = new_state;
  }

  abstract class Tutorial {
    abstract void run();
    void AI_move(SpellTapMove turn) {
      for(int h = 0; h < 2; h++) {
	turn.gest[h] = GESTURE_NONE;
	turn.spell[h] = -1;
	turn.spell_target[h] = -1;
      }
    }
  }

  // To pass this tutorial, the player merely has to drag their finger up three
  // times, starting from the lower part of the screen.
  class KnifeTutorial extends Tutorial {
    KnifeTutorial() {
      state = 0;
      count = 0;
    }
    void run() {
      for(;;) switch(state) {
	case 0:
	  put_gest("Knife", 0, -1);
	  stab_spell.learned = true;
	  arena.setVisibility(View.GONE);
	  arrow_view.setVisibility(View.GONE);
	  jack_says(R.string.welcome);
	  state = 100;
	  return;
	case 100:
	  jack_says(R.string.howtoknife);
	  state = 1;
	  return;
	case 1:
	  jack_says(R.string.howtoknife2);
	  state = 2;
	  return;
	case 2:
	  clear_choices();
	  invalidate();
	  jack_shutup(STATE_GESTURE_ONLY);
	  state = 3;
	  return;
	case 3:
	  if (choice[0] == GESTURE_KNIFE || choice[1] == GESTURE_KNIFE) {
	    count++;
	    switch(count) {
	    case 3:
	      jack_says(R.string.howtoknifepass3);
	      state = 4;
	      break;
	    case 2:
	      jack_says(R.string.howtoknifepass2);
	      state = 2;
	      break;
	    case 1:
	      jack_says(R.string.howtoknifepass1);
	      state = 2;
	      break;
	    }
	  } else {
	    state = 1;
	    break;
	  }
	  return;
	case 4:
	  jack_shutup(STATE_NORMAL);
	  tut = new DummyTutorial();
	  spelltap.unlock_place(SpellTap.PLACE_DOJO);
	  spelltap.goto_town();
	  return;
      }
    }
    int state;
    int count;
  }

  // Defeat a pacifist wooden dummy with 3 hitpoints to pass this one.
  // Actually, as long as the battle ends, the player passes. The only way
  // to lose is to stab yourself, which requires a player who knows what
  // they're doing.
  class DummyTutorial extends Tutorial {
    DummyTutorial() {
      state = 0;
    }
    void run() {
      for(;;) switch(state) {
      case 0:
	put_gest("Knife", 0, -1);
	stab_spell.learned = true;
	clear_choices();
	jack_says(R.string.dummytut);
	arena.setVisibility(View.VISIBLE);
	arrow_view.setVisibility(View.VISIBLE);
	invalidate();
	state = 2;
        return;
      case 2:
	jack_shutup(STATE_NORMAL);
	get_ready();
	state = 3;
        return;
      case 3:
	switch(winner) {
	case 0:
	  jack_says(R.string.dummytutwin);
	  tut = new TargetTutorial();
	  break;
	case 1:
	  jack_says(R.string.dummytutlose);
	  tut = new PalmTutorial();
	  break;
	case 2:
	  jack_says(R.string.dummytutdraw);
	  tut = new PalmTutorial();
	  break;
	}
        return;
      }
    }
    int state;
  }

  // Now we're talking! Two goblins and a dummy. Since the dummy is the
  // default target, the player is forced to retarget their stabs if they are
  // to win.
  class TargetTutorial extends Tutorial {
    TargetTutorial() {
      state = 0;
    }
    void run() {
      for(;;) switch(state) {
      case 0:
	put_gest("Knife", 0, -1);
        // Resurrect and restore HP
	being_list[0].start_life(5);
	being_list[1].start_life(3);
	// Two goblins.
	being_list[2] = new Being("Porsap", R.drawable.goblin, 1);
	being_list[2].start_life(1);
	being_list[2].target = 0;

	being_list[3] = new Being("Dedmeet", R.drawable.goblin, 1);
	being_list[3].start_life(1);
	being_list[3].target = 0;
	being_list_count = 4;

	clear_choices();
	jack_says(R.string.targettut);
	arena.setVisibility(View.VISIBLE);
	arrow_view.setVisibility(View.VISIBLE);
	state = 1;
	invalidate();
        return;
      case 1:
	jack_shutup(STATE_NORMAL);
        get_ready();
	state = 2;
        return;
      case 2:
	switch(winner) {
	case 0:
	  jack_says(R.string.targettutwin);
	  tut = new PalmTutorial();
	  break;
	case 1:
	  jack_says(R.string.targettutlose);
	  state = 0;
	  break;
	case 2:
	  jack_says(R.string.targettutlose);
	  tut = new PalmTutorial();
	  break;
	}
	reset_being_pos();
        return;
      }
    }
    int state;
  }

  // The Palm version of the Knife tutorial
  // times, starting from the lower part of the screen.
  class PalmTutorial extends Tutorial {
    PalmTutorial() {
      state = 0;
      count = 0;
    }
    void run() {
      for(;;) switch(state) {
	case 0:
	  put_gest("Knife", 0, -1);
	  put_gest("Palm", 0, 1);
	  stab_spell.learned = true;
	  arena.setVisibility(View.GONE);
	  arrow_view.setVisibility(View.GONE);
	  jack_says(R.string.palmtut);
	  state = 1;
	  return;
	case 1:
	  clear_choices();
	  jack_shutup(STATE_GESTURE_ONLY);
	  invalidate();
	  state = 2;
	  return;
	case 2:
	  if (choice[0] == GESTURE_PALM || choice[1] == GESTURE_PALM) {
	    count++;
	    switch(count) {
	    case 3:
	      jack_says(R.string.palmtutpass3);
	      tut = new ShieldTutorial();
	      break;
	    case 2:
	      jack_says(R.string.palmtutpass2);
	      state = 1;
	      break;
	    case 1:
	      jack_says(R.string.palmtutpass1);
	      state = 1;
	      break;
	    }
	  } else {
	    state = 0;
	    break;
	  }
	  return;
      }
    }
    int state;
    int count;
  }

  // The opponent just stabs every turn, but he also has 5 hitpoints.
  class ShieldTutorial extends Tutorial {
    ShieldTutorial() {
      state = 0;
      hand = 0;
    }
    void AI_move(SpellTapMove turn) {
      super.AI_move(turn);
      turn.gest[hand] = GESTURE_KNIFE;
      turn.spell[hand] = 64;
      turn.spell_target[hand] = 0;
      hand = 1 - hand;
    }
    void run() {
      for(;;) switch(state) {
      case 0:
	put_gest("Knife", 0, -1);
	put_gest("Palm", 0, 1);
        // Resurrect and restore HP
	being_list[0].start_life(5);
	// A new challenger.
	being_list[1].setup("Sendin", R.drawable.clown, 5);
	being_list_count = 2;
	hist.reset();
	opphist.reset();
	jack_says(R.string.shieldtut);
	clear_choices();
	arena.setVisibility(View.VISIBLE);
	arrow_view.setVisibility(View.VISIBLE);
	invalidate();
	state = 1;
	return;
      case 1:
	jack_says(R.string.shieldtut2);
	state = 100;
	return;
      case 100:
	jack_says(R.string.shieldtut3);
	state = 2;
	return;
      case 2:
	jack_shutup(STATE_NORMAL);
        get_ready();
	invalidate();
	state = 3;
        return;
      case 3:
	switch(winner) {
	case 0:
	  jack_says(R.string.shieldtutwin);
	  tut = new SDTutorial();
	  break;
	case 1:
	  jack_says(R.string.shieldtutlose);
	  state = 0;
	  break;
	case 2:
	  jack_says(R.string.shieldtutdraw);
	  state = 0;
	  break;
	}
        return;
      }
    }
    int state;
    int hand;
  }

  // Introduce S D gestures.
  class SDTutorial extends Tutorial {
    SDTutorial() {
      state = 0;
      hand = 0;
    }
    void run() {
      for(;;) switch(state) {
	case 0:
	  put_gest("Snap", -1, -1);
	  put_gest("Knife", 0, -1);
	  put_gest("Palm", 0, 1);
	  being_list[0].start_life(5);
	  being_list[1].setup("The Dummy", R.drawable.dummy, 5);
	  being_list_count = 2;
	  hist.reset();
	  opphist.reset();
	  arena.setVisibility(View.VISIBLE);
	  arrow_view.setVisibility(View.VISIBLE);
	  jack_says(R.string.SDtut);
	  state = 1;
	  return;
	case 1:
	  clear_choices();
	  get_ready();
	  jack_shutup(STATE_ON_END_ROUND);
	  state = 2;
	  invalidate();
	  return;
	case 2:
	  if (hist.gest[0][0] == GESTURE_SNAP &&
	      hist.gest[0][1] == GESTURE_SNAP) {
	    jack_says(R.string.SDtutpass1);
	    state = 3;
	  } else {
	    state = 0;
	    break;
	  }
	  return;
	case 3:
	  put_gest("Digit", 1, -1);
	  get_ready();
	  jack_shutup(STATE_ON_END_ROUND);
	  state = 4;
	  invalidate();
	  return;
	case 4:
	  if (hist.gest[1][0] == GESTURE_DIGIT &&
	      hist.gest[1][1] == GESTURE_DIGIT) {
	    jack_says(R.string.SDtutpass2);
	    tut = new WFPTutorial();
	  } else {
	    state = 0;
	    break;
	  }
	  return;
      }
    }
    int hand;
    int state;
  }
 
  // Introduces W F P: this spells requires multiple turns, and also
  // conflicts with another spell.
  class WFPTutorial extends Tutorial {
    WFPTutorial() {
      state = 0;
      hand = 0;
    }
    void run() {
      for(;;) switch(state) {
	case 0:
	  put_gest("Wave", -1, 1);
	  put_gest("Snap", -1, -1);
	  put_gest("Knife", 0, -1);
	  put_gest("Digit", 1, -1);
	  put_gest("Palm", 0, 1);
	  being_list[0].start_life(5);
	  being_list[1].start_life(5);
	  being_list[1].get_hurt(2);
	  hist.reset();
	  opphist.reset();
	  being_list_count = 2;
	  arena.setVisibility(View.VISIBLE);
	  arrow_view.setVisibility(View.VISIBLE);
	  jack_says(R.string.wavetut);
	  state = 1;
	  return;
	case 1:
	  clear_choices();
	  get_ready();
	  jack_shutup(STATE_ON_END_ROUND);
	  state = 2;
	  invalidate();
	  return;
	case 2:
	  if (hist.gest[0][0] == GESTURE_WAVE &&
	      hist.gest[0][1] == GESTURE_WAVE) {
	    jack_says(R.string.fingerstut);
	    state = 3;
	  } else {
	    state = 0;
	    break;
	  }
	  return;
	case 3:
	  put_gest("Fingers", 1, 1);
	  get_ready();
	  jack_shutup(STATE_ON_END_ROUND);
	  state = 4;
	  invalidate();
	  return;
	case 4:
	  if (hist.gest[1][0] == GESTURE_FINGERS &&
	      hist.gest[1][1] == GESTURE_FINGERS) {
	    jack_says(R.string.fingerstutpass1);
	    state = 5;
	  } else {
	    state = 0;
	    break;
	  }
	  return;
	case 5:
	  jack_says(R.string.fingerstutpass2);
	  state = 6;
	  return;
	case 6:
	  jack_says(R.string.fingerstutpass3);
	  state = 7;
	  return;
	case 7:
	  get_ready();
	  jack_shutup(STATE_ON_END_ROUND);
	  invalidate();
	  state = 8;
	  return;
	case 8:
	  if (hist.gest[2][0] == GESTURE_PALM &&
	      hist.gest[2][1] == GESTURE_PALM && 0 == winner) {
	    jack_says(R.string.fingerstutpass4);
	    tut = new PKFightTutorial();
	  } else {
	    jack_says(R.string.fingerstutfail);
	    state = 0;
	  }
	  return;
      }
    }
    int hand;
    int state;
  }

  int indexOfSpell(String name) {
    if (name == "Stab") return 64;
    for(int i = 0; i < spell_list_count; i++) {
      if (name == spell_list[i].name) return i;
    }
    return -1;
  }

  // Defeat an opponent gesturing P and K every turn.
  class PKFightTutorial extends Tutorial {
    PKFightTutorial() {
      state = 0;
      hand = 0;
      put_gest("Knife", 0, -1);
      put_gest("Wave", -1, 1);
      put_gest("Palm", 0, 1);
      put_gest("Fingers", 1, 1);
    }
    void AI_move(SpellTapMove turn) {
      turn.gest[hand] = GESTURE_KNIFE;
      turn.spell[hand] = 64;
      turn.spell_target[hand] = 0;
      hand = 1 - hand;
      turn.gest[hand] = GESTURE_PALM;
      turn.spell[hand] = indexOfSpell("Shield");
      turn.spell_target[hand] = 1;
    }
    void run() {
      for(;;) switch(state) {
      case 0:
        // Resurrect and restore HP
	being_list[0].start_life(5);
	being_list[1].setup("Sendin", R.drawable.clown, 5);
	being_list_count = 2;
	hist.reset();
	opphist.reset();

	jack_says(R.string.PKfighttut);
	clear_choices();
	arena.setVisibility(View.VISIBLE);
	arrow_view.setVisibility(View.VISIBLE);
	invalidate();
	state = 1;
	return;
      case 1:
	jack_says(R.string.PKfighttut2);
	state = 2;
	return;
      case 2:
	jack_shutup(STATE_NORMAL);
        get_ready();
	state = 3;
	return;
      case 3:
	switch(winner) {
	case 0:
	  jack_says(R.string.PKfighttutwin);
	  tut = new NoTutorial();
	  break;
	case 1:
	  jack_says(R.string.PKfighttutlose);
	  state = 0;
	  break;
	case 2:
	  jack_says(R.string.PKfighttutdraw);
	  state = 0;
	  break;
	}
	return;
      }
    }
    int hand;
    int state;
  }

  class NoTutorial extends Tutorial {
    NoTutorial() {}
    void run() {
      clear_choices();
      being_list[0].start_life(5);
      being_list[1].start_life(5);
      being_list_count = 2;
      jack_shutup(STATE_NORMAL);
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
    }
  }

  void clear_choices() {
    choice[1] = choice[0] = GESTURE_NONE;
    lastchoice[0] = lastchoice[1] = choice[0];
    ready_spell_count[0] = ready_spell_count[1] = 0;
    spell_text[0] = spell_text[1] = "";
  }

  class History {
    History() {
      gest = new int[128][2];
      start = new int[2];
      reset();
    }
    void reset() {
      cur = 0;
      start[0] = start[1] = 0;
    }
    boolean is_doubleP() {
      if (cur == 0) Log.e("History", "is_doubleP called with no history");
      return gest[cur - 1][0] == GESTURE_PALM && gest[cur - 1][1] == GESTURE_PALM;
    }
    void add(int g[]) {
      gest[cur][0] = g[0];
      gest[cur][1] = g[1];
      // Stabs and null gestures break combos.
      if (cur > 0) {
	if (gest[cur - 1][0] == GESTURE_KNIFE) start[0] = cur;
	if (gest[cur - 1][1] == GESTURE_KNIFE) start[1] = cur;
      }
      if (g[0] == GESTURE_KNIFE) start[0] = cur;
      if (g[1] == GESTURE_KNIFE) start[1] = cur;
      cur++;
      if (g[0] == GESTURE_NONE) start[0] = cur;
      if (g[1] == GESTURE_NONE) start[1] = cur;
      // No spell needs more than 7 turns.
      if (cur > start[0] + 6) start[0]++;
      if (cur > start[1] + 6) start[1]++;
    }
    int[][] gest;
    int cur;
    int[] start;
  }

  public MainView(Context context, AttributeSet attrs) {
    super(context, attrs);
    drag_i = -1;
    is_animating = false;
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
    hist = new History();
    opphist = new History();
    ready_spell_count = new int[2];
    ready_spell = new Spell[4][2];
    spell_choice = new int[2];
    spell_choice[0] = spell_choice[1] = 0;
    spell_text = new String[2];
    clear_choices();
    stab_spell = new StabSpell();
    stab_spell.index = 64;
    spell_list = new Spell[64];
    spell_list_count = 0;
    add_spell(new ShieldSpell());
    add_spell(new MissileSpell());
    add_spell(new CauseLightWoundsSpell());
    add_spell(new ConfusionSpell());
    add_spell(new SummonGoblinSpell());
    add_spell(new CureLightWoundsSpell());

    being_list = new Being[16];

    init_being_pos();

    being_list[0] = new Being("Player", R.drawable.wiz, -1);
    being_list[0].start_life(5);
    being_list[1] = new Being("The Dummy", R.drawable.dummy, -2);
    being_list[1].start_life(3);
    being_list_count = 2;

    spell_target = new int[2];
    exec_queue = new SpellCast[16];

    monatt = new MonsterAttack[5];
    for (int i = 1; i <= 4; i++) {
      monatt[i] = new MonsterAttack(i);
    }

    tut = new KnifeTutorial();
    //tut = new NoTutorial();
    msg = "";
    bmcorpse = BitmapFactory.decodeResource(getResources(), R.drawable.corpse);
    oppmove = new SpellTapMove();
    gameover = false;
  }

  public void get_ready() {
    print("Tap this line to confirm moves.");
  }

  public void add_spell(Spell sp) {
    spell_list[spell_list_count] = sp;
    sp.index = spell_list_count;
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

    // Opponent history.
    y = 16 - 4;
    x = 0;
    String s = "";
    for (int i = opphist.start[0]; i < opphist.cur; i++) {
      s += " " + gestname[opphist.gest[i][0]].charAt(0);
    }
    canvas.drawText(s, x, y, paint);
    s = "";
    for (int i = opphist.start[1]; i < opphist.cur; i++) {
      s += " " + gestname[opphist.gest[i][1]].charAt(0);
    }
    x = 160 + 32;
    canvas.drawText(s, x, y, paint);

    // Player history.
    y = ylower - 4;
    x = 0;
    s = "";
    for (int i = hist.start[0]; i < hist.cur; i++) {
      s += " " + gestname[hist.gest[i][0]].charAt(0);
    }
    canvas.drawText(s, x, y, paint);
    s = "";
    for (int i = hist.start[1]; i < hist.cur; i++) {
      s += " " + gestname[hist.gest[i][1]].charAt(0);
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
	}
      }
      x = 320 - 48 - 1;
    }
    for (int i = 2; i < being_list_count; i++) {
      Being b = being_list[i];
      if (!b.dead && 0 == b.controller && -1 != b.target) {
	Being b2 = being_list[b.target];
	arrow_view.add_arrow(b.x + b.midw, b.y + b.midh, b2.x + b2.midw, b2.y + b2.midh);
      }
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
	if (h == 0) x += 50;
	else x -= 50;
      }
      x = 320 - 50;
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
	Log.i("M", "D " + main_state);
	if (gameover) return true;
	if (is_animating) return false;
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
	      drag_i = 0;
	      return true;
	    } else if (x0 >= 320 - 48) {
	      drag_i = 1;
	      return true;
	    }
	  } else {
	    // Check for monster retargeting drag.
	    for (int i = 2; i < being_list_count; i++) {
	      Being b = being_list[i];
	      // It might not matter if you can retarget a corpse's
	      // attack, but it doesn't seem useful.
	      if (b.dead || 0 != b.controller) continue;
	      if (b.contains(x0, y0)) {
		Log.i("Drag", "" + i);
		drag_i = i;
		return true;
	      }
	    }
	  }
	  return false;
	}
	okstate = y0 > ystatus;
	return true;
      case MotionEvent.ACTION_UP:
	Log.i("M", "U " + main_state);
	if (gameover) {
	  gameover = false;
	  tut.run();
	  return true;
	}
	if (is_animating) return false;
	if (STATE_SPEECH == main_state) {
	  tut.run();
	  return true;
	}
	x1 = event.getX();
	y1 = event.getY();
	if (drag_i != -1) {
	  int target;
	  for(target = being_list_count - 1; target >= 0; target--) {
	    Being b = being_list[target];
	    if (b.contains(x1, y1)) break;
	  }
	  if (drag_i <= 1) {
	    spell_target[drag_i] = target;
	  } else {
	    Being b = being_list[drag_i];
	    b.target = target;
	  }
	  drag_i = -1;
	  invalidate();
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
		i = (320 - (int) x1) / 50;
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
	  if (null == gestname[choice[h]]) choice[h] = GESTURE_NONE;
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
    is_animating = true;
    arrow_view.setVisibility(View.GONE);
    hist.add(choice);

    tut.AI_move(oppmove);
    opphist.add(oppmove.gest);

    // TODO: Sort spells by priority.
    exec_queue_count = 0;
    for (int h = 0; h < 2; h++) {
      if (0 == ready_spell_count[h]) continue;
      if (-1 == spell_choice[h]) continue;
      SpellCast sc = new SpellCast(
	  ready_spell[spell_choice[h]][h], 0, spell_target[h]);
      exec_queue[exec_queue_count] = sc;
      exec_queue_count++;
    }
    for (int h = 0; h < 2; h++) {
      if (-1 == oppmove.spell[h]) continue;
      Spell sp = oppmove.spell[h] == 64 ? stab_spell : spell_list[oppmove.spell[h]];
      SpellCast sc = new SpellCast(sp, 1, oppmove.spell_target[h]);
      exec_queue[exec_queue_count] = sc;
      exec_queue_count++;
    }
    for (int i = 2; i < being_list_count; i++) {
      Being b = being_list[i];
      if (b.dead) continue;
      //if (-1 != b.target) {
	SpellCast sc = new SpellCast(monatt[b.life_max], i, b.target);
	exec_queue[exec_queue_count] = sc;
	exec_queue_count++;
      //}
    }

    clear_choices();

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
    Log.i("Spell", "" + exec_cursor + "/" + exec_queue_count);
    if (exec_cursor < exec_queue_count) {
      SpellCast sc = exec_queue[exec_cursor];
    Log.i("Spell", sc.spell.name + " " + sc.source);
      String s = "";
      String srcname = being_list[sc.source].name;
      String tgtname = null;
      if (sc.target != -1) {
	tgtname = being_list[sc.target].name;
      }
      if (sc.source >= 2) {
	s += srcname + " attacks ";
      } else if (sc.spell == stab_spell) {
	if (0 == sc.source) {
	  s += "You stab ";
	} else {
	  s += srcname + " stabs ";
	}
      } else {
	if (0 == sc.source) {
	  s += "You cast ";
	} else {
	  s += srcname + " casts ";
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
      Log.i("MV", s);
      sc.spell.execute(sc.source, sc.target);
      exec_cursor++;
    } else {
      end_round();
    }
  }

  static boolean gameover;
  // End of round. Check for death, shield expiration, etc.
  void end_round() {
    for(int i = being_list_count - 1; i >= 0; i--) {
      Being b = being_list[i];
      if (b.shield > 0) b.shield--;
      // TODO: Shield off animation.
      if (b.life <= 0) {
	if (i >= 2) b.bitmap = bmcorpse;
	b.die();
      }
    }

    is_animating = false;
    arrow_view.setVisibility(View.VISIBLE);
    winner = -1;
    if (being_list[1].dead) {
      gameover = true;
      winner = 0;
      print("You win!");
      if (being_list[0].dead) {
	winner = 2;
	print("You both die. It's a draw!");
      }
    } else if (being_list[0].dead) {
      winner = 1;
      gameover = true;
      print("You lose...");
    }

    if (!gameover) {
      if (hist.is_doubleP()) {
	if (opphist.is_doubleP()) {
	  winner = 2;
	  gameover = true;
	  print("You both surrender.");
	} else {
	  winner = 1;
	  gameover = true;
	  print("You surrender...");
	}
      } else if (opphist.is_doubleP()) {
	winner = 0;
	gameover = true;
	print("Your opponent surrenders. You win!");
      }
    }

    invalidate();

    if (gameover) {
      // Call tut.run() later once the player has tapped through the
      // victory screen.
    } else if (STATE_ON_END_ROUND == main_state) {
      tut.run();
    } else {
      get_ready();
    }
  }

  private void handle_new_choice(int h) {
    ready_spell_count[h] = 0;
    if (choice[h] == GESTURE_KNIFE) {
      if (choice[1 - h] == GESTURE_KNIFE) {
	spell_text[h] = "(only one knife)";
      } else {
	add_ready_spell(h, stab_spell);
	spell_text[h] = "";
      }
    } else {
      if (lastchoice[h] == GESTURE_KNIFE && choice[1 - h] == GESTURE_KNIFE) {
	ready_spell_count[1 - h] = 0;
	add_ready_spell(1 - h, stab_spell);
	choose_spell(1 - h, 0);
      }
      spell_text[h] = "";
      if (choice[h] != GESTURE_NONE) {
	for (int i = 0; i < spell_list_count; i++) {
	  String g = spell_list[i].gesture;
	  int k = g.length();
	  if (k > hist.cur - hist.start[h] + 1) continue;
	  k--;
	  if (g.charAt(k) != gestname[choice[h]].charAt(0)) continue;
	  k--;
	  int k2 = hist.cur - 1;
	  while (k >= 0) {
	    if (g.charAt(k) != gestname[hist.gest[k2][h]].charAt(0)) {
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
      learned = false;
    }

    abstract public void cast(int init_source, int init_target);
    Bitmap bitmap;
    String name;
    String gesture;
    int index;
    int target;
    int state;
    boolean learned;
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
	  // TODO: Remove duplicated code.
	  if (target != -1) {
	    Being b = being_list[target];
	    if (0 == b.shield) {
	      b.get_hurt(1);
	      arena.animate_move_damage(target, 1);
	    } else {
	      Log.i("TODO", "block animation");
	      arena.animate_move_damage(target, 0);
	    }
	  } else {
	    arena.animate_delay();
	  }
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
	  if (target != -1) {
	    Being b = being_list[target];
	    if (0 == b.shield) {
	      b.get_hurt(1);
	      arena.animate_damage(target, 1);
	    } else {
	      Log.i("TODO", "block animation");
	      arena.animate_damage(target, 0);
	    }
	  } else {
	    arena.animate_delay();
	  }
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
	    being_list[target].get_hurt(2);
	    print("Cause Light Wounds deals 2 damage.");
	  }
	  arena.animate_damage(target, 2);
	  return;
      }
    }
  }

  public class SummonGoblinSpell extends Spell {
    SummonGoblinSpell() {
      init("Summon Goblin", "SFW", R.drawable.summon1, 0);
    }
    public void cast(int source, int target) {
      switch(state) {
	case 0:
	  is_finished = true;
	  if (-1 != target) {
	    int k = being_list[target].controller;
	    Being b = being_list[being_list_count] = new Being("Goblin", R.drawable.goblin, k);
	    being_list_count++;
	    b.start_life(1);
	    b.target = 1 - k;
	    // TODO: Fade in goblin.
	    arena.animate_spell(target, bitmap);
	  } else {
	    arena.animate_delay();
	  }
	  return;
      }
    }
  }

  public class CureLightWoundsSpell extends Spell {
    CureLightWoundsSpell() {
      // TODO: Draw an icon for this.
      init("Cure Light Wounds", "DFW", R.drawable.confusion, 0);
    }
    public void cast(int source, int target) {
      switch(state) {
	case 0:
	  is_finished = true;
	  if (-1 != target) {
	    being_list[target].heal(1);
	  }
	  arena.animate_spell(target, bitmap);
	  return;
      }
    }
  }

  public class ConfusionSpell extends Spell {
    ConfusionSpell() {
      init("Confusion", "DSF", R.drawable.confusion, 1);
    }
    public void cast(int source, int target) {
      switch(state) {
	case 0:
	  is_finished = true;
	  if (-1 != target) {
	    being_list[target].status = STATUS_CONFUSED;
	  }
	  arena.animate_spell(target, bitmap);
	  return;
      }
    }
  }

  public class MonsterAttack extends Spell {
    MonsterAttack(int n) {
      init("", "", R.drawable.goblin, 1);
      level = n;
    }
    public void cast(int source, int target) {
      switch(state) {
	case 0:
	  arena.animate_move(source, target);
	  return;
	case 1:
	  if (target != -1) {
	    Being b = being_list[target];
	    if (0 == b.shield) {
	      b.get_hurt(1);
	      arena.animate_move_damage(target, 1);
	    } else {
	      Log.i("TODO", "block animation");
	      arena.animate_move_damage(target, 0);
	    }
	  } else {
	    arena.animate_delay();
	  }
	  return;
	case 2:
	  is_finished = true;
	  arena.animate_move_back();
	  return;
      }
    }
    int level;
  }

  static final int STATUS_OK = 0;
  static final int STATUS_CONFUSED = 1;

  static Bitmap bmcorpse;
  public class Being {
    public Being(String init_name, int bitmapid, int owner) {
      switch(owner) {
	case -1:  // This being is the player.
	  y = ylower - 64;
	  index = -1;
	  controller = 0;
	  break;
	case -2:  // This being is the opponent.
	  y = 0;
	  index = -2;
	  controller = 1;
	  break;
        case 0:  // Player controls this being.
	  for (index = 0; index < 16; index++) {
	    if (null == being_pos[index].being) break;
	  }
	  controller = 0;
	  break;
        case 1:  // Player controls this being.
	  for (index = 16 - 1; index >= 0; index--) {
	    if (null == being_pos[index].being) break;
	  }
	  controller = 1;
	  break;
	default:
	  Log.e("Being", "Ctor given bad owner.");
	  break;
      }
      if (owner < 0) {
	x = 160 - 32;
	set_size_64();
      } else {
	if (index < 0 || index >= 16) Log.e("Being", "index out of range! Summon should never have been successful?");
	x = being_pos[index].x;
	y = being_pos[index].y;
	being_pos[index].being = this;
	set_size_48();
      }
      status = STATUS_OK;
      shield = 0;
      dead = false;
      setup(init_name, bitmapid, 0);
    }
    void setup(String init_name, int bitmapid, int life) {
      name = init_name;
      bitmap = BitmapFactory.decodeResource(getResources(), bitmapid);
      start_life(life);
    }
    void heal(int amount) {
      if (!dead) {
	life += amount;
	if (life > life_max) life = life_max;
	lifeline = Integer.toString(life) + "/" + Integer.toString(life_max);
      }
    }
    void get_hurt(int amount) {
      if (!dead) {
	life -= amount;
	lifeline = Integer.toString(life) + "/" + Integer.toString(life_max);
      }
    }
    void set_size_48() {
      w = h = 48;
      midw = midh = 24;
    }
    void set_size_64() {
      w = h = 64;
      midw = midh = 32;
    }
    void start_life(int n) {
      life = life_max = n;
      dead = false;
      lifeline = Integer.toString(n) + "/" + Integer.toString(n);
    }
    void die() {
      dead = true;
      lifeline = "Dead";
    }
    boolean contains(float xf, float yf) {
      int x0 = (int) xf;
      int y0 = (int) yf;
      return x0 >= x && x0 < x + w && y0 >= y && y0 < y + h;
    }

    Bitmap bitmap;
    String name;
    String lifeline;
    int index;
    int x, y;
    int life;
    int life_max;
    int status;
    int target;
    int shield;
    int w, h;
    int midw, midh;
    short controller;
    boolean dead;
  }

  class BeingPosition {
    BeingPosition(int init_x, int init_y) {
      x = init_x;
      y = init_y;
      being = null;
    }
    int x, y;
    Being being;
  }

  // Summoned creatures should appear close to their owner, hence this mess.
  void init_being_pos() {
    int x, y;
    x = 160 - 32;
    y = ylower - 64;
    being_pos = new BeingPosition[16];
    being_pos[0] = new BeingPosition(x - 48 - 10, y);
    being_pos[1] = new BeingPosition(x + 64 + 10, y);
    being_pos[2] = new BeingPosition(x - 48 - 10, y - 48 - 4);
    being_pos[3] = new BeingPosition(x + 64 + 10, y - 48 - 4);
    being_pos[4] = new BeingPosition(x - 2 * 48 - 2 * 10, y);
    being_pos[5] = new BeingPosition(x + 64 + 48 + 2 * 10, y);
    being_pos[6] = new BeingPosition(x - 2 * 48 - 2 * 10, y - 48 - 4);
    being_pos[7] = new BeingPosition(x + 64 + 48 + 2 * 10, y - 48 - 4);
    for (int i = 0; i < 8; i++) {
      being_pos[8 + i] = new BeingPosition(
	  being_pos[8 - i - 1].x, ylower - 64 - being_pos[8 - i - 1].y + 16);
    }
  }
  void reset_being_pos() {
    for (int i = 0; i < 16; i++) being_pos[i].being = null;
  }

  static SpellTap spelltap;
}

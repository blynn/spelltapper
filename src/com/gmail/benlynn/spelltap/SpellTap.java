// TODO: Perhaps I'm better off separating the graphics from the state machines.
// i.e. hide all Views first, and each state machine chooses which ones to
// display. Like I'm already doing with MainView.
package com.gmail.benlynn.spelltap;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Button;

import android.util.Log;
public class SpellTap extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // No title bar.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.main);

    narrator = findViewById(R.id.narrator);
    narratortext = (TextView) findViewById(R.id.narratortext);
    spellbook = (TextView) findViewById(R.id.spellbook);
    spellbook.setVisibility(View.GONE);
    spellbookv = findViewById(R.id.spellbookscroll);
    spellbookv.setVisibility(View.GONE);
    mainview = (MainView) findViewById(R.id.mainview);
    mainview.spelltap = this;
    mainview.set_arena((Arena) findViewById(R.id.arena));
    mainview.set_arrow_view((ArrowView) findViewById(R.id.arrow_view));
    speech_layout = findViewById(R.id.speech_layout);
    speech_layout.setVisibility(View.GONE);
    speech_box = (TextView) findViewById(R.id.speech_box);

    mainframe = findViewById(R.id.mainframe);
    mainframe.setVisibility(View.GONE);

    townview = (TownView) findViewById(R.id.townview);
    townview.set_spelltap(this);

    mach = new SpellTapMachine[PLACE_COUNT];
    mach[PLACE_SCHOOL] = school = new School(this);
    mach[PLACE_DOJO] = dojo = new Dojo(this);
    mach[PLACE_PIT] = pit = new Pit(this);
    for (int i = 0; i < PLACE_COUNT; i++) {
      if (null == mach[i]) Log.e("SpellTap", "null mach remains");
    }
    hog = (InputHog) findViewById(R.id.inputhog);
    hog.spelltap = this;
    butv = findViewById(R.id.buttonhog);
    butclo = (Button) findViewById(R.id.button_close);
    butv.setVisibility(View.GONE);

    curmach = townview.stmach;
    curmach.run();
    state = 0;
    init_gesture_state_knowledge();
    next_state();
  }

  static final int MENU_SPELLBOOK = 1;
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(0, MENU_SPELLBOOK, 0, R.string.menu_spells);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case MENU_SPELLBOOK:
	compute_spellbook();
	spellbook.setVisibility(View.VISIBLE);
	spellbookv.setVisibility(View.VISIBLE);
	butv.setVisibility(View.VISIBLE);
	butclo.setOnClickListener(new SpellBookCloser());
	hog.blockInput();
	return true;
    }
    return false;
  }

  static class Wisdom {
    static public final int STAB = 0;
    static public final int STABNSHIELD = 1;
    static public final int UP_TO_MISSILE = 2;
    static public final int UP_TO_WFP = 3;
    static public final int UP_TO_DSF = 4;
    static public final int UP_TO_DFW = 5;
  }

  void set_spell_knowledge(int i) {
    mainview.set_spell_knowledge(i);
  }
  void set_gesture_knowledge(int i) {
    mainview.set_gesture_knowledge(i);
  }

  // TODO: There must be a way to do literal arrays in Java?
  static int gesture_state[] = new int[8];
  static int gesture_knowledge[] = new int[8];
  static int gsk_count;
  static void add_gsk(int s, int k) {
    gesture_state[gsk_count] = s;
    gesture_knowledge[gsk_count] = k;
    gsk_count++;
  }
  // TODO: Argh! Copy and past was fastest way.
  static final int GK_NONE = -1;
  static final int GK_KNIFE_ONLY = 0;
  static final int GK_KNIFE_AND_PALM = 1;
  static final int GK_KPS = 2;
  static final int GK_DKPS = 3;
  static final int GK_ALL_BUT_FC = 4;
  static final int GK_ALL_BUT_C = 5;
  static final int GK_ALL = 6;
  static void init_gesture_state_knowledge() {
    gsk_count = 0;
    add_gsk(0, GK_NONE);
    add_gsk(1, GK_KNIFE_ONLY);
    add_gsk(3, GK_KNIFE_AND_PALM);
    add_gsk(5, GK_DKPS);
    add_gsk(7, GK_ALL_BUT_C);
  }

  void next_state() {
    // Fragile code. Take care!
    mainframe.setVisibility(View.GONE);
    if (state > 0) {
      unlock_place(SpellTap.PLACE_DOJO);
    }
    if (state > 2) {
      unlock_place(SpellTap.PLACE_PIT);
    }
    int i;
    for (i = gsk_count - 1; gesture_state[i] > state; i--);
    set_gesture_knowledge(gesture_knowledge[i]);

    switch(state) {
    case 0:  // N00b. Can only go to Academy to get schooled.
      school.set_state_noob();
      state = 1;
      break;
    case 1:  // Jack waits in the Training Hall for the first lesson.
      set_spell_knowledge(Wisdom.STAB);
      dojo.set_state_firstlesson();
      school.set_state_jackwaits();
      state = 2;
      break;
    case 2:  // Knows Stab. Can train on dummy, or learn Palm in Academy.
      set_spell_knowledge(Wisdom.STAB);
      dojo.set_state_dummy(3);
      school.set_state_palmlesson();
      state = 3;
      break;
    case 3:  // Knows Palm, Shield. Arena is open.
      set_spell_knowledge(Wisdom.STABNSHIELD);
      pit.set_state_stabatha();
      school.set_state_firstadvice();
      state = 4;
      break;
    case 4:  // One win. Time to learn SD.
      set_spell_knowledge(Wisdom.STABNSHIELD);
      pit.set_state_closed();
      school.set_state_jackwaits();
      dojo.set_state_missilelesson();
      state = 5;
      break;
    case 5:  // See Jack to learn WFP.
      set_spell_knowledge(Wisdom.UP_TO_MISSILE);
      pit.set_state_closed();
      school.set_state_wfplesson();
      dojo.set_state_dummy(4);
      state = 6;
      break;
    case 6:  // Learn WFP.
      set_spell_knowledge(Wisdom.UP_TO_MISSILE);
      pit.set_state_closed();
      school.set_state_jackwaits();
      dojo.set_state_wfplesson();
      state = 7;
      break;
    case 7:  // See Jack to learn DSF.
      set_spell_knowledge(Wisdom.UP_TO_WFP);
      pit.set_state_closed();
      school.set_state_dsflesson();
      dojo.set_state_dummy(5);
      state = 8;
      break;
    case 8:  // Second duel.
      set_spell_knowledge(Wisdom.UP_TO_DSF);
      pit.set_state_duel2();
      school.set_state_duel2advice();
      dojo.set_state_dummy(5);
      state = 9;
      break;
    case 9:  // Learn Cure Light Wounds.
      set_spell_knowledge(Wisdom.UP_TO_DSF);
      pit.set_state_closed();
      school.set_state_curelesson();
      state = 10;
      break;
    case 10:  // Duel 3.
      set_spell_knowledge(Wisdom.UP_TO_DFW);
      pit.set_state_duel3();
      school.set_state_duel3advice();
      state = 11;
      break;
    }
  }

  void set_place(int i) {
    curmach = mach[i];
    curmach.run();
  }
  void goto_town() {
    curmach = townview.stmach;
    curmach.run();
    mainframe.setVisibility(View.GONE);
    townview.setVisibility(View.VISIBLE);
  }
  void goto_mainframe() {
    curmach = null;
    mainview.run();
    mainframe.setVisibility(View.VISIBLE);
  }
  void unlock_place(int place) {
    townview.unlock(place);
  }

  void jack_says(int string_constant) {
    speech_layout.setVisibility(View.VISIBLE);
    speech_box.setText(string_constant);
    hog.setVisibility(View.VISIBLE);
  }
  void narrate(int string_constant) {
    narratortext.setText(string_constant);
    narrator.setVisibility(View.VISIBLE);
    hog.setVisibility(View.VISIBLE);
  }

  void hogup() {
    speech_layout.setVisibility(View.GONE);
    narrator.setVisibility(View.GONE);
    hog.setVisibility(View.GONE);
    if (null == curmach) mainview.run();
    else curmach.run();
  }

  void compute_spellbook() {
    int count = 0;
    String s = "";
    for (int i = 0; i < 9; i++) {
      MainView.Gesture g = MainView.gesture[i];
      if (g == null || !g.learned) continue;
      count++;
      s += g.statusname + ": " + g.arrow + "\n";
    }
    if (0 == count) {
      spellbook.setText(getText(R.string.emptyspellbook));
      return;
    }
    spellbook.setText(getText(R.string.heading_gestures));
    spellbook.append(s);
    if (count > 2) {
      spellbook.append(getText(R.string.ins_righthand));
    }
    spellbook.append("\n");
    spellbook.append(getText(R.string.heading_spells));

    for (int i = 0; i < MainView.spell_list_count; i++) {
      MainView.Spell sp = MainView.spell_list[i];
      if (sp.learned) {
	spellbook.append(sp.gesture + " " + sp.name + ": ");
	spellbook.append(getText(sp.description));
	spellbook.append("\n");
      }
    }
  }

  class SpellBookCloser implements View.OnClickListener {
    SpellBookCloser() {}
    public void onClick(View v) {
      SpellTap.this.close_spellbook();
    }
  }

  void close_spellbook() {
    spellbook.setVisibility(View.GONE);
    butv.setVisibility(View.GONE);
    spellbookv.setVisibility(View.GONE);
    hog.unblockInput();
  }

  static MainView mainview;
  static View mainframe;
  static View narrator;
  static TextView narratortext;
  static final int PLACE_SCHOOL = 0;
  static final int PLACE_DOJO = 1;
  static final int PLACE_PIT = 2;
  static final int PLACE_COUNT = 3;
  static TownView townview;
  static InputHog hog;
  static int state;
  static View speech_layout;
  static TextView speech_box;
  static SpellTapMachine[] mach;
  static SpellTapMachine curmach;
  static Pit pit;
  static Dojo dojo;
  static School school;
  static TextView spellbook;
  static View spellbookv;
  static Button butclo;
  static View butv;
}

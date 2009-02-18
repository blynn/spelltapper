// TODO: Perhaps I'm better off separating the graphics from the state machines.
// i.e. hide all Views first, and each state machine chooses which ones to
// display. Like I'm already doing with MainView.
package com.gmail.benlynn.spelltap;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.widget.TextView;

import android.util.Log;
public class SpellTap extends Activity {
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // No title bar.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.main);

    narrator = findViewById(R.id.narrator);
    narratortext = (TextView) findViewById(R.id.narratortext);

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

    curmach = townview.stmach;
    curmach.run();
    state = 0;
    next_state();
  }

  void next_state() {
    mainframe.setVisibility(View.GONE);
    switch(state) {
    case 0:  // N00b. Can only go to Academy to get schooled.
      school.set_state_noob();
      state = 1;
      break;
    case 1:  // Jack waits in the Training Hall for the first lesson.
      unlock_place(SpellTap.PLACE_DOJO);
      dojo.set_state_firstlesson();
      school.set_state_jackwaits();
      state = 2;
      break;
    case 2:  // Knows Stab. Can train on dummy, or learn Palm in Academy.
      mainview.dummyhp = 3;
      dojo.set_state_dummy();
      school.set_state_palmlesson();
      state = 3;
      break;
    case 3:  // Knows Palm, Shield. Arena is open.
      unlock_place(SpellTap.PLACE_PIT);
      pit.set_state_stabatha();
      school.set_state_firstadvice();
      state = 4;
      break;
    case 4:  // One win. Time to learn SD.
      pit.set_state_closed();
      school.set_state_jackwaits();
      dojo.set_state_missilelesson();
      state = 5;
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
}

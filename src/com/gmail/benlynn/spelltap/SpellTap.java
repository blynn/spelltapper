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
    townview.spelltap = this;

    schoolview = (SchoolView) findViewById(R.id.schoolview);
    schoolview.setVisibility(View.GONE);
    schoolview.spelltap = this;

    dojoview = (DojoView) findViewById(R.id.dojoview);
    dojoview.setVisibility(View.GONE);
    dojoview.spelltap = this;

    pit = new Pit(this);

    townview.machine.run();
    curview = townview;
    state = 0;
    next_state();
  }

  void next_state() {
    switch(state) {
    case 0:  // N00b. Can only go to Academy to get schooled.
      schoolview.set_state_noob();
      state = 1;
      break;
    case 1:  // Jack waits in the Training Hall for the first lesson.
      unlock_place(SpellTap.PLACE_DOJO);
      dojoview.set_state_firstlesson();
      schoolview.set_state_jackwaits();
      state = 2;
      break;
    case 2:  // Knows Stab. Can train on dummy, or learn Palm in Academy.
      mainview.dummyhp = 3;
      dojoview.set_state_dummy();
      schoolview.set_state_palmlesson();
      state = 3;
      break;
    case 3:  // Knows Palm, Shield. Arena is open.
      unlock_place(SpellTap.PLACE_PIT);
      pit.set_state_stabatha();
      schoolview.set_state_firstadvice();
      state = 4;
      break;
    }
  }

  void narrate(int string_constant) {
    narrator.setVisibility(View.GONE);
    narrator.setVisibility(View.VISIBLE);
    narratortext.setText(string_constant);
  }
  void narrate_off() {
    narrator.setVisibility(View.GONE);
  }

  // TODO: Fade screen for these transitions.
  // TODO: Remove duplicated code.
  void goto_school() {
    if (null != curview) curview.setVisibility(View.GONE);
    curview = schoolview;
    schoolview.run();
    curview.setVisibility(View.VISIBLE);
  }
  void goto_dojo() {
    if (null != curview) curview.setVisibility(View.GONE);
    curview = dojoview;
    dojoview.run();
    curview.setVisibility(View.VISIBLE);
  }
  void goto_pit() {
    curview.setVisibility(View.GONE);
    curview = null;
    pit.run();
  }
  void goto_town() {
    if (null != curview) curview.setVisibility(View.GONE);
    curview = townview;
    townview.machine.run();
    curview.setVisibility(View.VISIBLE);
  }
  void goto_mainframe() {
    if (null != curview) curview.setVisibility(View.GONE);
    curview = mainframe;
    mainview.run();
    curview.setVisibility(View.VISIBLE);
  }
  void unlock_place(int place) {
    townview.unlock(place);
  }

  void jack_says(int string_constant) {
    speech_layout.setVisibility(View.VISIBLE);
    speech_box.setText(string_constant);
  }
  void jack_shutup() {
    speech_layout.setVisibility(View.GONE);
  }

  static MainView mainview;
  static View curview;
  static View mainframe;
  static View narrator;
  static TextView narratortext;
  static final int PLACE_SCHOOL = 0;
  static final int PLACE_DOJO = 1;
  static final int PLACE_PIT = 2;
  static final int PLACE_COUNT = 3;
  static TownView townview;
  static SchoolView schoolview;
  static DojoView dojoview;
  static int state;
  static View speech_layout;
  static TextView speech_box;
  static Pit pit;
}

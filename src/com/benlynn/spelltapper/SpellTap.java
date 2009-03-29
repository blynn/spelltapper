package com.benlynn.spelltapper;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.view.KeyEvent;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import android.util.Log;

public class SpellTap extends Activity {
  @Override
  public void onCreate(Bundle bun) {
    super.onCreate(bun);
    //Log.i("SpellTap", "onCreate");
    // No title bar.
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    Easel.init();
    setContentView(R.layout.main);
    booklayout = findViewById(R.id.booklayout);
    booklayout.setVisibility(View.GONE);
    bookview = (BookView) findViewById(R.id.bookview);
    bookview.tv = (TextView) findViewById(R.id.bookdesc);
    ((Button) findViewById(R.id.book_prev)).setOnClickListener(new BookPrev());
    ((Button) findViewById(R.id.book_next)).setOnClickListener(new BookNext());
    ((Button) findViewById(R.id.book_close)).setOnClickListener(new BookClose());
    mainview = (MainView) findViewById(R.id.mainview);
    mainview.set_spelltap(this);
    mainview.set_board((Board) findViewById(R.id.board));
    mainview.set_arrow_view((ArrowView) findViewById(R.id.arrow_view));
    mainview.emptyleftmsg = getText(R.string.empty_left_hand).toString();
    mainview.emptyrightmsg = getText(R.string.empty_right_hand).toString();
    speech_layout = findViewById(R.id.speech_layout);
    speech_layout.setVisibility(View.GONE);
    speech_box = (TextView) findViewById(R.id.speech_box);
    img_speaker = (ImageView) findViewById(R.id.img_speaker);
    name_speaker = (TextView) findViewById(R.id.name_speaker);
    portrait = findViewById(R.id.portrait);

    about_text = findViewById(R.id.about_text);
    about_text.setVisibility(View.GONE);

    mainframe = findViewById(R.id.mainframe);
    mainframe.setVisibility(View.GONE);

    townview = (TownView) findViewById(R.id.townview);
    townview.set_spelltap(this);
    townview.setVisibility(View.GONE);

    mach = new SpellTapMachine[PLACE_COUNT];
    mach[PLACE_SCHOOL] = school = new School(this);
    mach[PLACE_DOJO] = dojo = new Dojo(this);
    mach[PLACE_PIT] = pit = new Pit(this);
    mach[PLACE_NET] = tubes = new Tubes(this);
    mach[PLACE_TOWN] = townview.stmach;
    mach[PLACE_MAIN] = mainview.stmach;
    for (int i = 0; i < PLACE_COUNT; i++) {
      if (null == mach[i]) Log.e("SpellTap", "null mach remains");
    }
    hog = (InputHog) findViewById(R.id.inputhog);
    hog.setVisibility(View.GONE);
    hog.spelltap = this;

    netconfig = findViewById(R.id.netconfig_layout);
    netconfig.setVisibility(View.GONE);
    Tubes.init((Button) findViewById(R.id.netconfig_ok),
	(Button) findViewById(R.id.netconfig_cancel),
	(EditText) findViewById(R.id.netconfig_server),
	(EditText) findViewById(R.id.netconfig_port));

    init_gesture_state_knowledge();
    spellbook_is_open = false;
    Player.init();

    if (null != bun) {
      state = bun.getInt(ICE_STATE);
      MainView.load_bundle(bun);
      Tubes.load_bundle(bun);
      run();
      set_place(bun.getInt(ICE_PLACE));
      townview.setVisibility(bun.getInt(ICE_VIS_TOWN));
      mainframe.setVisibility(bun.getInt(ICE_VIS_MAIN));
      speech_layout.setVisibility(bun.getInt(ICE_VIS_SPEECH));
      hog.setVisibility(bun.getInt(ICE_VIS_HOG));
      booklayout.setVisibility(bun.getInt(ICE_VIS_BOOKVIEW));
      netconfig.setVisibility(bun.getInt(ICE_VIS_NETCONFIG));
    } else {
      try {
	FileInputStream in = mainview.con.openFileInput("save");
	if (0 == in.read()) {
	  state = in.read();
	}
	in.close();
      } catch(Exception e) {
	// Leave state at 0.
      }
      // Start in town.
      townview.setVisibility(View.VISIBLE);
      run();
      set_place(PLACE_TOWN);
    }
    senseman = (SensorManager) getSystemService(SENSOR_SERVICE);
    tilt_listener = new TiltListener();
  }

  SensorManager senseman;
  TiltListener tilt_listener;

  class TiltListener implements SensorListener {
    TiltListener() {
      hist = new float[8];
      first = true;
    }
    public void onAccuracyChanged(int sensor, int accuracy) {
      //Log.i("TiltListener", "Accuracy change: " + Float.toString(accuracy));
    }

    public void onSensorChanged(int sensor, float[] values) {
      if (first) {
        last_t =  SystemClock.uptimeMillis();
	for (i = 0; i < 8; i++) hist[i] = values[1];
	i = 0;
	first = false;
      } else {
	// Log.i("Tilt", Float.toString(values[1]));
	long t = SystemClock.uptimeMillis();
	// Log.i("Tilt", Long.toString(t));
	if (t < 128) return;
	last_t = t;

	for (int j = 0 == i ? 8 - 1 : i - 1;
	     j != i;
	     j = 0 == j ? 8 - 1 : j - 1) {
	  float delta = values[1] - hist[j];
	  if (delta < -180) delta += 360;
	  else if (delta > 180) delta -= 360;
	  if (delta < -25) {
	    tilt_up();
	    break;
	  } else if (delta > 25) {
	    tilt_down();
	    break;
	  }
	}
	hist[i] = values[1];
	if (8 - 1 == i) i = 0;
	else i++;
      }
    }
    float hist[];
    long last_t;
    int i;
    boolean first;
  }

  void tilt_up() {
    // Log.i("Tilt", "Up");
    if (PLACE_MAIN == curplace) mainview.tilt_up();
  }

  void tilt_down() {
    // Log.i("Tilt", "Down");
    if (PLACE_MAIN == curplace) mainview.tilt_down();
  }

  @Override
  protected void onResume() {
    super.onResume();
    Tubes.is_abandoned = false;
    if (null != Tubes.net_thread) {
      //Log.i("SpellTap", "Restaring nethread");
      Tubes.net_thread.run();
    }
    senseman.registerListener(tilt_listener,
	SensorManager.SENSOR_ORIENTATION |
	SensorManager.SENSOR_DELAY_GAME);
    /*
      SensorManager.SENSOR_ORIENTATION
      SensorManager.SENSOR_ACCELEROMETER
      SensorManager.SENSOR_MAGNETIC_FIELD
      SensorManager.SENSOR_DELAY_FASTEST
    */
  }

  @Override
  protected void onStop() {
    senseman.unregisterListener(tilt_listener);
    super.onStop();
  }

  static final String ICE_STATE = "game-state";
  static final String ICE_PLACE = "game-place";
  static final String ICE_VIS_MAIN = "game-vis-main";
  static final String ICE_VIS_TOWN = "game-vis-town";
  static final String ICE_VIS_BOOKVIEW = "game-vis-booklayout";
  static final String ICE_VIS_SPEECH = "game-vis-speech";
  static final String ICE_VIS_HOG = "game-vis-hog";
  static final String ICE_VIS_NETCONFIG = "game-vis-netconfig";

  @Override
  public void onSaveInstanceState(Bundle bun) {
    bun.putInt(ICE_STATE, state);
    bun.putInt(ICE_PLACE, curplace);
    bun.putInt(ICE_VIS_MAIN, mainframe.getVisibility());
    bun.putInt(ICE_VIS_TOWN, townview.getVisibility());
    bun.putInt(ICE_VIS_BOOKVIEW, booklayout.getVisibility());
    bun.putInt(ICE_VIS_SPEECH, speech_layout.getVisibility());
    // TODO: Save speech contents too?
    bun.putInt(ICE_VIS_HOG, hog.getVisibility());
    bun.putInt(ICE_VIS_NETCONFIG, netconfig.getVisibility());
    Tubes.save_bundle(bun);
    MainView.save_bundle(bun);
    //Log.i("SpellTap", "Saving " + state);
  }

  @Override
  public void onPause() {
    //Log.i("SpellTap", "Pause");
    Tubes.is_abandoned = true;
    super.onPause();
  }

  static final int MENU_SPELLBOOK = 1;
  static final int MENU_ABOUT = 2;
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(Menu.NONE, MENU_SPELLBOOK, Menu.NONE, R.string.menu_spells);
    menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.menu_about);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case MENU_SPELLBOOK:
        BookView.compute_spells();
	open_spellbook();
	return true;
      case MENU_ABOUT:
	open_about();
	return true;
    }
    return false;
  }

  void warp(int n) {
    state = n;
    run();
    curmach.run();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch(keyCode) {
      case KeyEvent.KEYCODE_BACK:
	if (View.VISIBLE == hog.getVisibility()) {
	  hogoff();
	  return true;
	}
	if (spellbook_is_open) {
	  close_spellbook();
	  return true;
	}
	if (is_in_town()) {
	  return super.onKeyDown(keyCode, event);
	}
	curmach.go_back();
	return true;
      case KeyEvent.KEYCODE_A:
	warp(0);
	return true;
      case KeyEvent.KEYCODE_B:
	warp(14);
	return true;
      case KeyEvent.KEYCODE_C:
	warp(16);
	return true;
      case KeyEvent.KEYCODE_D:
	warp(128);
	return true;
      case KeyEvent.KEYCODE_1:
	warp(3);
	return true;
      case KeyEvent.KEYCODE_2:
	warp(8);
	return true;
      case KeyEvent.KEYCODE_3:
	warp(10);
	return true;
      case KeyEvent.KEYCODE_4:
	warp(12);
	return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  static class Wisdom {
    static public final int STAB = 0;
    static public final int STABNSHIELD = 1;
    static public final int UP_TO_MISSILE = 2;
    static public final int UP_TO_WFP = 3;
    static public final int UP_TO_DSF = 4;
    static public final int UP_TO_DFW = 5;
    static final int ALL_LEVEL_0 = 8;
    static final int ALL_LEVEL_1 = 9;
    static final int ALL_LEVEL_2 = 10;
    static final int ALL_LEVEL_3 = 11;
    static final int ALL_LEVEL_4 = 12;
    static final int ALL_LEVEL_5 = 13;
    static final int ALL_SPELLS = ALL_LEVEL_5;

    static final int NONE = -1;
    static final int KNIFE_ONLY = 0;
    static final int KNIFE_AND_PALM = 1;
    static final int KPS = 2;
    static final int DKPS = 3;
    static final int ALL_BUT_FC = 4;
    static final int ALL_BUT_C = 5;
    static final int ALL_GESTURES = 6;
  }

  void set_spell_knowledge(int i) {
    mainview.set_spell_knowledge(i);
  }
  void set_gesture_knowledge(int i) {
    mainview.set_gesture_knowledge(i);
  }

  static int gesture_state[] = new int[8];
  static int gesture_knowledge[] = new int[8];
  static int gsk_count;
  static void add_gsk(int s, int k) {
    gesture_state[gsk_count] = s;
    gesture_knowledge[gsk_count] = k;
    gsk_count++;
  }
  static void init_gesture_state_knowledge() {
    gsk_count = 0;
    add_gsk(0, Wisdom.NONE);
    add_gsk(1, Wisdom.KNIFE_ONLY);
    add_gsk(3, Wisdom.KNIFE_AND_PALM);
    add_gsk(5, Wisdom.DKPS);
    add_gsk(7, Wisdom.ALL_BUT_C);
    add_gsk(15, Wisdom.ALL_GESTURES);
  }

  void next_state() {
    if (state < 17) {
      state++;
      try {
	FileOutputStream os = mainview.con.openFileOutput("save", 0);
	os.write(0);  // Save version number.
	os.write(state);
	os.close();
      } catch (Exception e) {
	Log.e("SpellTapper", "Proceeding without saving state.");
      }
    }
    run();
    switch(state) {
      case 15:
	ben_says(R.string.limit1);
	break;
      case 16:
	ben_says(R.string.limit2);
	break;
      case 17:
	ben_says(R.string.limit3);
	break;
    }
  }

  void run() {
    // Fragile code. Take care!
    if (state > 13) {
      allow_confirm_one = false;
    } else {
      allow_confirm_one = true;
    }
    if (state > 0) {
      unlock_place(SpellTap.PLACE_DOJO);
      townview.set_state_normal();
    } else {
      townview.set_state_first();
    }
    if (state > 2) {
      unlock_place(SpellTap.PLACE_PIT);
    }
    if (state > 13) {
      unlock_place(SpellTap.PLACE_NET);
    }
    if (state < 14) {
      Player.level = 0;
    } else if (state < 15) {
      Player.level = 1;
    } else if (state < 16) {
      Player.level = 2;
    } else if (state < 17) {
      Player.level = 3;
    } else {
      Player.level = 5;
    }
    set_spell_knowledge(Wisdom.ALL_LEVEL_0 + Player.level);
    if (Player.level < 2) {
      mainview.has_circles = false;
    } else {
      mainview.has_circles = true;
    }

    int i;
    for (i = gsk_count - 1; gesture_state[i] > state; i--);
    set_gesture_knowledge(gesture_knowledge[i]);

    switch(state) {
    case 0:  // N00b. Can only go to Academy to get schooled.
      school.set_state_noob();
      break;
    case 1:  // Jack waits in the Training Hall for the first lesson.
      set_spell_knowledge(Wisdom.STAB);
      dojo.set_state_firstlesson();
      school.set_state_jackwaits();
      break;
    case 2:  // Knows Stab. Can train on dummy, or learn Palm in Academy.
      set_spell_knowledge(Wisdom.STAB);
      dojo.set_state_dummy(3);
      school.set_state_palmlesson();
      break;
    case 3:  // Knows Palm, Shield. Arena is open.
      set_spell_knowledge(Wisdom.STABNSHIELD);
      pit.set_state_duel(Agent.getStabatha());
      school.set_state_firstadvice();
      break;
    case 4:  // One win. Time to learn SD.
      set_spell_knowledge(Wisdom.STABNSHIELD);
      pit.set_state_closed();
      school.set_state_jackwaits();
      dojo.set_state_missilelesson();
      break;
    case 5:  // See Jack to learn WFP.
      set_spell_knowledge(Wisdom.UP_TO_MISSILE);
      pit.set_state_closed();
      school.set_state_wfplesson();
      dojo.set_state_dummy(4);
      break;
    case 6:  // Learn WFP.
      set_spell_knowledge(Wisdom.UP_TO_MISSILE);
      pit.set_state_closed();
      school.set_state_jackwaits();
      dojo.set_state_wfplesson();
      break;
    case 7:  // See Jack to learn DSF.
      set_spell_knowledge(Wisdom.UP_TO_WFP);
      pit.set_state_closed();
      school.set_state_dsflesson();
      dojo.set_state_dummy(5);
      break;
    case 8:  // Second duel.
      set_spell_knowledge(Wisdom.UP_TO_DSF);
      pit.set_state_duel(Agent.getBloodyRipper());
      school.set_state_duel2advice();
      dojo.set_state_dummy(5);
      break;
    case 9:  // Learn Cure Light Wounds.
      set_spell_knowledge(Wisdom.UP_TO_DSF);
      pit.set_state_closed();
      school.set_state_curelesson();
      dojo.set_state_dummy(5);
      break;
    case 10:  // Duel 3.
      set_spell_knowledge(Wisdom.UP_TO_DFW);
      pit.set_state_duel(Agent.getSendin());
      school.set_state_generic_advice();
      dojo.set_state_dummy(5);
      break;
    case 11:  // Learn 2 remaining Level 1 spells.
      set_spell_knowledge(Wisdom.UP_TO_DFW);
      pit.set_state_closed();
      school.set_state_lvl1lesson();
      dojo.set_state_dummy(5);
      break;
    case 12:  // Level 1 boss.
      set_spell_knowledge(Wisdom.ALL_LEVEL_1);
      pit.set_state_duel(Agent.getAlTeffor());
      school.set_state_generic_advice();
      dojo.set_state_dummy(5);
      break;
    case 13:  // Graduation.
      set_spell_knowledge(Wisdom.ALL_LEVEL_1);
      pit.set_state_closed();
      school.set_state_graduate();
      dojo.set_state_dummy(5);
      break;
    case 14:  // Net play.
      pit.set_state_exhibition(Agent.getAlTeffor());
      school.set_state_generic_advice();
      dojo.set_state_dummy(5);
      break;
    case 15:
      pit.set_state_exhibition(Agent.getAlTeffor());
      school.set_state_level2();
      dojo.set_state_dummy(12);
      break;
    case 16:
      pit.set_state_closed();
      school.set_state_level3();
      dojo.set_state_dummy(25);
      break;
    case 17:
      pit.set_state_closed();
      school.set_state_level5();
      dojo.set_state_dummy(50);
      break;
    case 128:
      set_spell_knowledge(Wisdom.ALL_SPELLS);
      set_gesture_knowledge(Wisdom.ALL_GESTURES);
      pit.set_state_closed();
      school.set_state_generic_advice();
      dojo.set_state_dummy(50);
      break;
    }
  }

  void set_place(int i) {
    curplace = i;
    curmach = mach[i];
    curmach.run();
  }
  boolean is_in_town() { return PLACE_TOWN == curplace; }
  void goto_town() {
    mainframe.setVisibility(View.GONE);
    townview.setVisibility(View.VISIBLE);
    set_place(PLACE_TOWN);
  }
  void goto_mainframe() {
    townview.setVisibility(View.GONE);
    mainframe.setVisibility(View.VISIBLE);
    set_place(PLACE_MAIN);
  }
  void unlock_place(int place) {
    townview.unlock(place);
  }

  static void tip_off() {
    speech_layout.setVisibility(View.GONE);
  }
  static void someone_tip(int img, String name, int string_constant) {
    speech_layout.setVisibility(View.VISIBLE);
    speech_box.setText(string_constant);
    img_speaker.setImageResource(img);
    name_speaker.setText(name);
    portrait.setVisibility(View.VISIBLE);
  }
  static void someone_says(int img, String name, int string_constant) {
    someone_tip(img, name, string_constant);
    hog.setVisibility(View.VISIBLE);
  }
  static void jack_tip(int string_constant) {
    someone_tip(R.drawable.jack, "One-eyed Jack", string_constant);
  }
  static void jack_says(int string_constant) {
    someone_says(R.drawable.jack, "One-eyed Jack", string_constant);
  }
  static void ben_says(int string_constant) {
    someone_says(R.drawable.trace, "Two-eyed Ben", string_constant);
  }
  static void narrate(int string_constant) {
    someone_tip(R.drawable.trace, "Bug if you can read this!", string_constant);
    portrait.setVisibility(View.INVISIBLE);
    hog.setVisibility(View.VISIBLE);
  }
  static void show_tip(int string_constant) {
    someone_tip(R.drawable.trace, "Bug if you can read this!", string_constant);
    portrait.setVisibility(View.INVISIBLE);
  }
  static void hogoff() {
    speech_layout.setVisibility(View.GONE);
    about_text.setVisibility(View.GONE);
    hog.setVisibility(View.GONE);
    curmach.run();
  }

  void open_about() {
    about_text.setVisibility(View.VISIBLE);
    hog.setVisibility(View.VISIBLE);
  }

  void open_spellbook() {
    booklayout.setVisibility(View.VISIBLE);
    spellbook_is_open = true;
  }

  class BookPrev implements View.OnClickListener {
    BookPrev() {}
    public void onClick(View v) {
      bookview.prev();
    }
  }

  class BookNext implements View.OnClickListener {
    BookNext() {}
    public void onClick(View v) {
      bookview.next();
    }
  }

  class BookClose implements View.OnClickListener {
    BookClose() {}
    public void onClick(View v) {
      SpellTap.this.close_spellbook();
    }
  }

  void close_spellbook() {
    booklayout.setVisibility(View.GONE);
    spellbook_is_open = false;
  }

  static boolean spellbook_is_open;
  static MainView mainview;
  static View netconfig;
  static View mainframe;
  static View booklayout;
  static BookView bookview;
  static final int PLACE_SCHOOL = 0;
  static final int PLACE_DOJO = 1;
  static final int PLACE_PIT = 2;
  static final int PLACE_NET = 3;
  static final int PLACE_TOWN_COUNT = 4;
  static final int PLACE_TOWN = 4;
  static final int PLACE_MAIN = 5;
  static final int PLACE_COUNT = 6;
  static TownView townview;
  static InputHog hog;
  static View speech_layout;
  static TextView speech_box;
  static SpellTapMachine[] mach;
  static SpellTapMachine curmach;
  static Pit pit;
  static Dojo dojo;
  static School school;
  static Tubes tubes;
  static int state = 0;
  static int curplace;
  static boolean allow_confirm_one;
  static ImageView img_speaker;
  static TextView name_speaker;
  static View portrait;
  static View about_text;
}

package com.gmail.benlynn.spelltap;

class School extends SpellTapMachine {
  School(SpellTap st) {
    super(st);
    strlist = new int[16];
    strcount = 0;
  }
  abstract class Machine { abstract void run(); }
  void run() { machine.run(); }

  static int strlist[];
  static int strcount;

  void set_state_noob() {
    machine = new IntroMachine();
  }
  void set_state_jackwaits() {
    machine = new JackWaitsMachine();
  }
  void set_state_palmlesson() {
    machine = new PalmMachine();
  }
  void set_state_firstadvice() {
    machine = new AdviceMachine(
      R.string.duel1advice1,
      R.string.duel1advice2);
  }
  void set_state_wfplesson() {
    machine = new LectureMachine(
      R.string.meetforwfp,
      R.string.meetforwfp2,
      R.string.meetforwfp3);
  }
  void set_state_duel2advice() {
    machine = new AdviceMachine(
      R.string.duel2advice1,
      R.string.duel2advice2);
  }
  void set_state_dsflesson() {
    machine = new LectureMachine(
      R.string.DSFteach1,
      R.string.DSFteach2,
      R.string.DSFteach3);
  }
  void set_state_curelesson() {
    machine = new LectureMachine(
      R.string.cureteach1,
      R.string.cureteach2,
      R.string.cureteach3,
      R.string.cureteach4);
  }
  void set_state_generic_advice() {
    machine = new AdviceMachine(R.string.generic_advice);
  }
  void set_state_lvl1lesson() {
    machine = new LectureMachine(
      R.string.lvl1teach1,
      R.string.lvl1teach4);
  }
  void set_state_graduate() {
    machine = new LectureMachine(
      R.string.graduate1,
      R.string.graduate2,
      R.string.graduate3,
      R.string.graduate4);
  }

  class IntroMachine extends Machine {
    IntroMachine() {}
    void run() {
      // TODO: Introduce Jack.
      spelltap.mainview.set_state_knifetutorial();
      spelltap.goto_mainframe();
    }
  }

  class JackWaitsMachine extends Machine {
    JackWaitsMachine() {
      state = 0;
    }
    void run() {
      for(;;) switch(state) {
	case 0:
	  spelltap.narrate(R.string.jackwaitsatdummy);
	  state = 1;
	  return;
	case 1:
	  state = 0;
	  spelltap.goto_town();
	  return;
      }
    }
    int state;
  }

  class PalmMachine extends Machine {
    PalmMachine() {}
    void run() {
      // TODO: Another sort of intro?
      spelltap.mainview.set_state_palmtutorial();
      spelltap.goto_mainframe();
    }
  }

  class LectureMachine extends Machine {
    LectureMachine(int m0, int m1) {
      strlist[0] = m0;
      strlist[1] = m1;
      strcount = 2;
      state = 0;
    }
    LectureMachine(int m0, int m1, int m2) {
      strlist[0] = m0;
      strlist[1] = m1;
      strlist[2] = m2;
      strcount = 3;
      state = 0;
    }
    LectureMachine(int m0, int m1, int m2, int m3) {
      strlist[0] = m0;
      strlist[1] = m1;
      strlist[2] = m2;
      strlist[3] = m3;
      strcount = 4;
      state = 0;
    }
    void run() {
      if (state < strcount) {
	spelltap.jack_says(strlist[state]);
	state++;
	return;
      }
      spelltap.next_state();
      spelltap.goto_town();
    }
    int state;
  }

  class AdviceMachine extends Machine {
    AdviceMachine(int m0) {
      strlist[0] = m0;
      strcount = 1;
      state = 0;
    }
    AdviceMachine(int m0, int m1) {
      strlist[0] = m0;
      strlist[1] = m1;
      strcount = 2;
      state = 0;
    }
    AdviceMachine(int m0, int m1, int m2) {
      strlist[0] = m0;
      strlist[1] = m1;
      strlist[2] = m2;
      strcount = 3;
      state = 0;
    }
    void run() {
      if (state < strcount) {
	spelltap.jack_says(strlist[state]);
	state++;
	return;
      }
      spelltap.goto_town();
      state = 0;
    }
    int state;
  }

  static Machine machine;
}

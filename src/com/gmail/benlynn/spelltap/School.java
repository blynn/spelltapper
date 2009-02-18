package com.gmail.benlynn.spelltap;

class School extends SpellTapMachine {
  School(SpellTap st) { super(st); }
  abstract class Machine { abstract void run(); }
  void run() { machine.run(); }

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
    machine = new FirstAdviceMachine();
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
	  spelltap.narrate_off();
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

  class FirstAdviceMachine extends Machine {
    FirstAdviceMachine() {
      state = 0;
    }
    void run() {
      for(;;) switch(state) {
	case 0:
	  spelltap.jack_says(R.string.jackfirstadvice);
	  state = 1;
	  return;
	case 1:
	  spelltap.jack_shutup();
	  state = 0;
	  spelltap.goto_town();
	  return;
      }
    }
    int state;
  }

  static Machine machine;
}

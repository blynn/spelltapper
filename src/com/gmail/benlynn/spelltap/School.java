package com.gmail.benlynn.spelltap;

class School extends SpellTapMachine {
  School(SpellTap st) {
    super(st);
    advice_list = new int[16];
    advice_count = 0;
  }
  abstract class Machine { abstract void run(); }
  void run() { machine.run(); }

  static int advice_list[];
  static int advice_count;

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
    advice_list[0] = R.string.duel1advice1;
    advice_list[1] = R.string.duel1advice2;
    advice_list[2] = R.string.duel1advice3;
    advice_count = 3;
    machine = new AdviceMachine();
  }
  void set_state_wfplesson() {
    machine = new WFPMachine();
  }
  void set_state_duel2advice() {
    advice_list[0] = R.string.duel2advice1;
    advice_list[1] = R.string.duel2advice2;
    advice_count = 2;
    machine = new AdviceMachine();
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

  class AdviceMachine extends Machine {
    AdviceMachine() {
      state = 0;
    }
    void run() {
      if (state < advice_count) {
	spelltap.jack_says(advice_list[state]);
	state++;
	return;
      }
      spelltap.goto_town();
      state = 0;
    }
    int state;
  }

  class WFPMachine extends Machine {
    WFPMachine() {
      state = 0;
    }
    void run() {
      for(;;) switch(state) {
	case 0:
	  spelltap.jack_says(R.string.meetforwfp);
	  state = 1;
	  return;
	case 1:
	  spelltap.next_state();
	  spelltap.goto_town();
	  return;
      }
    }
    int state;
  }

  static Machine machine;
}

package com.gmail.benlynn.spelltap;

class Pit extends SpellTapMachine {
  Pit(SpellTap st) { super(st); }
  abstract class Machine { abstract void run(); }
  void run() { machine.run(); }

  void set_state_stabatha() {
    machine = new StabathaMachine();
  }

  void set_state_closed() {
    machine = new ClosedMachine();
  }

  class StabathaMachine extends Machine {
    StabathaMachine() {}
    void run() {
      spelltap.mainview.set_state_stabatha();
      spelltap.goto_mainframe();
    }
  }

  class ClosedMachine extends Machine {
    ClosedMachine() {}
    void run() {
      spelltap.narrate(R.string.arenashut);
      spelltap.goto_town();
    }
  }

  static Machine machine;
}

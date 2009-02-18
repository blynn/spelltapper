package com.gmail.benlynn.spelltap;

class Dojo extends SpellTapMachine {
  Dojo(SpellTap st) { super(st); }
  abstract class Machine { abstract void run(); }

  void run() { machine.run(); }

  void set_state_firstlesson() {
    machine = new FirstMachine();
  }
  void set_state_dummy() {
    machine = new DummyMachine();
  }

  class FirstMachine extends Machine {
    FirstMachine() {}
    void run() {
      spelltap.mainview.set_state_dummytutorial();
      spelltap.goto_mainframe();
    }
  }

  class DummyMachine extends Machine {
    DummyMachine() {}
    void run() {
      spelltap.mainview.set_state_practicemode();
      spelltap.goto_mainframe();
    }
  }

  static Machine machine;
}

package com.benlynn.spelltapper;

class Dojo extends SpellTapMachine {
  Dojo(SpellTap st) { super(st); }
  abstract class Machine { abstract void run(); }

  void run() { machine.run(); }

  void set_state_firstlesson() {
    machine = new FirstMachine();
  }
  void set_state_dummy(int hp) {
    machine = new DummyMachine(hp);
  }
  void set_state_missilelesson() {
    machine = new MissileMachine();
  }
  void set_state_wfplesson() {
    machine = new WFPMachine();
  }

  class FirstMachine extends Machine {
    FirstMachine() {}
    void run() {
      spelltap.mainview.set_tutorial(MainView.MACHINE_DUMMY);
      spelltap.goto_mainframe();
    }
  }

  class DummyMachine extends Machine {
    DummyMachine(int i_hp) { hp = i_hp; }
    void run() {
      spelltap.mainview.set_state_practicemode(hp);
      spelltap.goto_mainframe();
    }
    int hp;
  }

  class MissileMachine extends Machine {
    MissileMachine() {}
    void run() {
      spelltap.mainview.set_state_missilelesson();
      spelltap.goto_mainframe();
    }
  }

  class WFPMachine extends Machine {
    WFPMachine() {}
    void run() {
      spelltap.mainview.set_state_wfplesson();
      spelltap.goto_mainframe();
    }
  }

  static Machine machine;
}

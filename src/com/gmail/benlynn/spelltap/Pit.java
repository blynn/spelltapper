package com.gmail.benlynn.spelltap;

class Pit extends SpellTapMachine {
  Pit(SpellTap st) { super(st); }
  abstract class Machine { abstract void run(); }
  void run() { machine.run(); }

  void set_state_duel(Agent a) {
    machine = new DuelMachine(a);
  }
  void set_state_closed() {
    machine = new ClosedMachine();
  }
  void set_state_exhibition(Agent a) {
    machine = new ExhibitionMachine(a);
  }

  class DuelMachine extends Machine {
    DuelMachine(Agent a) { agent = a; }
    void run() {
      spelltap.mainview.set_state_win_to_advance(agent);
      spelltap.goto_mainframe();
    }
    Agent agent;
  }
  class ClosedMachine extends Machine {
    ClosedMachine() {}
    void run() {
      spelltap.narrate(R.string.arenashut);
      spelltap.goto_town();
    }
  }
  class ExhibitionMachine extends Machine {
    ExhibitionMachine(Agent a) { agent = a; }
    void run() {
      spelltap.mainview.set_state_exhibition_match(agent);
      spelltap.goto_mainframe();
    }
    Agent agent;
  }

  static Machine machine;
}

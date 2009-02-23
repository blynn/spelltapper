package com.gmail.benlynn.spelltap;

abstract public class Agent {
  abstract String name();
  abstract String name_full();
  abstract int life();
  abstract int bitmap_id();

  void move(MainView.SpellTapMove turn) {
    for(int h = 0; h < 2; h++) {
      turn.gest[h] = Gesture.NONE;
      turn.spell[h] = -1;
      turn.spell_target[h] = -1;
    }
  }
  void set_charm(int hand, int gesture) {
  }
  int get_charm_hand() {
    return 0;
  }
  int get_charm_gesture() {
    return Gesture.PALM;
  }

  static class DummyAgent extends Agent {
    DummyAgent() {}
    String name() { return "The Dummy"; }
    String name_full() { return "Spitz The Dummy"; }
    int life() { return 5; }
    int bitmap_id() { return R.drawable.dummy; }
  }

  static class Stabatha extends Agent {
    Stabatha() {}
    String name() { return "Stabatha"; }
    String name_full() { return "Stabatha"; }
    int life() { return 5; }
    int bitmap_id() { return R.drawable.stabatha; }
  }

  static class Sendin extends Agent {
    Sendin() {}
    String name() { return "Sen Din"; }
    String name_full() { return "Sen Din the Clown"; }
    int life() { return 5; }
    int bitmap_id() { return R.drawable.clown; }
  }

  static Agent getDummy() { return new DummyAgent(); }
  static Agent getStabatha() { return new Stabatha(); }
  static Agent getSendin() { return new Sendin(); }
}

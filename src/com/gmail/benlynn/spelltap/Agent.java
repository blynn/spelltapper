package com.gmail.benlynn.spelltap;

import com.gmail.benlynn.spelltap.MainView.SpellTapMove;
import com.gmail.benlynn.spelltap.MainView.Status;

abstract public class Agent {
  abstract String name();
  abstract String name_full();
  abstract int life();
  abstract int bitmap_id();

  void move(SpellTapMove turn) {
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

  static int indexOfSpell(String name) {
    return MainView.indexOfSpell(name);
  }
  static int indexOfSpellGesture(String gesture) {
    return MainView.indexOfSpellGesture(gesture);
  }

  // Opponent with 5 hit points that stabs every turn.
  static class Stabatha extends Agent {
    Stabatha() {
      hand = 0;
    }
    void move(SpellTapMove turn) {
      super.move(turn);
      turn.gest[hand] = Gesture.KNIFE;
      turn.spell[hand] = indexOfSpell("Stab");
      turn.spell_target[hand] = 0;
      hand = 1 - hand;
    }
    String name() { return "Stabatha"; }
    String name_full() { return "Stabatha"; }
    int life() { return 5; }
    int bitmap_id() { return R.drawable.stabatha; }
    int hand;
  }

  // An opponent gesturing P and K every turn.
  static class BloodyRipper extends Agent {
    BloodyRipper() {
      hand = 0;
    }
    void move(SpellTapMove turn) {
      turn.gest[hand] = Gesture.KNIFE;
      turn.spell[hand] = indexOfSpell("Stab");
      turn.spell_target[hand] = 0;
      hand = 1 - hand;
      // If confused, gesture knife with other hand even though it's useless.
      // Beats surrendering!
      if (Status.CONFUSED == MainView.being_list[1].status) {
	turn.gest[hand] = Gesture.KNIFE;
	turn.spell[hand] = -1;
	return;
      }
      turn.gest[hand] = Gesture.PALM;
      turn.spell[hand] = indexOfSpell("Shield");
      turn.spell_target[hand] = 1;
    }
    String name() { return "Bloody Ripper"; }
    String name_full() { return "Bloody Ripper"; }
    int life() { return 5; }
    int bitmap_id() { return R.drawable.clown; }
    int hand;
  }

  // An opponent who bets the game on WFP.
  static class Sendin extends Agent {
    Sendin() {
      hand = 0;
      count = 0;
    }
    void move(SpellTapMove turn) {
      switch(count) {
	case 0:
	  turn.gest[0] = Gesture.PALM;
	  turn.spell[0] = indexOfSpellGesture("P");
	  turn.spell_target[0] = 1;
	  turn.gest[1] = Gesture.WAVE;
	  turn.spell[1] = -1;
	  turn.spell_target[1] = -1;
	  break;
	case 1:
	  turn.gest[0] = Gesture.PALM;
	  turn.spell[0] = indexOfSpellGesture("P");
	  turn.spell_target[0] = 0;
	  turn.gest[1] = Gesture.FINGERS;
	  turn.spell[1] = -1;
	  turn.spell_target[1] = -1;
	  break;
	case 2:
	  turn.gest[0] = Gesture.KNIFE;
	  turn.spell[0] = indexOfSpellGesture("K");
	  turn.spell_target[0] = 0;
	  turn.gest[1] = Gesture.PALM;
	  turn.spell[1] = indexOfSpellGesture("WFP");
	  turn.spell_target[1] = 0;
	  break;
	case 3:
	  turn.gest[0] = Gesture.WAVE;
	  turn.spell[0] = -1;
	  turn.spell_target[0] = -1;
	  turn.gest[1] = Gesture.WAVE;
	  turn.spell[1] = -1;
	  turn.spell_target[1] = -1;
	  break;
	case 4:
	  turn.gest[0] = Gesture.FINGERS;
	  turn.spell[0] = -1;
	  turn.spell_target[0] = -1;
	  turn.gest[1] = Gesture.FINGERS;
	  turn.spell[1] = -1;
	  turn.spell_target[1] = -1;
	  break;
	case 5:
	  turn.gest[0] = Gesture.PALM;
	  turn.spell[0] = indexOfSpellGesture("WFP");
	  turn.spell_target[0] = 0;
	  turn.gest[1] = Gesture.PALM;
	  turn.spell[1] = indexOfSpellGesture("WFP");
	  turn.spell_target[1] = 0;
	  break;
	  // Do or die! By now, we've lost or won.
      }
      count++;
    }
    String name() { return "Sen Din"; }
    String name_full() { return "Sen Din the Clown"; }
    int life() { return 5; }
    int bitmap_id() { return R.drawable.clown; }
    int hand;
    int count;
  }

  static Agent getDummy() { return new DummyAgent(); }
  static Agent getStabatha() { return new Stabatha(); }
  static Agent getBloodyRipper() { return new BloodyRipper(); }
  static Agent getSendin() { return new Sendin(); }
}

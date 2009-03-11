package com.gmail.benlynn.spelltap;

import java.util.Random;
import android.util.Log;

import com.gmail.benlynn.spelltap.MainView.SpellTapMove;
import com.gmail.benlynn.spelltap.Being.Status;

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
  void set_para(int target, int hand) {}
  void get_para(int target) { reply_hand = 0; }
  void set_charm(int hand, int gesture) {}
  void reset() {}
  void get_charm_hand() { reply_hand = 0; }
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
    Stabatha() {}
    void reset() { hand = 0; }
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
    BloodyRipper() {}
    void reset() { hand = 0; }
    void move(SpellTapMove turn) {
      turn.gest[hand] = Gesture.KNIFE;
      turn.spell[hand] = indexOfSpell("Stab");
      turn.spell_target[hand] = 0;
      hand = 1 - hand;
      // If confused, gesture knife with other hand even though it's useless.
      // Beats surrendering!
      if (Status.CONFUSED == Being.list[1].status) {
	turn.gest[hand] = Gesture.KNIFE;
	turn.spell[hand] = -1;
	return;
      }
      turn.gest[hand] = Gesture.PALM;
      turn.spell[hand] = indexOfSpell("Shield");
      turn.spell_target[hand] = 1;
    }
    String name() { return "Ripper"; }
    String name_full() { return "Bloody Ripper"; }
    int life() { return 5; }
    int bitmap_id() { return R.drawable.ripper; }
    int hand;
  }

  // An opponent who bets the game on WFP.
  static class Sendin extends Agent {
    Sendin() {}
    void reset() { hand = 0; count = 0; }
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
	  turn.spell_target[0] = 1;
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

  class SearchResult {
    SearchResult() {
      count = new int[2];
      spell = new String[64][2];
      progress = new int[64][2];
      remain = new int[64][2];
    }
    int count[];
    int progress[][];
    int remain[][];
    String spell[][];
  }

  // Level 1 boss.
  static class AlTeffor extends Agent {
    AlTeffor() {
      rnd = new Random(3);
      res = new SearchResult();
    }
    void reset() {
      first = true;
      shield = indexOfSpellGesture("P");
    }
    boolean flip() {
      if (rnd.nextInt(2) == 0) return false;
      return true;
    }
    int rand_start_gesture() {
      switch(rnd.nextInt(3)) {
	case 0:
	 return Gesture.WAVE;
	case 1:
	 return Gesture.DIGIT;
	case 2:
	 return Gesture.SNAP;
      }
      return -1;  // Should not reach here!
    }
    void move(SpellTapMove turn) {
      if (first) {
	first = false;
	int h = flip() ? 0 : 1;
	turn.gest[h] = Gesture.PALM;
	turn.spell[h] = shield;
	turn.spell_target[h] = 1;
	turn.gest[1 - h] = rand_start_gesture();
	turn.spell[1 - h] = -1;
	phand = h;
	return;
      }
      MainView.search_opphist1(res);
      // XXX Handle Charm Person, Confusion
      // XXX
      // If WWP has been cast, don't need P.
      if (Being.list[1].shield > 1) {
	for (int h = 0; h < 2; h++) {
	  if (0 == res.count[h]) {
	    turn.gest[h] = rand_start_gesture();
	    turn.spell[h] = -1;
	    continue;
	  }
	  // XXX
	  //int i = rnd.nextInt(res.count[h]);
	}
      }
      int h = 1 - phand;
      // Continue random spell in non-Shielding hand.
      int i = 0;
      if (0 == res.count[h]) {
	turn.gest[h] = rand_start_gesture();
      } else {
	do {
	  i = rnd.nextInt(res.count[h]);
	} while(0 == res.remain[i][h]);
	turn.gest[h] = Gesture.find(res.spell[i][h].charAt(res.progress[i][h]));
	// Don't want double P.
	if (turn.gest[h] == Gesture.PALM) {
	  turn.gest[phand] = Gesture.SNAP;
	  turn.spell[phand] = -1;
	  phand = h;
	} else {
	  turn.gest[phand] = Gesture.PALM;
	  turn.spell[phand] = shield;
	  turn.spell_target[phand] = 1;
	}
      }
      MainView.search_oppcomplete1(res, turn.gest);
      if (res.count[h] > 0) {
	i = rnd.nextInt(res.count[h]);
	// Spell completed!
	turn.spell[h] = indexOfSpellGesture(res.spell[i][h]);
	turn.spell_target[h] = 1 - MainView.spell_list[turn.spell[h]].target;
      } else {
	turn.spell[h] = -1;
      }
    }
    String name() { return "Al Teffor"; }
    String name_full() { return "Al Teffor, Destroyer of Windows"; }
    int life() { return 5; }
    int bitmap_id() { return R.drawable.alteffor; }
    Random rnd;
    SearchResult res;
    boolean first;
    int phand;
    int shield;
  }

  static Agent getDummy() { return new DummyAgent(); }
  static Agent getStabatha() { return new Stabatha(); }
  static Agent getBloodyRipper() { return new BloodyRipper(); }
  static Agent getSendin() { return new Sendin(); }
  static Agent getAlTeffor() { return new AlTeffor(); }

  int reply_hand;
}

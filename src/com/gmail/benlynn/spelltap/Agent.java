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

  static abstract class BasicAgent extends Agent {
    void basic_init() {
      rnd = new Random();
      res = new SearchResult();
    }
    void reset() {
      first = true;
      shield = indexOfSpellGesture("P");
      stab = indexOfSpellGesture("K");
      is_charmed = false;
    }
    boolean flip() {
      if (rnd.nextInt(2) == 0) return false;
      return true;
    }
    int rand_start_gesture() {
      switch(rnd.nextInt(4)) {
	case 0:
	 return Gesture.WAVE;
	case 1:
	 return Gesture.DIGIT;
	case 2:
	 return Gesture.SNAP;
	case 3:
	 return Gesture.KNIFE;
      }
      return -1;  // Should not reach here!
    }
    void set_charm(int hand, int gesture) {
      is_charmed = true;
      charm_hand = hand;
      charm_gesture = gesture;
    }
    void get_charm_hand() { reply_hand = rnd.nextInt(2); }
    int get_charm_gesture() {
      return flip() ? Gesture.PALM : Gesture.KNIFE;
    }
    void finalize_move(SpellTapMove turn) {
      MainView.search_oppcomplete1(res, turn.gest);
      for (int h = 0; h < 2; h++) {
	if (res.count[h] > 0) {  // At least one spell completed.
	  int i = rnd.nextInt(res.count[h]);
	  turn.spell[h] = indexOfSpellGesture(res.spell[i][h]);
	  turn.spell_target[h] = 1 - MainView.spell_list[turn.spell[h]].target;
	  if (turn.gest[h] == Gesture.PALM &&
	      res.spell[i][h] != "WWP" &&
	      rnd.nextInt(5) < 1) {
	    turn.spell[h] = shield;
	    turn.spell_target[h] = 1;
	  }
	} else if (turn.gest[h] == Gesture.PALM) {
	  turn.spell[h] = shield;
	  turn.spell_target[h] = 1;
	} else if (turn.gest[h] == Gesture.KNIFE) {
	  turn.spell[h] = stab;
	  turn.spell_target[h] = 0;
	} else {
	  turn.spell[h] = -1;
	}
      }
      is_charmed = false;
    }
    void move(SpellTapMove turn) {
      if (first) {
	first = false;
	int h = rnd.nextInt(2);
	turn.gest[h] = Gesture.PALM;
	turn.gest[1 - h] = rand_start_gesture();
	phand = h;
	finalize_move(turn);
	return;
      }
      MainView.search_opphist1(res);
      // If WWP has been cast, don't need P.
      if (Being.list[1].shield > 0) phand = -1;
      else if (-1 == phand) {  // WWP wore off?
	phand = rnd.nextInt(2);
      }

      // Continue random spell in non-Shielding hand(s).
      for (int h = 0; h < 2; h++) {
	if (h == phand) {
	  turn.gest[h] = Gesture.PALM;
	  continue;
	}
	int i = 0;
	if (0 == res.count[h]) {
	  turn.gest[h] = rand_start_gesture();
	} else {
	  do {
	    i = rnd.nextInt(res.count[h]);
	  } while(0 == res.remain[i][h]);
	  turn.gest[h] = Gesture.find(res.spell[i][h].charAt(res.progress[i][h]));
	}
      }

      if (Status.CONFUSED == Being.list[1].status) {
	// Respect Confusion.
	if (turn.gest[0] != Gesture.PALM) {
	  turn.gest[1] = turn.gest[0];
	} else if (turn.gest[1] != Gesture.PALM) {
	  turn.gest[0] = turn.gest[1];
	} else {
	  turn.gest[0] = turn.gest[1] = rand_start_gesture();
	}
      } else if (is_charmed) {
	// Respect Charm Person.
	turn.gest[charm_hand] = charm_gesture;
	if (Gesture.PALM == charm_gesture) {
	  if (Gesture.PALM == turn.gest[1 - charm_hand]) {
	    turn.gest[1 - charm_hand] = rand_start_gesture();
	  }
	}
      } else {
	// Don't want double P.
	if (turn.gest[0] == Gesture.PALM && turn.gest[0] == turn.gest[1]) {
	  int h = rnd.nextInt(2);
	  if (phand != -1) {
	    h = phand;
	    phand = 1 - h;
	  }
	  turn.gest[h] = rand_start_gesture();
	}
      }
      finalize_move(turn);
    }
    int life() { return Player.life[Player.level]; }
    Random rnd;
    SearchResult res;
    boolean first;
    boolean is_charmed;
    int charm_hand, charm_gesture;
    int phand;
    int shield, stab;
  }

  static class Sendin extends BasicAgent {
    Sendin() {
      basic_init();
    }
    String name() { return "Sen Din"; }
    String name_full() { return "Sen Din the Clown"; }
    int bitmap_id() { return R.drawable.clown; }
  }

  // Level 1 boss.
  static class AlTeffor extends BasicAgent {
    AlTeffor() {
      basic_init();
    }
    String name() { return "Al Teffor"; }
    String name_full() { return "Al Teffor, Destroyer of Windows"; }
    int bitmap_id() { return R.drawable.alteffor; }
  }

  static Agent getDummy() { return new DummyAgent(); }
  static Agent getStabatha() { return new Stabatha(); }
  static Agent getBloodyRipper() { return new BloodyRipper(); }
  static Agent getSendin() { return new Sendin(); }
  static Agent getAlTeffor() { return new AlTeffor(); }

  int reply_hand;
}

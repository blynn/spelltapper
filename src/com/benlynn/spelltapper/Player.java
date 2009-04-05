package com.benlynn.spelltapper;

import com.benlynn.spelltapper.SpellTap.Wisdom;

class Player {
  static void init() {
    life = new int[6];
    life[0] = 5;
    life[1] = 5;
    life[2] = 9;
    life[3] = 12;
    life[4] = 14;
    life[5] = 15;
    level = 0;
  }

  static void set_level(int i) {
    level = i;
    MainView.set_spell_knowledge(Wisdom.ALL_LEVEL_0 + Player.level);
    if (level < 2) {
      MainView.has_circles = false;
    } else {
      MainView.has_circles = true;
    }
    if (level < 3) {
      MainView.set_gesture_knowledge(Wisdom.ALL_BUT_C);
    } else {
      MainView.set_gesture_knowledge(Wisdom.ALL_GESTURES);
    }
  }

  static void set_true_level(int i) {
    true_level = i;
    set_level(i);
  }

  static String name, name_full;
  static int life[];
  static int level;
  static int true_level;
}

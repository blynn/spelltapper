package com.gmail.benlynn.spelltap;

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

  static String name, name_full;
  static int life[];
  static int level;
}

package com.gmail.benlynn.spelltap;

class Gesture {
  Gesture(String i_name, int i_x, int i_y) {
    name = i_name;
    initial = name.charAt(0);
    statusname = initial + " (" + name + ")";
    x = i_x;
    y = i_y;
    arrow = new String[2];
    arrow[0] = get_arrow(x, y);
    arrow[1] = get_arrow(-x, y);
    learned = false;
  }

  static int find(char c) {
    for (int i = 0; i < 9; i++) {
      if (null != list[i] && c == list[i].initial) return i;
    }
    return -1;
  }

  static final int flattenxy(int x, int y) {
    return (x + 1) + (y + 1) * 3;
  }

  static final int paralyze(int g) {
    if (FINGERS == g) return CLAP;
    if (SNAP == g) return DIGIT;
    if (WAVE == g) return PALM;
    return g;
  }

  static void put_gest(String s, int x, int y) {
    int n = Gesture.flattenxy(x, y);
    Gesture g = list[n] = new Gesture(s, x, y);;
  }

  static void init() {
    list = new Gesture[9];
    put_gest("Snap", -1, -1);
    put_gest("Knife", 0, -1);
    put_gest("Digit", 1, -1);
    put_gest("Clap", 1, 0);
    put_gest("Wave", -1, 1);
    put_gest("Palm", 0, 1);
    put_gest("Fingers", 1, 1);
  }

  static char abbr(int i)  {
    return list[i].initial;
  }

  static String get_arrow(int x, int y) {
    // Assign unicode arrows.
    switch(x) {
      case -1:
	switch(y) {
	  case 0:  // Left.
	    return "\u2190";
	  case -1:  // Up and left.
	    return "\u2196";
	  case 1:  // Down and left.
	    return "\u2199";
	}
      case 0:
	switch(y) {
	  case -1:  // Up.
	    return "\u2191";
	  case 1:  // Down.
	    return "\u2193";
	}
      case 1:
	switch(y) {
	  case 0:  // Right.
	    return "\u2192";
	  case -1:  // Up and right.
	    return "\u2197";
	  case 1:  // Down and right.
	    return "\u2198";
	}
    }
    return "BUG!";
  }

  String name;
  String arrow[];
  String statusname;
  char initial;
  boolean learned;
  int x, y;

  static final int SNAP = flattenxy(-1, -1);
  static final int KNIFE = flattenxy(0, -1);
  static final int DIGIT = flattenxy(1, -1);
  static final int WAVE = flattenxy(-1, 1);
  static final int PALM = flattenxy(0, 1);
  static final int FINGERS = flattenxy(1, 1);
  static final int CLAP = flattenxy(1, 0);
  static final int NONE = flattenxy(0, 0);
  static Gesture[] list;
}

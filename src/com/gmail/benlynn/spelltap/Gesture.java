package com.gmail.benlynn.spelltap;

class Gesture {
  Gesture(String i_name, int i_x, int i_y) {
    name = i_name;
    initial = name.charAt(0);
    statusname = initial + " (" + name + ")";
    x = i_x;
    y = i_y;
    // Assign unicode arrows.
    switch(x) {
      case -1:
	switch(y) {
	  case 0:  // Left.
	    arrow = "\u2190";
	  case -1:  // Up and left.
	    arrow = "\u2196";
	    break;
	  case 1:  // Down and left.
	    arrow = "\u2199";
	    break;
	}
	break;
      case 0:
	switch(y) {
	  case -1:  // Up.
	    arrow = "\u2191";
	    break;
	  case 1:  // Down.
	    arrow = "\u2193";
	    break;
	}
	break;
      case 1:
	switch(y) {
	  case 0:  // Right.
	    arrow = "\u2192";
	    break;
	  case -1:  // Up and right.
	    arrow = "\u2197";
	    break;
	  case 1:  // Down and right.
	    arrow = "\u2198";
	    break;
	}
	break;
    }
    learned = false;
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

  String name;
  String arrow;
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

package com.benlynn.spelltapper;

import android.util.Log;

class History {
  History() {
    gest = new int[128][2];
    start = new int[2];
    reset();
  }
  void fast_forward() {
    start[0] = start[1] = cur;
  }
  boolean was_fast_forwarded() {
    return start[0] == cur && start[1] == cur;
  }
  void reset() {
    cur = 0;
    start[0] = start[1] = 0;
  }
  boolean is_doubleP() {
    if (cur == 0) Log.e("History", "is_doubleP called with no history");
    return gest[cur - 1][0] == Gesture.PALM && gest[cur - 1][1] == Gesture.PALM;
  }
  int last_gesture(int h) {
    if (0 == cur) return Gesture.NONE;
    return gest[cur - 1][h];
  }
  void add(int g[]) {
    gest[cur][0] = g[0];
    gest[cur][1] = g[1];
    // Stabs and null gestures break combos.
    if (cur > 0) {
      if (gest[cur - 1][0] == Gesture.KNIFE) start[0] = cur;
      if (gest[cur - 1][1] == Gesture.KNIFE) start[1] = cur;
    }
    if (g[0] == Gesture.KNIFE) start[0] = cur;
    if (g[1] == Gesture.KNIFE) start[1] = cur;
    cur++;
    if (g[0] == Gesture.NONE) start[0] = cur;
    if (g[1] == Gesture.NONE) start[1] = cur;
    // No spell needs more than 7 turns.
    if (cur > start[0] + 6) start[0]++;
    if (cur > start[1] + 6) start[1]++;
  }
  int[][] gest;
  int cur;
  int[] start;
}

package com.gmail.benlynn.spelltap;

import android.util.Log;

abstract class SpellTapMachine {
  SpellTapMachine(SpellTap st) {
    spelltap = st;
  }
  abstract void run();
  void go_back() {
    Log.i("STM", "BACK pressed.");
  }
  static SpellTap spelltap;
}

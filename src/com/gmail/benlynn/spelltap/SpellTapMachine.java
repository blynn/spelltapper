package com.gmail.benlynn.spelltap;

abstract class SpellTapMachine {
  SpellTapMachine(SpellTap st) {
    spelltap = st;
  }
  abstract void run();
  static SpellTap spelltap;
}

package com.benlynn.spelltapper;

import android.graphics.Bitmap;
import android.util.Log;

public class Being {
  // TODO: Add new Being to list (rather than have MainView do this),
  // and store its list index in a field.
  public Being(String i_name, Bitmap i_bitmap, int owner) {
    switch(owner) {
      case -1:  // This being is the player.
	y = MainView.ylower - 64;
	index = -1;
	controller = 0;
	break;
      case -2:  // This being is the opponent.
	y = 0;
	index = -2;
	controller = 1;
	break;
      case 0:  // Player controls this being.
	for (index = 0; index < 16; index++) {
	  if (null == pos[index].being) break;
	}
	controller = 0;
	break;
      case 1:  // Opponent controls this being.
	for (index = 16 - 1; index >= 0; index--) {
	  if (null == pos[index].being) break;
	}
	controller = 1;
	break;
      default:
	Log.e("Being", "Ctor given bad owner.");
	break;
    }
    if (owner < 0) {
      x = 160 - 32;
      set_size_64();
    } else {
      if (index < 0 || index >= 16) Log.e("Being", "index out of range! Summon should never have been successful?");
      x = pos[index].x;
      y = pos[index].y;
      pos[index].being = this;
      set_size_48();
      id = controller + 2 * summon_count[controller];
      summon_count[controller]++;
    }
    status = Status.OK;
    unsummoned = false;
    remove_enchantments();
    counterspell = false;
    mirror = false;
    dead = false;
    setup(i_name, i_bitmap, 0);
  }
  void setup(String i_name, Bitmap i_bitmap, int life) {
    name = i_name;
    bitmap = bitmap_alive = i_bitmap;
    bitmap_dead = null;
    start_life(life);
  }
  void heal(int amount) {
    if (!dead) {
      life += amount;
      if (life > life_max) life = life_max;
      lifeline = Integer.toString(life) + "/" + Integer.toString(life_max);
    }
  }
  void get_hurt(int amount) {
    if (!dead) {
      life -= amount;
      lifeline = Integer.toString(life) + "/" + Integer.toString(life_max);
    }
  }
  void set_size_48() {
    w = h = 48;
    midw = midh = 24;
  }
  void set_size_64() {
    w = h = 64;
    midw = midh = 32;
  }
  void start_life(int n) {
    life_max = n;
    heal_full();
  }
  void heal_full() {
    dead = false;
    doomed = false;
    life = life_max;
    lifeline = Integer.toString(life) + "/" + Integer.toString(life);
    bitmap = bitmap_alive;
  }
  void die() {
    dead = true;
    lifeline = "Dead";
    remove_enchantments();
    if (null != bitmap_dead) bitmap = bitmap_dead;
  }
  boolean contains(float xf, float yf) {
    int x0 = (int) xf;
    int y0 = (int) yf;
    return x0 + MainView.SLOP >= x && x0 < x + w + MainView.SLOP &&
	y0 + MainView.SLOP >= y && y0 < y + h + MainView.SLOP;
  }

  void remove_enchantments() {
    poison = 0;
    disease = 0;
    invisibility = 0;
    cast_invisibility = false;
    shield = 0;
    antispell = false;
    resist_heat = false;
    resist_cold = false;
    para_hand = -1;
    status = Status.OK;
  }

  static class Position {
    Position(int init_x, int init_y) {
      x = init_x;
      y = init_y;
      being = null;
    }
    int x, y;
    Being being;
  }

  // TODO: Once the Being's list index is stored in a field, change this
  // to void unsummon().
  void unsummon(int i) {
    if (index < 0) Log.e("Being", "Bug! Cannot unsummon wizard!");
    pos[index].being = null;
    for(int j = list_count - 1; j > i; j--) {
      list[j - 1] = list[j];
    }
    list_count--;
    // TODO: Unsummon animation.
  }

  // Summoned creatures should appear close to their owner, hence this mess.
  static void init() {
    list = new Being[16];
    summon_count = new int[2];
    int x, y;
    x = 160 - 32;
    y = MainView.ylower - 64;
    pos = new Position[16];
    pos[0] = new Position(x - 48 - 10, y);
    pos[1] = new Position(x + 64 + 10, y);
    pos[2] = new Position(x - 48 - 10, y - 48 - 4);
    pos[3] = new Position(x + 64 + 10, y - 48 - 4);
    pos[4] = new Position(x - 2 * 48 - 2 * 10, y);
    pos[5] = new Position(x + 64 + 48 + 2 * 10, y);
    pos[6] = new Position(x - 2 * 48 - 2 * 10, y - 48 - 4);
    pos[7] = new Position(x + 64 + 48 + 2 * 10, y - 48 - 4);
    for (int i = 0; i < 8; i++) {
      pos[8 + i] = new Position(
	  pos[8 - i - 1].x, MainView.ylower - 64 - pos[8 - i - 1].y + 16);
    }
  }
  // Should be called every new game.
  static void reset() {
    summon_count[0] = summon_count[1] = 0;
    for (int i = 0; i < 16; i++) pos[i].being = null;
  }
  static public class Status {
    static public final int OK = 0;
    static public final int CONFUSED = 1;
    static public final int CHARMED = 2;
    static public final int FEAR = 3;
    static public final int AMNESIA = 4;
    static public final int PARALYZED = 5;
  }

  static int[] summon_count;
  static int list_count;
  static Being list[];
  static Position pos[];

  Bitmap bitmap;
  Bitmap bitmap_alive;
  Bitmap bitmap_dead;
  String name;
  String lifeline;
  int index;  // Index into pos.
  int x, y;
  int life;
  int life_max;
  int status;
  int target;
  int shield;
  int w, h;
  int midw, midh;
  // For monsters, the player that controls it.
  // In future, if ever we support more than two players, for players it
  // could represent the source of a Charm Person spell. For now we know
  // it must be the other player.
  short controller;
  boolean dead, doomed, unsummoned;
  boolean counterspell;  // True if protected by counter-spell.
  boolean mirror;  // True if protected by mirror.
  int para_hand;
  int psych;  // Detects psychological spell conflicts.

  // The nth summoned monster by player i is given ID 2 * n + i.
  int id;
  int disease;
  int poison;
  int invisibility;
  boolean cast_invisibility;
  boolean resist_heat, resist_cold;
  boolean is_fireballed;
  boolean raisedead;
  boolean antispell;
}

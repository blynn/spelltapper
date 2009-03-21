// Board: where the action happens. Handles animation.
package com.benlynn.spelltapper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import android.os.Handler;
import android.os.Message;

import com.benlynn.spelltapper.Being.Status;

public class Board extends View {
  public Board(Context context, AttributeSet attrs) {
    super(context, attrs);
    init_board();
  }

  public Board(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init_board();
  }

  void init_board() {
    animation_reset();
    paint = new Paint();
    fade_paint = new Paint();
    white_paint = new Paint();
    white_paint.setARGB(255, 255, 255, 255);
    big_white_text = new Paint();
    big_white_text.setARGB(255, 255, 255, 255);
    big_white_text.setTextSize(32);
    black_stroke_paint = new Paint();
    black_stroke_paint.setStyle(Paint.Style.STROKE);
    black_stroke_paint.setStrokeWidth(2);
    shield_paint = new Paint[4];
    shield_paint[0] = new Paint();
    shield_paint[0].setARGB(191, 255, 63, 63);
    shield_paint[0].setStyle(Paint.Style.STROKE);
    shield_paint[0].setStrokeWidth(4);
    shield_paint[1] = new Paint(shield_paint[0]);
    shield_paint[1].setARGB(191, 255, 255, 63);
    shield_paint[2] = new Paint(shield_paint[0]);
    shield_paint[2].setARGB(191, 127, 255, 127);
    shield_paint[3] = new Paint(shield_paint[2]);
    spot_x[0] = 3; spot_y[0] = -10;
    spot_x[1] = 20; spot_y[1] = 18;
    spot_x[2] = -5; spot_y[2] = 9;
    spot_x[3] = -16; spot_y[3] = 13;
    spot_x[4] = -17; spot_y[4] = -17;
    spot_x[5] = 19; spot_y[5] = -10;
  }

  static int source, target;
  static int x, y;
  static Bitmap bitmap;
  static Bitmap bmsumcirc;
  static int x1, y1;
  static int xdelta, ydelta;
  static Being star;
  static int hand;
  static int frame;
  static int anim;
  static final int ANIM_TILT = -2;
  static final int ANIM_DELAY = -1;
  static final int ANIM_NONE = 0;
  static final int ANIM_MOVE = 1;
  static final int ANIM_MOVE_BACK = 2;
  // TODO: In retrospect, when moving I should change the coordinates stored
  // in the source Being class, and cache the original coordinates for
  // ANIM_MOVE_BACK. That way, I only need one DAMAGE animation routine.
  // TODO: Use floats for coordinates, as the animation is choppy due to
  // accumulated errors. Either that or don't use deltas.
  static final int ANIM_MOVE_DAMAGE = 3;
  static final int ANIM_BULLET = 4;
  static final int ANIM_DAMAGE = 5;
  static final int ANIM_SPELL = 6;
  static final int ANIM_SHIELD = 7;
  static final int ANIM_SUMMON = 8;
  static int delay = 24; // 32
  static int frame_max = 24;
  static Paint fade_paint, white_paint, big_white_text, black_stroke_paint;
  static Paint[] shield_paint;
  static int alphadelta;
  static String damage;
  static int damagex = 12, damagey = 16;
  static float shieldr, rdelta;

  private void update() {
    switch(anim) {
      case ANIM_BULLET:
      case ANIM_MOVE_BACK:
      case ANIM_MOVE:
	x += xdelta;
	y += ydelta;
	break;
      case ANIM_SUMMON:
	star.x += xdelta;
	star.y += ydelta;
	break;
      case ANIM_MOVE_DAMAGE:
      case ANIM_DAMAGE:
      case ANIM_SPELL:
      case ANIM_TILT:
	fade_paint.setAlpha(fade_paint.getAlpha() + alphadelta);
	break;
      case ANIM_SHIELD:
        shieldr += rdelta;
	break;
    }
    // Tilt helper animation has special needs.
    if (ANIM_TILT == anim) {
      // User has one second to execute down tilt to cast spell.
      if (frame < 20) {
	frame++;
	anim_handler.sleep(50);
	return;
      }
    } else if (frame < frame_max) {
      frame++;
      anim_handler.sleep(delay);
      return;
    }

    frame = 0;
    x = x1;
    y = y1;
    if (anim == ANIM_SPELL && alphadelta > 0) {
      // Fade out the spell.
      alphadelta = -alphadelta;
      anim_handler.sleep(delay);
    } else {
      if (ANIM_SUMMON == anim) {
	star.x = x1;
	star.y = y1;
      }
      anim = ANIM_NONE;
      if (null != notify_me) notify_me.sendEmptyMessage(0);
    }
  }

  public void animate_delay() {
    anim = ANIM_DELAY;
    anim_handler.sleep(delay);
  }

  public void animate_tilt() {
    anim = ANIM_TILT;
    fade_paint.setARGB(255, 127, 255, 127);
    alphadelta = -255 / 20;
    anim_handler.sleep(delay);
  }

  public void animate_bullet(int init_source, int init_target) {
    anim = ANIM_BULLET;
    source = init_source;
    target = init_target;
    Being b = Being.list[source];
    x = b.x + b.midw;
    y = b.y + b.midh;
    if (target == -1 || target == source) {
      xdelta = 0;
      ydelta = 0;
      x1 = x;
      y1 = y;
    } else {
      b = Being.list[target];
      x1 = b.x + b.midw;
      y1 = b.y + b.midh;
      ydelta = (y1 - y) / frame_max;
      xdelta = (x1 - x) / frame_max;
    }
    anim_handler.sleep(delay);
  }

  public void animate_shield(int init_target) {
    anim = ANIM_SHIELD;
    target = init_target;
    if (target != -1) {
      Being b = Being.list[target];
      x = b.x + b.midw;
      y = b.y + b.midh;
      shieldr = 1;
      rdelta = (b.midw + 5 - shieldr) / frame_max;
    }
    anim_handler.sleep(delay);
  }

  public void animate_move_back() {
    anim = ANIM_MOVE_BACK;
    Being b = Being.list[source];
    x1 = b.x;
    y1 = b.y;
    ydelta = (y1 - y) / frame_max;
    xdelta = (x1 - x) / frame_max;
    anim_handler.sleep(delay);
  }

  public void animate_move(int init_source, int init_target) {
    anim = ANIM_MOVE;
    source = init_source;
    target = init_target;
    Being b = Being.list[source];
    x = b.x;
    y = b.y;
    if (target == -1 || target == source) {
      xdelta = 0;
      ydelta = 0;
      x1 = x;
      y1 = y;
    } else {
      Being b2 = Being.list[target];
      x1 = b2.x;
      if (b2.y > y) y1 = b2.y - b.h;
      else if (b2.y < y) y1 = b2.y + b2.h;
      else y1 = b2.y;  // TODO: Adjust x1 to avoid overlap?
      ydelta = (y1 - y) / frame_max;
      xdelta = (x1 - x) / frame_max;
    }
    anim_handler.sleep(delay);
  }

  public void animate_summon(int source, int i_hand, Being b) {
    anim = ANIM_SUMMON;
    hand = i_hand;
    star = b;
    // TODO: Fade in monster first.
    x1 = star.x;
    y1 = star.y;
    star.x = MainView.xsumcirc[hand];
    star.y = MainView.ysumcirc[source];
    ydelta = (y1 - star.y) / frame_max;
    xdelta = (x1 - star.x) / frame_max;
    anim_handler.sleep(delay);
  }

  public void animate_spell(int init_target, Bitmap init_bitmap) {
    anim = ANIM_SPELL;
    target = init_target;
    bitmap = init_bitmap;
    fade_paint.setARGB(0, 255, 0, 0);
    alphadelta = 255 / frame_max;
    anim_handler.sleep(delay);
  }

  public void animate_damage(int init_target, int init_damage) {
    anim = ANIM_DAMAGE;
    // TODO: Remove duplicated code.
    target = init_target;
    damage = Integer.toString(init_damage);
    fade_paint.setARGB(200, 255, 0, 0);
    alphadelta = -200 / frame_max;
    anim_handler.sleep(delay);
  }

  public void animate_move_damage(int init_target, int init_damage) {
    anim = ANIM_MOVE_DAMAGE;
    target = init_target;
    damage = Integer.toString(init_damage);
    fade_paint.setARGB(200, 255, 0, 0);
    alphadelta = -200 / frame_max;
    anim_handler.sleep(delay);
  }

  static Paint paint;
  static Handler notify_me;
  void set_notify_me(Handler h) { notify_me = h; }

  public void drawBeing(int i, int mx, int my, Canvas canvas) {
    Being b = Being.list[i];
    if (b.invisibility > 0) {
      canvas.drawBitmap(b.bitmap, mx, my, Easel.invisipaint);
    } else {
      canvas.drawBitmap(b.bitmap, mx, my, paint);
    }
    if (b.shield > 0) {
      int n = b.shield - 1;
      if (n > 3) n = 3;
      canvas.drawCircle(mx + b.midw, my + b.midh, b.midw + 5, shield_paint[n]);
    }
    switch(b.status) {
      case Status.AMNESIA:
	canvas.drawText("Amn", mx + b.w, my + 16 - 4, Easel.tiny_rtext);
	break;
      case Status.CONFUSED:
	canvas.drawText("Conf", mx + b.w, my + 16 - 4, Easel.tiny_rtext);
	break;
      case Status.FEAR:
	canvas.drawText("Fear", mx + b.w, my + 16 - 4, Easel.tiny_rtext);
	break;
      case Status.PARALYZED:
	canvas.drawText("Para", mx + b.w, my + 16 - 4, Easel.tiny_rtext);
	break;
    }
    if (b.dead) {
      canvas.drawText(b.lifeline, mx, my + 16 - 4, Easel.red_paint);
    } else {
      canvas.drawText(b.lifeline, mx, my + 16 - 4, Easel.green_paint);
      for (int k = 0; k < b.disease - 1; k++) {
	canvas.drawCircle(mx + b.midw + spot_x[k], my + b.midh + spot_y[k], 3, Easel.red_paint);
      }
      for (int k = 0; k < b.poison - 1; k++) {
	canvas.drawCircle(mx + b.midw - spot_x[k], my + b.midh - spot_y[k], 3, Easel.green_paint);
      }
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (null == Being.list) return;

    if (!MainView.is_simplified()) {
      canvas.drawBitmap(bmsumcirc,
	  MainView.xsumcirc[0], MainView.ysumcirc[0], paint);
      canvas.drawBitmap(bmsumcirc,
	  MainView.xsumcirc[1], MainView.ysumcirc[0], paint);
      canvas.drawBitmap(bmsumcirc,
	  MainView.xsumcirc[0], MainView.ysumcirc[1], paint);
      canvas.drawBitmap(bmsumcirc,
	  MainView.xsumcirc[1], MainView.ysumcirc[1], paint);
    }

    if (ANIM_SUMMON == anim) {
      canvas.drawBitmap(bmsumcirc, MainView.xsumcirc[hand],
	  MainView.ysumcirc[0], paint);
    }

    // Avatars.
    for (int i = 0; i < Being.list_count; i++) {
      if (anim == ANIM_MOVE || anim == ANIM_MOVE_BACK || anim == ANIM_MOVE_DAMAGE) {
	if (i == source) {
	  continue;
	}
      }
      Being b = Being.list[i];
      drawBeing(i, b.x, b.y, canvas);
    }

    switch(anim) {
      case ANIM_MOVE:
      case ANIM_MOVE_BACK:
	drawBeing(source, x, y, canvas);
	break;
      case ANIM_MOVE_DAMAGE:
	drawBeing(source, x, y, canvas);
	// ***  FALLTHROUGH ***
      case ANIM_DAMAGE:
	if (-1 != target) {
	  Being b = Being.list[target];
	  canvas.drawRect(b.x, b.y, b.x + b.w, b.y + b.h, fade_paint);
	  canvas.drawText(damage, b.x + b.midw - damagex,
	      b.y + b.midh + damagey, big_white_text);
	}
	break;
      case ANIM_SPELL:
	if (-1 != target) {
	  Being b = Being.list[target];
	  canvas.drawBitmap(bitmap, b.x + b.midw - 24, b.y + b.midh - 24, fade_paint);
	}
	break;
      case ANIM_SHIELD:
        canvas.drawCircle(x, y, shieldr, shield_paint[0]);
	break;
      case ANIM_BULLET:
        canvas.drawCircle(x, y, 10, big_white_text);
        canvas.drawCircle(x, y, 11, black_stroke_paint);
	break;
      case ANIM_TILT:
        canvas.drawRect(0, MainView.ylower, 320, 480, fade_paint);
	break;
    }
  }

  private RefreshHandler anim_handler = new RefreshHandler();
  class RefreshHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      Board.this.update();
      Board.this.invalidate();
    }

    public void sleep(long delayMillis) {
      this.removeMessages(0);
      sendEmptyMessageDelayed(0, delayMillis);
    }
  }

  void animation_reset() {
    frame = 0;
    anim = ANIM_NONE;
  }

  // Spots to signify disease.
  static final int spot_x[] = new int[6];
  static final int spot_y[] = new int[6];
}

package com.gmail.benlynn.waver;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import android.os.Handler;
import android.os.Message;

import com.gmail.benlynn.waver.MainView.Being;

public class Arena extends View {
  public Arena(Context context, AttributeSet attrs) {
    super(context, attrs);
    init_arena();
  }

  public Arena(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init_arena();
  }

  void init_arena() {
    anim = ANIM_NONE;
    frame = 0;
    paint = new Paint();
    damage_paint = new Paint();
    damage_paint.setARGB(200, 255, 0, 0);
    big_white_paint = new Paint();
    big_white_paint.setARGB(255, 255, 255, 255);
    big_white_paint.setTextSize(32);
  }

  static int source, target;
  static int x, y;
  static int x1, y1;
  static int xdelta, ydelta;
  static int frame;
  static int anim;
  static final int ANIM_NONE = 0;
  static final int ANIM_MOVE = 1;
  static final int ANIM_MOVE_BACK = 2;
  static final int ANIM_MOVE_DAMAGE = 3;
  static int delay = 20;
  static int frame_max = 20;
  static Paint damage_paint, big_white_paint;
  static int alphadelta;
  static String damage;
  static int damagex = 12, damagey = 16;

  private void update() {
    switch(anim) {
      case ANIM_MOVE_BACK:
      case ANIM_MOVE:
	x += xdelta;
	y += ydelta;
	break;
      case ANIM_MOVE_DAMAGE:
	damage_paint.setAlpha(damage_paint.getAlpha() - alphadelta);
	break;
    }
    if (frame < frame_max) {
      frame++;
      anim_handler.sleep(delay);
    } else {
      frame = 0;
      anim = ANIM_NONE;
      x = x1;
      y = y1;
      notify_me.sendEmptyMessage(0);
    }
  }

  public void animate_move_back() {
    anim = ANIM_MOVE_BACK;
    Being b = being_list[source];
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
    Being b = being_list[source];
    x = b.x;
    y = b.y;
    if (target == -1 || target == source) {
      xdelta = 0;
      ydelta = 0;
      x1 = x;
      y1 = y;
    } else {
      b = being_list[target];
      x1 = b.x;
      if (b.y > y) y1 = b.y - b.h;
      else if (b.y < y) y1 = b.y + b.h;
      else y1 = b.y;  // TODO: Adjust x1 to avoid overlap?
      ydelta = (y1 - y) / frame_max;
      xdelta = (x1 - x) / frame_max;
    }
    anim_handler.sleep(delay);
  }

  public void animate_move_damage(int init_target, int init_damage) {
    anim = ANIM_MOVE_DAMAGE;
    target = init_target;
    damage = Integer.toString(init_damage);
    damage_paint.setAlpha(200);
    alphadelta = 200 / frame_max;
    anim_handler.sleep(delay);
  }

  static Being[] being_list;
  static int being_list_count;
  static Paint paint;
  static Handler notify_me;
  void set_notify_me(Handler h) { notify_me = h; }

  public void drawBeing(int i, int mx, int my, Canvas canvas) {
      Being b = being_list[i];
      canvas.drawBitmap(b.bitmap, mx, my, paint);
      // TODO: Cache life string.
      canvas.drawText(Integer.toString(b.life) + "/" + Integer.toString(b.max_life), mx, my + 16 - 4, paint);
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (null == being_list) return;

    // Avatars.
    for (int i = 0; i < being_list_count; i++) {
      if (anim == ANIM_MOVE || anim == ANIM_MOVE_BACK || anim == ANIM_MOVE_DAMAGE) {
	if (i == source) {
	  continue;
	}
      }
      Being b = being_list[i];
      drawBeing(i, b.x, b.y, canvas);
    }

    switch(anim) {
      case ANIM_MOVE:
      case ANIM_MOVE_BACK:
	drawBeing(source, x, y, canvas);
	break;
      case ANIM_MOVE_DAMAGE:
	drawBeing(source, x, y, canvas);
	if (-1 != target) {
	  Being b = being_list[target];
	  canvas.drawRect(b.x, b.y, b.x + b.w, b.y + b.h, damage_paint);
	  canvas.drawText(damage, b.x + b.midw - damagex,
	      b.y + b.midh + damagey, big_white_paint);
	}
	break;
    }
  }

  private RefreshHandler anim_handler = new RefreshHandler();
  class RefreshHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      Arena.this.update();
      Arena.this.invalidate();
    }

    public void sleep(long delayMillis) {
      this.removeMessages(0);
      sendEmptyMessageDelayed(0, delayMillis);
    }
  }
}

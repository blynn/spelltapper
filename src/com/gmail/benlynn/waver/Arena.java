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
  }

  static int source, target;
  static int x, y;
  static int x1, y1;
  static int xdelta, ydelta;
  static int frame;
  static int anim;
  static final int ANIM_NONE = 0;
  static final int ANIM_MOVE = 1;
  static int delay = 20;
  static int frame_max = 20;

  private void update() {
    frame++;
    x += xdelta;
    y += ydelta;
    if (frame < frame_max) {
      anim_handler.sleep(delay);
    } else {
      frame = 0;
      anim = ANIM_NONE;
      x = x1;
      y = y1;
    }
  }

  public void animate_move(int init_source, int init_target) {
    anim = ANIM_MOVE;
    source = init_source;
    target = init_target;
    MainView.Being b = being_list[source];
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
      if (b.y > y) y1 = b.y - 64;
      else if (b.y < y) y1 = b.y + 64;
      else y1 = b.y;  // TODO: Adjust x1 to avoid overlap?
      ydelta = (y1 - y) / frame_max;
      xdelta = (x1 - x) / frame_max;
    }
    anim_handler.sleep(delay);
  }

  static MainView.Being[] being_list;
  static int being_list_count;
  static Paint paint;

  public void drawBeing(int i, int mx, int my, Canvas canvas) {
      MainView.Being b = being_list[i];
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
      if (anim == ANIM_MOVE) {
	if (i == source) {
	  continue;
	}
      }
      MainView.Being b = being_list[i];
      drawBeing(i, b.x, b.y, canvas);
    }

    if (anim == ANIM_MOVE) {
      drawBeing(source, x, y, canvas);
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
	sendMessageDelayed(obtainMessage(0), delayMillis);
    }
  };
}

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
    bmplayer = BitmapFactory.decodeResource(getResources(), R.drawable.wiz);
    x = 50;
    y = 50;
    i = 0;
  }


  static int x, y;
  static int i;

  private void update() {
    i++;
    x += 10;
    if (i < 20) {
      anim_handler.sleep(10);
    } else {
      i = 0;
      x = 50;
    }
  }

  public void animate() {
    anim_handler.sleep(10);
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    Paint paint = new Paint();
    canvas.drawBitmap(bmplayer, x, y, paint);
  }
  static Bitmap bmplayer;

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

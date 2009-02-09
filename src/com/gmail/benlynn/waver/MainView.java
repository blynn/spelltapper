package com.gmail.benlynn.waver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;

public class MainView extends View {
  public MainView(Context context, AttributeSet attrs) {
    super(context, attrs);
    msg = "Hello";
    paint = new Paint();
    paint.setARGB(255, 255, 255, 255);
    state = 0;
  }

  Paint paint;
  String msg;
  float x0, y0, x1, y1;
  int state;

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
	canvas.drawText(msg, 0, 50, paint);
    switch(state) {
      case 0:
	break;
      case 2:
	canvas.drawLine(x0, y0, x1, y1, paint);
	break;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    // Log.i("Wave", "got event " + event.getAction());
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
	x0 = event.getX();
	y0 = event.getY();
	state = 1;
	return true;
      case MotionEvent.ACTION_UP:
	x1 = event.getX();
	y1 = event.getY();
	float dx = x1 - x0;
	float dy = y1 - y0;
	msg = "unknown";
	if (dy < -16 && -dy > Math.abs(dx) * 2) {
	  if (x1 < 120) {
	    msg = "left up!";
	  } else {
	    msg = "right up!";
	  }
	}
	state = 2;
	invalidate();
	return true;
    }
    return false;
  }
}

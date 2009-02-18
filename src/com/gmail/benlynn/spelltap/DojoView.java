package com.gmail.benlynn.spelltap;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.util.Log;
import android.os.Handler;
import android.os.Message;

public class DojoView extends View {
  public DojoView(Context context, AttributeSet attrs) {
    super(context, attrs);
    paint = new Paint();
  }

  abstract class Machine {
    abstract void run();
  }

  void run() {
    machine.run();
  }

  void set_state_firstlesson() {
    machine = new FirstMachine();
  }
  void set_state_dummy() {
    machine = new DummyMachine();
  }

  class FirstMachine extends Machine {
    FirstMachine() {}
    void run() {
Log.i("DV", "made it here");
      spelltap.mainview.set_state_dummytutorial();
      spelltap.goto_mainframe();
    }
  }

  class DummyMachine extends Machine {
    DummyMachine() {}
    void run() {
      spelltap.mainview.set_state_practicemode();
      spelltap.goto_mainframe();
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        return true;
      case MotionEvent.ACTION_UP:
        run();
        return true;
    }
    return false;
  }
  static Paint paint;
  static SpellTap spelltap;
  static Machine machine;
}

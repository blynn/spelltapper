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

public class SchoolView extends View {
  public SchoolView(Context context, AttributeSet attrs) {
    super(context, attrs);
    paint = new Paint();
  }

  abstract class Machine {
    abstract void run();
  }

  void run() {
    machine.run();
  }

  void set_state_noob() {
    machine = new IntroMachine();
  }
  void set_state_jackwaits() {
    machine = new JackWaitsMachine();
  }
  void set_state_palmlesson() {
    machine = new PalmMachine();
  }
  void set_state_firstadvice() {
    machine = new FirstAdviceMachine();
  }

  class IntroMachine extends Machine {
    IntroMachine() {}
    void run() {
      // TODO: Introduce Jack.
      spelltap.mainview.set_state_knifetutorial();
      spelltap.goto_mainframe();
    }
  }

  class JackWaitsMachine extends Machine {
    JackWaitsMachine() {
      state = 0;
    }
    void run() {
      for(;;) switch(state) {
	case 0:
	  spelltap.narrate(R.string.jackwaitsatdummy);
	  state = 1;
	  return;
	case 1:
	  spelltap.narrate_off();
	  state = 0;
	  spelltap.goto_town();
	  return;
      }
    }
    int state;
  }

  class PalmMachine extends Machine {
    PalmMachine() {}
    void run() {
      // TODO: Another sort of intro?
      spelltap.mainview.set_state_palmtutorial();
      spelltap.goto_mainframe();
    }
  }

  class FirstAdviceMachine extends Machine {
    FirstAdviceMachine() {
      state = 0;
    }
    void run() {
      for(;;) switch(state) {
	case 0:
	  spelltap.jack_says(R.string.jackfirstadvice);
	  state = 1;
	  return;
	case 1:
	  spelltap.jack_shutup();
	  state = 0;
	  spelltap.goto_town();
	  return;
      }
    }
    int state;
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

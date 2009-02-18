package com.gmail.benlynn.spelltap;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class InputHog extends View {
  public InputHog(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
	return true;
      case MotionEvent.ACTION_UP:
        spelltap.hogup();
	return true;
    }
    return false;
  }

  static SpellTap spelltap;
}

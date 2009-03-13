package com.benlynn.spelltapper;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class InputHog extends View {
  public InputHog(Context context, AttributeSet attrs) {
    super(context, attrs);
    is_blocked = false;
  }

  static boolean is_blocked;
  static int old_visibility;
  public void blockInput() {
    is_blocked = true;
    old_visibility = getVisibility();
    setVisibility(View.VISIBLE);
  }
  public void unblockInput() {
    is_blocked = false;
    setVisibility(old_visibility);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (is_blocked) return true;
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
	return true;
      case MotionEvent.ACTION_UP:
        spelltap.hogoff();
	return true;
    }
    return false;
  }

  static SpellTap spelltap;
}

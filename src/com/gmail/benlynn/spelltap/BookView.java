package com.gmail.benlynn.spelltap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.gmail.benlynn.spelltap.MainView.Spell;

public class BookView extends View {
  // Constructor.
  public BookView(Context context, AttributeSet attrs) {
    super(context, attrs);
    list = new Spell[10];
  }

  static void set_level(int i_level) {
    level = i_level;
    list_count = 0;
    for (int i = 0; i < MainView.spell_list_count; i++) {
      Spell sp = MainView.spell_list[i];
      if (sp.level == level && sp.learned) {
	list[list_count] = sp;
	list_count++;
      }
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    set_level(2);
    canvas.drawRect(0, 0, 320, 480, Easel.book_background);
    for (int i = 0; i < list_count; i++) {
      int x = 0, y = i * 50;
      if (i >= 5) {
	x = 160;
	y = (i - 5) * 50;
      }
      Spell sp = list[i];
      canvas.drawBitmap(sp.bitmap, x, y, Easel.paint);
      canvas.drawText(sp.gesture, x + 50, y + 24, Easel.white_text);
      canvas.drawText(sp.name, x + 50, y + 44, Easel.book_spell_text);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    /*
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
	if (NET_REPLY == net_state) {
	  net_state = NET_IDLE;
	  resolve();
	  return false;
	}
	if (NET_WAIT == net_state) return false;
	if (is_animating) return false;
	x0 = event.getX();
	y0 = event.getY();
	if (y0 < ylower) {
	  if (STATE_GESTURE_TEACH == main_state) {
	    run();
	    return false;
	  }
	  // Check for spell retargeting drag.
	  if (y0 >= yicon - SLOP && y0 < yicon + 48 + SLOP) {
	    if (x0 < 48 + SLOP) {
	      drag_i = 0;
	    } else if (x0 >= 320 - 48 - SLOP) {
	      drag_i = 1;
	    }
	  } else {
	    // Check for monster retargeting drag.
	    for (int i = 2; i < Being.list_count; i++) {
	      Being b = Being.list[i];
	      // TODO: Allow corpse retargeting at Level 5 (for Raise Dead).
	      if (b.dead || 0 != b.controller) continue;
	      if (b.contains(x0, y0)) {
		drag_i = i;
		break;
	      }
	    }
	    if (-1 == drag_i && !is_simplified()) {
	      // Check for summon circle retarget.
	      if (y0 >= ysumcirc[0] && y0 <= ysumcirc[0] + 48) {
		if (x0 >= xsumcirc[0] && x0 <= xsumcirc[0] + 48) {
		  drag_i = -2;
		}
		else if (x0 >= xsumcirc[1] && x0 <= xsumcirc[1] + 48) {
		  drag_i = -3;
		}
	      }
	    }
	  }
	  if (-1 != drag_i) {
	    return true;
	  }
	  return false;
	}
	if (y0 >= ystatus) {
	  okstate = true;
	  return true;
	}
	return true;
      case MotionEvent.ACTION_MOVE:
        if ((event.getEventTime() - event.getDownTime()) > 256 && y0 >= ylower) {
	  if (x0 < 160 - BUFFERZONE) {
	    gesture_help = 0;
	    arrow_view.invalidate();
	  } else if (x0 >= 160 + BUFFERZONE) {
	    gesture_help = 1;
	    arrow_view.invalidate();
	  }
	}
        return true;
      case MotionEvent.ACTION_UP:
	if (-1 != gesture_help) {
	  gesture_help = -1;
	  arrow_view.invalidate();
	}
	x1 = event.getX();
	y1 = event.getY();
	if (drag_i != -1) {
	  int target;
	  for(target = Being.list_count - 1; target >= 0; target--) {
	    Being b = Being.list[target];
	    if (b.contains(x1, y1)) break;
	  }
	  if (!is_simplified()) {
	    // Check for summon circle retarget.
	    if (y1 >= ysumcirc[0] && y1 <= ysumcirc[0] + 48) {
	      if (x1 >= xsumcirc[0] && x1 <= xsumcirc[0] + 48) {
		target = -2;
	      }
	      else if (x1 >= xsumcirc[1] && x1 <= xsumcirc[1] + 48) {
		target = -3;
	      }
	    }
	    if (y1 >= ysumcirc[1] && y1 <= ysumcirc[1] + 48) {
	      if (x1 >= xsumcirc[0] && x1 <= xsumcirc[0] + 48) {
		target = -4;
	      }
	      else if (x1 >= xsumcirc[1] && x1 <= xsumcirc[1] + 48) {
		target = -5;
	      }
	    }
	  }
	  if (drag_i < -1) {
	    // Summon circle retarget.
	    fut_choice[-2 - drag_i] = target;
	  } else if (drag_i <= 1) {
	    // drag_i = 0, 1 means player is targeting spell.
	    if (target == -1) {
	      // Doesn't count if still in spell icon.
	      if (y1 >= yicon - SLOP && y1 < yicon + 48 + SLOP) {
		if (x1 < 48 + SLOP) {
		  // In the future I plan to add a meaning for dragging
		  // one spell icon to the other.
		} else if (x1 >= 320 - 48 - SLOP) {
		} else {
		  spell_target[drag_i] = -1;
		}
	      } else {
		spell_target[drag_i] = -1;
	      }
	    } else {
	      spell_target[drag_i] = target;
	      if (STATE_TARGET_TEACH == main_state && target > 1) run();
	    }
	    if (spell_is_twohanded) {
	      spell_target[1 - drag_i] = spell_target[drag_i];
	    }
	  } else {
	    Being b = Being.list[drag_i];
	    if (Status.OK == b.status) {
	      b.target = target;
	    }
	  }
	  drag_i = -1;
	  invalidate();
	  return true;
	}
	float dx = x1 - x0;
	float dy = y1 - y0;
	if (dx * dx + dy * dy < 32 * 32) {
	  if (STATE_GESTURE_TEACH == main_state) {
	    run();
	    return true;
	  }
	  if (okstate && y1 >= ystatus) {
	    confirm_move();
	    return true;
	  }
	  if (STATE_ON_CONFIRM == main_state) return false;
	  if (y1 >= yspellrow && y1 < yspellrow + 2 * 50) {
	    Log.i("MV", "coord " + x1 + " " + y1);
	    // Could be choosing a ready spell.
	    int i = 0, h = 0;  // Initial values suppress bogus warnings.
	    if (y1 > yspellrow + 50) {
	      // Handle up to two-handed spells.
	      if (x1 >= 160 - 50 && x1 < 160 + 50) {
		i = x1 < 160 ? 0 : 1;
		if (i < ready_spell_count[2]) choose_twohanded_spell(i);
		return true;
	      } else {
		i = 3;
	      }
	    }
	    // Handle one-handed spells.
	    if (x1 < 3 * 50) {
	      i += ((int) x1) / 50;
	      h = 0;
	    } else if (x1 > 320 - 3 * 50) {
	      i += (320 - (int) x1) / 50;
	      h = 1;
	    }
	    if (i < ready_spell_count[h]) {
	      choose_spell(h, i);
	      return true;
	    }
	  }
	} else if (x0 >= 160 - BUFFERZONE && x0 < 160 + BUFFERZONE) {
	  return false;
	} else {
	  if (STATE_ON_CONFIRM == main_state ||
	      freeze_gesture ||
	      TILT_AWAIT_DOWN == tilt_state ||
	      STATE_TARGET_TEACH == main_state) {
	    return false;
	  }
	  int dirx, diry;
	  int h;
	  dirx = dx > 0 ? 1 : -1;
	  diry = dy > 0 ? 1 : -1;
	  if (Math.abs(dy) > Math.abs(dx) * 2) {
	    dirx = 0;
	  } else if (Math.abs(dx) > Math.abs(dy) * 2) {
	    diry = 0;
	  }
	  if (x0 < 160) {
	    h = 0;
	  } else {
	    h = 1;
	    dirx *= -1;
	  }
	  if (!choosing_para && !choosing_charm) {
	    if (h == charmed_hand) return true;
	    if (Status.PARALYZED == Being.list[0].status &&
		h == Being.list[0].para_hand) return true;
	  }
	  choice[h] = Gesture.flattenxy(dirx, diry);
	  if (null == Gesture.list[choice[h]] || !Gesture.list[choice[h]].learned) {
	    choice[h] = Gesture.NONE;
	  }
	  if (!choosing_para && !choosing_charm) {
	    if (Status.FEAR == Being.list[0].status &&
		choice[h] != Gesture.PALM &&
		choice[h] != Gesture.WAVE &&
		choice[h] != Gesture.KNIFE) choice[h] = Gesture.NONE;
	  }
	  if (choice[h] != lastchoice[h]) {
	    handle_new_choice(h);
	  }
	  if (STATE_GESTURE_TEACH == main_state) run();
	  return true;
	}
    }
    */
    return false;
  }
  static int level = 1;
  static int list_count;
  static Spell list[];
}

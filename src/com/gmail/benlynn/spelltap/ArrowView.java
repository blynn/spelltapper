// Draws spell icons and arrows pointing to their targets, and arrows from
// monsters to their targets (but not the monsters or targets themselves;
// MainView/Board handles this).
//
// TODO: Arrowheads.
package com.gmail.benlynn.spelltap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;
import com.gmail.benlynn.spelltap.Being.Status;

public class ArrowView extends View {
  public ArrowView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ArrowView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  private void init() {
    bmspell = new Bitmap[2];
    bmspell[0] = bmspell[1] = null;
  }

  static Bitmap bmspell[];

  static int xtgt, ytgt;
  static Paint ptgt;
  void compute_xytgt(int target) {
    if (target >= 0) {
      Being b2 = Being.list[target];
      xtgt = b2.x + b2.midw;
      ytgt = b2.y + b2.midh;
    } else if (-1 == target) Log.e("ArrowView", "Bug! Arrow to thin air!");
    else {
      int h = -2 - target;
      int j = 0;
      if (h >= 2) {
	h -= 2;
	j++;
      }
      xtgt = MainView.xsumcirc[h] + 24;
      ytgt = MainView.ysumcirc[j] + 24;
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // Spell icons.
    int x = 0;
    int y = MainView.yicon;

    for (int h = 0; h < 2; h++) {
      if (null != bmspell[h]) {
	canvas.drawBitmap(bmspell[h], x, y, Easel.paint);
	int i = MainView.spell_target[h];
	if (-1 != i) {
	  compute_xytgt(i);
	  if (i < -1)  {
	    ptgt = Easel.fut_arrow_paint;
	  } else {
	    ptgt = Easel.arrow_paint;
	  }
	  canvas.drawLine(x + 24, y + 24, xtgt, ytgt, ptgt);
	}
      }
      x = 320 - 48 - 1;
    }

    // Monster attacks.
    for (int i = 2; i < Being.list_count; i++) {
      Being b = Being.list[i];
      if (!b.dead && 0 == b.controller && -1 != b.target) {
	compute_xytgt(b.target);
	if (Status.OK != b.status) {
	  ptgt = Easel.weird_arrow_paint;
	} else if (b.target < -1)  {
	  ptgt = Easel.fut_arrow_paint;
	} else {
	  ptgt = Easel.arrow_paint;
	}
	canvas.drawLine(b.x + b.midw, b.y + b.midh, xtgt, ytgt, ptgt);
      }
    }

    // Future attacks
    if (!MainView.is_simplified()) {
      for (int h = 0; h < 2; h++) {
	int n = MainView.fut_choice[h];
	if (-1 != n) {
	  compute_xytgt(n);
	  canvas.drawLine(MainView.xsumcirc[h] + 24, MainView.ysumcirc[0] + 24,
	      xtgt, ytgt, Easel.fut_arrow_paint);
	}
      }
    }

    // Gesture compass
    if (-1 != MainView.gesture_help) {
      x = 0;
      y = 128;
      if (1 == MainView.gesture_help) x = 200;
      canvas.drawRect(x, y, x + 120, y + 120, Easel.compass_background);
      for (int i = 0; i <= 8; i++) {
	Gesture g = Gesture.list[i];
	if (null != g && g.learned) {
	  x = g.x * 20;
	  if (1 == MainView.gesture_help) x = 200 - x;
	  y = g.y * 20;
	  canvas.drawText(g.arrow[MainView.gesture_help], x + 52, y + 64 + 128, Easel.compass_text);
	  x = g.x * 40;
	  if (1 == MainView.gesture_help) x = 200 - x;
	  y *= 2;
	  canvas.drawText("" + g.initial, x + 52, y + 64 + 128, Easel.compass_text);
	}
      }
    }
  }
}

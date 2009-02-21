// TODO: Arrowheads.
package com.gmail.benlynn.spelltap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;
import com.gmail.benlynn.spelltap.MainView.Being;

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
    paint = new Paint();
    paint.setARGB(191, 127, 255, 127);
    paint.setStrokeWidth(4);
    paint.setStrokeCap(Paint.Cap.ROUND);
    iconpaint = new Paint();
    bmspell = new Bitmap[2];
    bmspell[0] = bmspell[1] = null;
  }

  static Paint paint, iconpaint;
  static Bitmap bmspell[];

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // Spell icons.
    int x = 0;
    int y = MainView.yicon;

    for (int h = 0; h < 2; h++) {
      if (null != bmspell[h]) {
	canvas.drawBitmap(bmspell[h], x, y, iconpaint);
	int i = MainView.spell_target[h];
	if (-1 != i) {
	  Being b = MainView.being_list[i];
	  canvas.drawLine(x + 24, y + 24, b.x + b.midw, b.y + b.midh, paint);
	}
      }
      x = 320 - 48 - 1;
    }

    // Monster attacks.
    for (int i = 2; i < MainView.being_list_count; i++) {
      Being b = MainView.being_list[i];
      if (!b.dead && 0 == b.controller && -1 != b.target) {
	Being b2 = MainView.being_list[b.target];
	canvas.drawLine(b.x + b.midw, b.y + b.midh,
	    b2.x + b2.midw, b2.y + b2.midh, paint);
      }
    }
  }
}

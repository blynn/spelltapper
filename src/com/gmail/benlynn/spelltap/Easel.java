package com.gmail.benlynn.spelltap;

import android.graphics.Paint;

class Easel {
  static void init() {
    paint = new Paint();
    // Translucent green rounded lines for arrows.
    arrow_paint = new Paint();
    arrow_paint.setARGB(191, 127, 255, 127);
    arrow_paint.setStrokeWidth(4);
    arrow_paint.setStrokeCap(Paint.Cap.ROUND);
    weird_arrow_paint = new Paint(arrow_paint);
    weird_arrow_paint.setARGB(191, 255, 127, 255);

    grey_text = new Paint();
    grey_text.setARGB(255, 143, 143, 143);
    grey_rtext = new Paint(grey_text);
    grey_rtext.setTextAlign(Paint.Align.RIGHT);
    grey_ctext = new Paint(grey_text);
    grey_ctext.setTextAlign(Paint.Align.CENTER);

    white_text = new Paint();
    white_text.setARGB(255, 255, 255, 255);
    white_rtext = new Paint(white_text);
    white_rtext.setTextAlign(Paint.Align.RIGHT);

    octarine = new Paint();
    octarine.setARGB(255, 63, 0, 63);

    status_paint = new Paint();
    status_paint.setARGB(255, 95, 63, 95);

    sel_paint = new Paint();
    sel_paint.setARGB(255, 127, 255, 127);
  }

  static Paint paint;
  static Paint arrow_paint, weird_arrow_paint;
  static Paint grey_text, grey_rtext, grey_ctext;
  static Paint white_text, white_rtext;
  static Paint octarine;
  static Paint status_paint;
  static Paint sel_paint;
}

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

    green_paint = new Paint();
    green_paint.setARGB(255, 127, 255, 125);

    red_paint = new Paint();
    red_paint.setARGB(255, 255, 63, 63);

    white_text = new Paint();
    white_text.setARGB(255, 255, 255, 255);
    white_text.setTextSize(18);
    white_text.setAntiAlias(true);
    white_text.setSubpixelText(true);
    white_rtext = new Paint(white_text);
    white_rtext.setTextAlign(Paint.Align.RIGHT);

    grey_text = new Paint(white_text);
    grey_text.setARGB(255, 143, 143, 143);
    grey_rtext = new Paint(grey_text);
    grey_rtext.setTextAlign(Paint.Align.RIGHT);

    charm_text = new Paint(white_text);
    charm_text.setARGB(255, 255, 127, 255);
    charm_rtext = new Paint(charm_text);
    charm_rtext.setTextAlign(Paint.Align.RIGHT);

    history_text = new Paint(grey_text);
    history_rtext = new Paint(grey_rtext);
    history_text.setTextSize(16);
    history_rtext.setTextSize(16);

    octarine = new Paint();
    octarine.setARGB(255, 63, 0, 63);

    status_paint = new Paint();
    status_paint.setARGB(255, 95, 63, 95);

    sel_paint = new Paint();
    sel_paint.setARGB(255, 127, 255, 127);
  }

  static Paint paint;
  static Paint arrow_paint, weird_arrow_paint;
  static Paint grey_text, grey_rtext;
  static Paint white_text, white_rtext;
  static Paint green_paint;
  static Paint red_paint;
  static Paint octarine;
  static Paint status_paint;
  static Paint sel_paint;
  static Paint charm_text, charm_rtext;
  static Paint history_text, history_rtext;
}

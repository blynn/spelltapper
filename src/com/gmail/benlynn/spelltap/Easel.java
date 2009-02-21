package com.gmail.benlynn.spelltap;

import android.graphics.Paint;

class Easel {
  static void init() {
    paint = new Paint();
    arrow_paint = new Paint();
    arrow_paint.setARGB(191, 127, 255, 127);
    arrow_paint.setStrokeWidth(4);
    arrow_paint.setStrokeCap(Paint.Cap.ROUND);
  }

  static Paint paint;
  static Paint arrow_paint;
}

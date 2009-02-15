package com.gmail.benlynn.waver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

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
    x0 = new int[16];
    y0 = new int[16];
    x1 = new int[16];
    y1 = new int[16];
    arrow_count = 0;
    paint = new Paint();
    paint.setARGB(191, 127, 255, 127);
    paint.setStrokeWidth(4);
    paint.setStrokeCap(Paint.Cap.ROUND);
  }

  public void clear_arrows() {
    arrow_count = 0;
  }

  public void add_arrow(int from_x, int from_y, int to_x, int to_y) {
    x0[arrow_count] = from_x;
    y0[arrow_count] = from_y;
    x1[arrow_count] = to_x;
    y1[arrow_count] = to_y;
    arrow_count++;
  }

  static int x0[], y0[], x1[], y1[];
  static int arrow_count;
  static Paint paint;

  @Override
  public void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    for (int i = 0; i < arrow_count; i++) {
      canvas.drawLine(x0[i], y0[i], x1[i], y1[i], paint);
    }
  }
}

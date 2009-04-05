package com.benlynn.spelltapper;

import android.graphics.Paint;

class Easel {
  static void init() {
    paint = new Paint();
    // Translucent green rounded lines for arrows.
    arrow_paint = new Paint();
    arrow_paint.setARGB(191, 127, 255, 127);
    arrow_paint.setStrokeWidth(3);
    arrow_paint.setStrokeCap(Paint.Cap.ROUND);
    weird_arrow_paint = new Paint(arrow_paint);
    weird_arrow_paint.setARGB(191, 255, 127, 255);
    fut_arrow_paint = new Paint(arrow_paint);
    fut_arrow_paint.setARGB(127, 63, 127, 63);

    green_paint = new Paint();
    green_paint.setARGB(255, 127, 255, 125);

    invisipaint = new Paint();
    invisipaint.setARGB(63, 0, 0, 0);

    red_paint = new Paint();
    red_paint.setARGB(255, 255, 63, 63);
    dark_red_paint = new Paint(red_paint);
    dark_red_paint.setARGB(255, 95, 0, 0);

    white_text = new Paint();
    white_text.setARGB(255, 255, 255, 255);
    white_text.setTextSize(18);
    white_text.setAntiAlias(true);
    white_text.setSubpixelText(true);
    white_rtext = new Paint(white_text);
    white_rtext.setTextAlign(Paint.Align.RIGHT);

    white_ctext = new Paint(white_text);
    white_ctext.setTextAlign(Paint.Align.CENTER);

    spell_text = new Paint(white_text);
    spell_text.setTextSize(16);
    spell_rtext = new Paint(white_rtext);
    spell_rtext.setTextSize(16);

    grey_text = new Paint(white_text);
    grey_text.setARGB(255, 143, 143, 143);
    grey_rtext = new Paint(grey_text);
    grey_rtext.setTextAlign(Paint.Align.RIGHT);

    tap_ctext = new Paint(white_ctext);
    tap_ctext.setARGB(255, 127, 255, 127);

    charm_text = new Paint(white_text);
    charm_text.setARGB(255, 255, 127, 255);
    charm_rtext = new Paint(charm_text);
    charm_rtext.setTextAlign(Paint.Align.RIGHT);

    history_text = new Paint(grey_text);
    history_rtext = new Paint(grey_rtext);
    history_text.setTextSize(16);
    history_rtext.setTextSize(16);
    para_text = new Paint(history_text);
    para_rtext = new Paint(history_rtext);
    para_text.setARGB(255, 63, 159, 63);
    para_rtext.setARGB(255, 63, 159, 63);

    octarine = new Paint();
    octarine.setARGB(255, 63, 0, 63);

    status_paint = new Paint();
    status_paint.setARGB(255, 95, 63, 95);

    surrender_paint = new Paint();
    surrender_paint.setARGB(255, 127, 0, 0);

    sel_paint = new Paint();
    sel_paint.setARGB(255, 127, 255, 127);
    sel_paint.setStyle(Paint.Style.STROKE);

    wait_paint = new Paint();
    wait_paint.setARGB(255, 127, 0, 0);
    reply_paint = new Paint();
    reply_paint.setARGB(255, 0, 0, 170);

    tiny_rtext = new Paint(white_rtext);
    tiny_rtext.setTextSize(12);

    comment_text = new Paint(white_text);
    comment_text.setTextSize(16);

    compass_text = new Paint(white_text);
    compass_text.setTextSize(20);

    compass_background = new Paint();
    compass_background.setARGB(159, 0, 0, 0);

    book_background = new Paint();
    book_background.setARGB(223, 0, 0, 0);

    book_spell_text = new Paint(grey_text);
    book_spell_text.setTextSize(12);
  }

  static Paint paint;
  static Paint arrow_paint, weird_arrow_paint;
  static Paint fut_arrow_paint;
  static Paint grey_text, grey_rtext;
  static Paint tap_ctext;
  static Paint white_text, white_rtext;
  static Paint white_ctext;
  static Paint green_paint;
  static Paint red_paint;
  static Paint octarine;
  static Paint status_paint;
  static Paint sel_paint;
  static Paint charm_text, charm_rtext;
  static Paint history_text, history_rtext;
  static Paint spell_text, spell_rtext;
  static Paint wait_paint, reply_paint;
  static Paint surrender_paint;
  static Paint tiny_rtext;
  static Paint para_text, para_rtext;
  static Paint dark_red_paint;
  static Paint comment_text;
  static Paint compass_text;
  static Paint compass_background;
  static Paint book_background;
  static Paint book_spell_text;
  static Paint invisipaint;
}

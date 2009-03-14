package com.benlynn.spelltapper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.benlynn.spelltapper.MainView.Spell;

public class BookView extends View {
  // Constructor.
  public BookView(Context context, AttributeSet attrs) {
    super(context, attrs);
    list = new Spell[10];
    level = 1;
    choice = -1;
  }

  static void set_level(int i_level) {
    level = i_level;
    choice = -1;
    compute_spells();
  }

  void prev() {
    if (level > 1) set_level(level - 1);
    invalidate();
  }

  void next() {
    if (level < Player.level) set_level(level + 1);
    invalidate();
  }

  static void compute_spells() {
    list_count = 0;
    for (int i = 0; i < MainView.spell_list_count; i++) {
      Spell sp = MainView.spell_list[i];
      if (sp.level == level && sp.learned) {
	if ("WWS" == sp.gesture) continue;
	list[list_count] = sp;
	list_count++;
      }
    }
  }

  @Override
  public void onDraw(Canvas canvas) {
    //Rect r = new Rect();
    super.onDraw(canvas);
    canvas.drawRect(0, 0, 320, 480, Easel.book_background);
    canvas.drawText("Level " + level + " spells", 0, 16, Easel.white_text);
    for (int i = 0; i < list_count; i++) {
      int x = 0, y = i * 50;
      if (i >= 5) {
	x = 160;
	y = (i - 5) * 50;
      }
      y += 20;
      if (i == choice) {
	canvas.drawRect(x, y, x + 50, y + 50, Easel.sel_paint);
      }
      Spell sp = list[i];
      String g = sp.gesture;
      if ("WPP" == sp.gesture) {
	g += ", WWS";
      }
      canvas.drawBitmap(sp.bitmap, x + 1, y + 1, Easel.paint);
      canvas.drawText(g, x + 50, y + 24, Easel.white_text);
      //Easel.book_spell_text.getTextBounds(sp.name, 0, sp.name.length(), r);
      //r.offset(x + 160, y + 44);
      //canvas.drawRect(r, Easel.book_background);
      canvas.drawText(sp.name, x + 50, y + 44, Easel.book_spell_text);
    }
    if (-1 == choice) {
      if (list_count == 0) {
	tv.setText(R.string.emptyspellbook);
      } else {
	tv.setText(R.string.book_nochoice);
      }
    } else {
      Spell sp = list[choice];
      canvas.drawText(sp.name, 0, 286, Easel.white_text);
      tv.setText(sp.description);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
	int x0 = (int) event.getX();
	int y0 = (int) event.getY();
	if (y0 < 5 * 50 + 20) {
	  choice = (y0 - 20) / 50;
	  if (x0 >= 160) choice += 5;
	}
	if (choice >= list_count) choice = -1;
	invalidate();
	return true;
    }
    return true;
  }
  static int level = 1;
  static int list_count;
  static int choice;
  static Spell list[];
  static TextView tv;
}

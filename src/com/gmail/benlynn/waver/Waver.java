package com.gmail.benlynn.waver;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.View;
import android.widget.TextView;

public class Waver extends Activity {
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // No title bar.
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.main);
    View tv = findViewById(R.id.speech);
    tv.setVisibility(View.GONE);
    MainView mv = (MainView) findViewById(R.id.mainview);
    mv.set_arena((Arena) findViewById(R.id.arena));
    mv.set_arrow_view((ArrowView) findViewById(R.id.arrow_view));
    //mv.set_speech((TextView) findViewById(R.id.speech));
  }
}

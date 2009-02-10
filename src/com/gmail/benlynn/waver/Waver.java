package com.gmail.benlynn.waver;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.ViewGroup;

public class Waver extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	// No title bar.
	requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
	ViewGroup vg = (ViewGroup) findViewById(R.id.container);
	vg.removeViewAt(1);
    }
}

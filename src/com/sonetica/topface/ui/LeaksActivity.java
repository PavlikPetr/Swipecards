package com.sonetica.topface.ui;

import java.util.Vector;
import com.sonetica.topface.utils.LeaksManager;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

public class LeaksActivity extends Activity {
  //---------------------------------------------------------------------------
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    System.gc();
    
    ScrollView scrollView = new ScrollView(this);
    TextView textView = new TextView(this);
    scrollView.addView(textView);
    LeaksManager leakManager = LeaksManager.getInstance();
    Vector<String> frames = leakManager.checkLeaks();
    textView.setText("LeaksManager ");
    textView.append("[size:"+frames.size()+"]\n");
    for(int i=frames.size()-1;i>=0;i--)
      textView.append(frames.get(i)+"\n");
    
    setContentView(scrollView);
  }
  //---------------------------------------------------------------------------
}

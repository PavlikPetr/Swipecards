package com.sonetica.topface.ui;

import com.sonetica.topface.Data;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;
import android.widget.TextView;

public class JLogActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ScrollView scrollView = new ScrollView(this);
    TextView textView = new TextView(this);
    scrollView.addView(textView);

    
    for(int i=Data.s_LogList.size()-1;i>0;i--) {
      if(Data.s_LogList.get(i).length()>200)
        textView.append(Data.s_LogList.get(i).substring(0,200)+"\n");
      else
        textView.append(Data.s_LogList.get(i)+"\n");
    }
    
    setContentView(scrollView);
  }
}

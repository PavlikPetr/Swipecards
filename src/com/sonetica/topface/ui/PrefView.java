package com.sonetica.topface.ui;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrefView extends LinearLayout {
  // Data
  private TextView mKey;
  private TextView mValue;
  //---------------------------------------------------------------------------
  public PrefView(Context context) {
    this(context,null);
  }
  //---------------------------------------------------------------------------
  public PrefView(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    setClickable(true);
    setOrientation(VERTICAL);
    setPadding(20,6,6,6);
    /*
    android:layout_marginLeft="20dip"
    android:layout_marginRight="6dip"
    android:layout_marginTop="6dip"
    android:layout_marginBottom="6dip"
    */
    //setMinimumHeight(android.R.attr.listPreferredItemHeight);
    setBackgroundResource(R.drawable.list_selector_background);

    mKey = new TextView(context);
    mKey.setMaxLines(1);
    mKey.setText("key");
    addView(mKey);
    
    mValue = new TextView(context);
    mValue.setMaxLines(1);
    mValue.setText("value");
    addView(mValue);
    
  }
  //---------------------------------------------------------------------------
  public void setKeyName(String keyName) {
    mKey.setText(keyName);
  }
  //---------------------------------------------------------------------------
  public void setValueName(String valueName) {
    mValue.setText(valueName);    
  }
  //---------------------------------------------------------------------------  
}


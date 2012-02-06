package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout;

public class DoubleButton extends LinearLayout {
  // Data
  private Button mLeftBoysButton;
  private Button mRightGirlsButoon;
  //---------------------------------------------------------------------------
  public DoubleButton(Context context) {
    super(context,null);
  }
  //---------------------------------------------------------------------------
  public DoubleButton(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    mLeftBoysButton = new Button(context);
    mLeftBoysButton.setText(getResources().getString(R.string.tops_btn_boys));
    mLeftBoysButton.setBackgroundResource(R.drawable.btn_double_left_selector);
    addView(mLeftBoysButton);
    mRightGirlsButoon = new Button(context);
    mRightGirlsButoon.setText(getResources().getString(R.string.tops_btn_girls));
    mRightGirlsButoon.setBackgroundResource(R.drawable.btn_double_right_selector);
    addView(mRightGirlsButoon);
    
    /*
    // Inflater
    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.zoom_controls,this,true);
    mLeftButton  = (ImageButton)findViewById(R.id.zoomIn);
    mRightButoon = (ImageButton)findViewById(R.id.zoomOut);
    */
    
  }
  //---------------------------------------------------------------------------
}

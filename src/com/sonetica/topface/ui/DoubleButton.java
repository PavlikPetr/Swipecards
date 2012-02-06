package com.sonetica.topface.ui;

import com.sonetica.topface.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class DoubleButton extends LinearLayout {
  // Data
  private Button mLeftButton;
  private Button mRightButoon;
  //---------------------------------------------------------------------------
  public DoubleButton(Context context) {
    super(context,null);
  }
  //---------------------------------------------------------------------------
  public DoubleButton(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.btn_double,this,true);

    mLeftButton  = (Button)findViewById(R.id.dblLeft);
    mRightButoon = (Button)findViewById(R.id.dblRight);
    
  }
  //---------------------------------------------------------------------------
  public void setLeftText(String text) {
    mLeftButton.setText(text);
  }
  //---------------------------------------------------------------------------
  public void setRightText(String text) {
    mRightButoon.setText(text);
  }
  //---------------------------------------------------------------------------
  public void setLeftListener(OnClickListener onClickListener) {
    mLeftButton.setOnClickListener(onClickListener);
  }
  //---------------------------------------------------------------------------
  public void setRightListener(OnClickListener onClickListener) {
    mRightButoon.setOnClickListener(onClickListener);
  }
  //---------------------------------------------------------------------------
}

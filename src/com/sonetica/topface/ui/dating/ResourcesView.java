package com.sonetica.topface.ui.dating;

import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ResourcesView extends LinearLayout {
  // Data
  private TextView mPowerTxt;
  private TextView mMoneyTxt;
  private ImageView mPowerImg;
  private ImageView mMoneyImg;
  //---------------------------------------------------------------------------
  public ResourcesView(Context context) {
    super(context);
    
    int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
    
    setBackgroundColor(Color.TRANSPARENT);
    setOrientation(HORIZONTAL);
    setPadding(padding,padding,0,0);
    

    // power
    mPowerTxt = new TextView(context);
    mPowerTxt.setText(""+Data.s_Power);
    mPowerTxt.setTextColor(Color.WHITE);
    mPowerTxt.setTypeface(Typeface.DEFAULT_BOLD);
    mPowerTxt.setPadding(padding,0,padding,0);
    mPowerTxt.setGravity(Gravity.CENTER);
    
    mPowerImg = new ImageView(context);
    mPowerImg.setImageResource(R.drawable.dating_power);
    
    // money
    mMoneyTxt = new TextView(context);
    mMoneyTxt.setText(""+Data.s_Money);
    mMoneyTxt.setTextColor(Color.WHITE);
    mMoneyTxt.setTypeface(Typeface.DEFAULT_BOLD);
    mMoneyTxt.setPadding(padding,0,padding,0);
    mMoneyTxt.setGravity(Gravity.CENTER);
    
    mMoneyImg = new ImageView(context);
    mMoneyImg.setImageResource(R.drawable.dating_money);
    
    addView(mPowerTxt);
    addView(mPowerImg);
    
    addView(mMoneyTxt);
    addView(mMoneyImg);
  }
  //---------------------------------------------------------------------------
  public void setResources(int power,int money) {
    mPowerTxt.setText(""+power);
    mMoneyTxt.setText(""+money);
  }
  //---------------------------------------------------------------------------
  public void release() {
    mPowerTxt = null;
    mMoneyTxt = null;
    mPowerImg = null;
    mMoneyImg = null;
  }
  //---------------------------------------------------------------------------
}

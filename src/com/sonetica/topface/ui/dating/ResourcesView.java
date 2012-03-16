package com.sonetica.topface.ui.dating;

import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ResourcesView extends LinearLayout {
  // Data
  private TextView  mPowerTxt;
  private TextView  mMoneyTxt;
//  private ImageView mPowerImg;
//  private ImageView mMoneyImg;
  private ImageView mBuying;
  //---------------------------------------------------------------------------
  public ResourcesView(Context context) {
    this(context,null);
  }
  //---------------------------------------------------------------------------
  public ResourcesView(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
    
    setId(R.id.datingRes);
    setBackgroundColor(Color.TRANSPARENT);
    setOrientation(HORIZONTAL);
    setPadding(6,6,0,0);

    //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
    //layoutParams.gravity = Gravity.CENTER_VERTICAL;
    
    Drawable drwbl_power = getResources().getDrawable(R.drawable.dating_power);
    Drawable drwbl_money = getResources().getDrawable(R.drawable.dating_money);

    // power
    mPowerTxt = new TextView(context);
    mPowerTxt.setText(""+Data.s_Power);
    mPowerTxt.setTextColor(Color.WHITE);
    mPowerTxt.setTypeface(Typeface.DEFAULT_BOLD);
    mPowerTxt.setPadding(padding,0,padding,0);
    mPowerTxt.setCompoundDrawablePadding(5);
    mPowerTxt.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_power,null);
    addView(mPowerTxt);
    
//    mPowerImg = new ImageView(context);
//    mPowerImg.setImageResource(R.drawable.dating_power);
//    addView(mPowerImg);
    
    
    
    // money
    mMoneyTxt = new TextView(context);
    mMoneyTxt.setText(""+Data.s_Money);
    mMoneyTxt.setTextColor(Color.WHITE);
    mMoneyTxt.setTypeface(Typeface.DEFAULT_BOLD);
    mMoneyTxt.setPadding(padding,0,padding,0);
    mMoneyTxt.setCompoundDrawablePadding(5);
    mMoneyTxt.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_money,null);
    addView(mMoneyTxt);
    
//    mMoneyImg = new ImageView(context);
//    mMoneyImg.setImageResource(R.drawable.dating_money);
//    addView(mMoneyImg);
    
    // Buying
    mBuying = new ImageView(context);
    mBuying.setId(R.id.datingPlus);
    mBuying.setImageResource(R.drawable.dating_plus);
    mBuying.setVisibility(View.GONE);
    mBuying.setEnabled(false);
    addView(mBuying);    
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
//    mPowerImg = null;
//    mMoneyImg = null;
    mBuying   = null;
  }
  //---------------------------------------------------------------------------
}

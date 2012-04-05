package com.topface.topface.ui.dating;

import com.topface.topface.Data;
import com.topface.topface.R;
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
  private ImageView mBuying;
  //---------------------------------------------------------------------------
  public ResourcesView(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    int padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, getResources().getDisplayMetrics());

    setId(R.id.datingRes);
    setBackgroundColor(Color.TRANSPARENT);
    setOrientation(HORIZONTAL);
    
    Drawable drwbl_power = getResources().getDrawable(R.drawable.dating_power);
    Drawable drwbl_money = getResources().getDrawable(R.drawable.dating_money);

    // power
    mPowerTxt = new TextView(context);
    mPowerTxt.setText(""+Data.s_Power);
    mPowerTxt.setTextColor(Color.WHITE);
    mPowerTxt.setPadding(0,0,padding,0);
    mPowerTxt.setTypeface(Typeface.DEFAULT_BOLD);
    mPowerTxt.setCompoundDrawablePadding(5);
    mPowerTxt.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_power,null);
    addView(mPowerTxt);

    // money
    mMoneyTxt = new TextView(context);
    mMoneyTxt.setText(""+Data.s_Money);
    mMoneyTxt.setTextColor(Color.WHITE);
    mMoneyTxt.setPadding(0,0,padding,0);
    mMoneyTxt.setTypeface(Typeface.DEFAULT_BOLD);
    mMoneyTxt.setCompoundDrawablePadding(5);
    mMoneyTxt.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_money,null);
    addView(mMoneyTxt);
    
    // plus
    mBuying = new ImageView(context);
    mBuying.setMinimumHeight(drwbl_money.getMinimumHeight());
    mBuying.setId(R.id.datingPlus);
    mBuying.setPadding(0,padding/2,0,0);
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
    mBuying   = null;
  }
  //---------------------------------------------------------------------------
}

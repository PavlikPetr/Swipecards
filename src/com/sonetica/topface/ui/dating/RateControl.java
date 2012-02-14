package com.sonetica.topface.ui.dating;

import android.content.Context;
import android.view.ViewGroup;

public class RateControl extends ViewGroup {
  // Data
  private InformerView mInformerView;
  private StarsView mStarsView;
  //---------------------------------------------------------------------------
  public RateControl(Context context) {
    super(context);
    mInformerView = new InformerView(context);
    addView(mInformerView);
    mStarsView = new StarsView(context,mInformerView);
    addView(mStarsView);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);

    int count = getChildCount();
    for(int i=0;i<count;i++)
      getChildAt(i).measure(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec)-DatingActivity.mHeaderBar.getHeight());
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int l,int t,int r,int b) {
    int stars_x = getMeasuredWidth() - mStarsView.getMeasuredWidth();
    int stars_y = 0;
    mStarsView.layout(stars_x,stars_y,stars_x+mStarsView.getMeasuredWidth(),stars_y+mStarsView.getMeasuredHeight());
    mInformerView.layout(stars_x-mInformerView.getMeasuredWidth(),stars_y,stars_x,stars_y+mInformerView.getMeasuredHeight());
  }
  //---------------------------------------------------------------------------
  public void setBlock(boolean block) {
    mInformerView.setBlock(block);
    mStarsView.setBlock(block);
  }
  //---------------------------------------------------------------------------
  public void release() {
    mStarsView.release();
    mInformerView.release();
  }
  //---------------------------------------------------------------------------
}

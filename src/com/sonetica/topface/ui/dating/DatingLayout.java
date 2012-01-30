package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DatingLayout extends ViewGroup {
  // Data
  ImageView    mImageView;
  StarsView    mStarsView;
  InformerView mInformerView;
  private int  mHeaderOffset = 50;
  //---------------------------------------------------------------------------
  public DatingLayout(Context context) {
    super(context);
    
    // Baby
    mImageView = new ImageView(context);
    mImageView.setImageResource(R.drawable.im_red_informer);
    addView(mImageView);

    // Profile, chat
    mInformerView = new InformerView(context);
    addView(mInformerView);
    
    // Stars
    mStarsView = new StarsView(context,mInformerView);
    addView(mStarsView);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    
    int count = getChildCount();
    for(int i=0;i<count;i++)
      getChildAt(i).measure(0,0);
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    mImageView.layout(left,top,right,bottom);
    
    // оффсет смещения для хедера
    int offset = top + mHeaderOffset;
    
    int stars_x = right - mStarsView.getMeasuredWidth();
    int stars_y = offset + top;
    mStarsView.layout(stars_x,stars_y,right,bottom);
    mInformerView.layout(stars_x - mInformerView.getMeasuredWidth(),stars_y,stars_x,bottom);
  }
  //---------------------------------------------------------------------------
}

package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DatingLayout extends ViewGroup {
  // Data
  public  ImageView    mImageView;
  private StarsView    mStarsView;
  private InformerView mInformerView;
  private int  mHeaderOffset = 60;
  //---------------------------------------------------------------------------
  public DatingLayout(Context context) {
    super(context);

    setBackgroundColor(Color.GRAY);
    
    // Baby
    mImageView = new ImageView(context);
    mImageView.setImageResource(R.drawable.im_red_informer);
    addView(mImageView);

    // Informer, profile, chat
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
    
    int z = MeasureSpec.getSize(heightMeasureSpec);
    
    int count = getChildCount();
    for(int i=0;i<count;i++)
      getChildAt(i).measure(0,MeasureSpec.getSize(heightMeasureSpec)-mHeaderOffset); // HEADER OFFSET !!!!!
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    mImageView.layout(left,top,right,bottom);

    int stars_x = getMeasuredWidth() - mStarsView.getMeasuredWidth();
    int stars_y = mHeaderOffset;
    
    mStarsView.layout(stars_x,stars_y,stars_x+mStarsView.getMeasuredWidth(),stars_y+mStarsView.getMeasuredHeight());
    mInformerView.layout(stars_x-mInformerView.getMeasuredWidth(),stars_y,stars_x,stars_y+mInformerView.getMeasuredHeight());
  }
  //---------------------------------------------------------------------------
}

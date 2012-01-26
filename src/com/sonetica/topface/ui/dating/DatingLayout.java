package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DatingLayout extends ViewGroup {
  // Data
  ImageView mImageView;
  StarsView mStarsView;
  InformerView mInformerView;
  private int mOffset = 30;
  //---------------------------------------------------------------------------
  public DatingLayout(Context context) {
    super(context);
    
    setBackgroundColor(Color.YELLOW);
    
    mImageView = new ImageView(context);
    mImageView.setImageResource(R.drawable.im_red_informer);
    addView(mImageView);

    mInformerView = new InformerView(context);
    addView(mInformerView);
    
    mStarsView = new StarsView(context,mInformerView);
    addView(mStarsView);
    
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onLayout(boolean changed,int l,int t,int r,int b) {
    Debug.log(this,">DL_0 onLayout,l:"+l+" t:"+t+" r:"+r+" b:"+b);
    mImageView.layout(l,t,r,b);
    t = t + mOffset;
    mStarsView.layout(r-mStarsView.I,t,r,b);
    int z = 80;
    mInformerView.layout(z,t,z+100,b);
    Debug.log(this,">DL_1 onLayout,l:"+l+" t:"+t+" r:"+r+" b:"+b);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    Debug.log(this,">DL_2 onMeasure,");
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    int width  = MeasureSpec.getSize(widthMeasureSpec);
    int height = MeasureSpec.getSize(heightMeasureSpec);
    Debug.log(this,">DL_1 onMeasure, w:"+width+" h:"+height);
  }
  //---------------------------------------------------------------------------
}

package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import com.sonetica.topface.ui.Jopa;
import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DatingLayout extends ViewGroup {
  // Data
  ImageView mImageView;
  StarsView mStarsView;
  Jopa _jopa;
  InformerView mInformerView;
  private int mHeaderOffset = 50;
  //---------------------------------------------------------------------------
  public DatingLayout(Context context) {
    super(context);
    
    setBackgroundColor(Color.YELLOW);
    
    _jopa = new Jopa(context);
    addView(_jopa);
    
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
    mImageView.layout(l,t,r,b);
    
    // офсет смещения для хедера
    int offset = t + mHeaderOffset;
    
    _jopa.layout(0,0,_jopa.getMeasuredWidth(),_jopa.getMeasuredHeight());
    
    int x = r-mStarsView.getWidthEx();
    mStarsView.layout(x,offset+10,r,b);
    
    int x0 = x - mInformerView.getWidthEx(); 
    mInformerView.layout(x0,offset,x,b);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    
    int count = getChildCount();
    for(int i=0;i<count;i++)
      getChildAt(i).measure(100,100);
    
  }
  //---------------------------------------------------------------------------
}

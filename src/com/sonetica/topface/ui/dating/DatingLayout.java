package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class DatingLayout extends ViewGroup implements View.OnClickListener {
  // Data
  public  ImageView    mImageView;
  private StarsView    mStarsView;
  private InformerView mInformerView;
  private View mHeaderBar;
  private int  mHeaderOffset;
  //---------------------------------------------------------------------------
  public DatingLayout(Context context) {
    super(context);

    setOnClickListener(this);
    setBackgroundColor(Color.BLACK);
    
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
    
    mHeaderBar = DatingActivity.mHeaderBar;
    mHeaderOffset = DatingActivity.HEADER_HEIGHT;
    
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
  //-------------------------------------------------------------------------
  public void onProfileBtnClick() {
    ((DatingGallery)getParent()).onProfileBtnClick();
  }
  //---------------------------------------------------------------------------
  public void onChatBtnClick() {
    ((DatingGallery)getParent()).onChatBtnClick();
  }
  //---------------------------------------------------------------------------
  public void onRate(int index) {
    ((DatingGallery)getParent()).onRate(index);
  }
  //---------------------------------------------------------------------------
  public void hideChildren(int visibility) {
    mStarsView.setVisibility(visibility);
    mInformerView.setVisibility(visibility);
    mHeaderBar.setVisibility(visibility);
  }
  //---------------------------------------------------------------------------
  public void hideChildren() {
    int visibility = mStarsView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE ;
    mStarsView.setVisibility(visibility);
    visibility = mInformerView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE ;
    mInformerView.setVisibility(visibility);
    visibility = mHeaderBar.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE ;
    mHeaderBar.setVisibility(visibility);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View v) {
    hideChildren();
  }
  //---------------------------------------------------------------------------
}

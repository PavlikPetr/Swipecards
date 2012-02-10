package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DatingLayout extends ViewGroup implements View.OnClickListener {
  // Data
  public  ImageView     mFaceView;     // оцениваемая фотография
  public  InfoView      mInfoView;     // информация о пользователе
  public  ResourcesView mResView;     // ресурсы
  private StarsView     mStarsView;    // контрол со звездами для оценки
  private InformerView  mInformerView; // контрол с всплывающей панелей и 2 кнопками(чат,профиль)
  public ProgressBar    mProgress;
  // 
  private View mHeaderBar;
  private int  mHeaderOffset;
  //private TextView mPopupView;  // оценка по центру
  //---------------------------------------------------------------------------
  public DatingLayout(Context context) {
    super(context);

    setOnClickListener(this);
    setBackgroundColor(Color.TRANSPARENT);
    
    // Baby
    mFaceView = new ImageView(context);
    mFaceView.setImageResource(R.drawable.im_black_square);
    addView(mFaceView);
    
    // Info
    mInfoView = new InfoView(context);
    addView(mInfoView);

    // Informer, profile, chat
    mInformerView = new InformerView(context);
    addView(mInformerView);
    
    mResView = new ResourcesView(context);
    addView(mResView);

    /*
    // Popup
    mPopupView = new TextView(context);
    mPopupView.setBackgroundResource(R.drawable.dating_popup);
    mPopupView.setVisibility(View.INVISIBLE);
    mPopupView.setGravity(Gravity.CENTER);
    addView(mPopupView);
    */
    
    // Stars
    mStarsView = new StarsView(context,mInformerView);
    addView(mStarsView);
    
    mProgress = new ProgressBar(context);
    mProgress.setVisibility(View.INVISIBLE);
    addView(mProgress);

  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    
    // ПОПРАВИТЬ !!!!
    mHeaderBar    = DatingActivity.mHeaderBar;
    mHeaderOffset = DatingActivity.mHeaderBar.getHeight();
    
    int count = getChildCount();
    for(int i=0;i<count;i++)
      getChildAt(i).measure(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec)-mHeaderOffset); // HEADER OFFSET !!!!!
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    mFaceView.layout(left,top,right,bottom);

    int stars_x = getMeasuredWidth() - mStarsView.getMeasuredWidth();
    int stars_y = mHeaderOffset;
    
    mStarsView.layout(stars_x,stars_y,stars_x+mStarsView.getMeasuredWidth(),stars_y+mStarsView.getMeasuredHeight());
    mInformerView.layout(stars_x-mInformerView.getMeasuredWidth(),stars_y,stars_x,stars_y+mInformerView.getMeasuredHeight());
    mInfoView.layout(0,stars_y,mInfoView.getMeasuredWidth(),stars_y+mInfoView.getMeasuredHeight());
    /*
    stars_x = (getMeasuredWidth() - mPopupView.getMeasuredWidth())/2;
    stars_y = (getMeasuredHeight() - mPopupView.getMeasuredHeight())/2;
    mPopupView.layout(stars_x,stars_y,stars_x+mPopupView.getMeasuredWidth(),stars_y+mPopupView.getMeasuredHeight());
    */
    stars_x = (getMeasuredWidth() - mProgress.getMeasuredWidth())/2;
    stars_y = (getMeasuredHeight() - mProgress.getMeasuredHeight())/2;
    mProgress.layout(stars_x,stars_y,stars_x+mProgress.getMeasuredWidth(),stars_y+mProgress.getMeasuredHeight());
    
    mResView.layout(0,mHeaderOffset,0+mResView.getMeasuredWidth(),mHeaderOffset+mResView.getMeasuredHeight());
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
    mInfoView.setVisibility(visibility);
  }
  //---------------------------------------------------------------------------
  public void hideChildren() {
    int visibility = mStarsView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE ;
    mStarsView.setVisibility(visibility);
    visibility = mInformerView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE ;
    mInformerView.setVisibility(visibility);
    visibility = mHeaderBar.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE ;
    mHeaderBar.setVisibility(visibility);
    visibility = mInfoView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE ;
    mInfoView.setVisibility(visibility);
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View v) {
    hideChildren();
  }
  //---------------------------------------------------------------------------
}

package com.sonetica.topface.ui.dating;

import java.util.LinkedList;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.ui.dating.DatingActivity;
import com.sonetica.topface.ui.dating.RateControl;
import com.sonetica.topface.ui.dating.ResourcesView;
import com.sonetica.topface.ui.dating.FaceView;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;

/*    ЛАПША КОД - СКОРОСТЬ превыше КАЧЕСТВА
 *    
 */
public class DatingControl extends ViewGroup {
  // Data
  private int mPosition;
  private boolean mStart;
  private DatingGallery mDatingGallery;
  private DatingGalleryAdapter mGalleryAdapter;
  // 
  private FaceView mFaceView;
  private ResourcesView mResView;
  private RateControl mRateControl;
  private Button mBackButton;
  private LinkedList<SearchUser> mDataList;
  //---------------------------------------------------------------------------
  public DatingControl(Context context,AttributeSet attrs) {
    super(context,attrs);
    
    mStart = false;
    mDataList = new LinkedList<SearchUser>();
    
    // Adapter
    mGalleryAdapter = new DatingGalleryAdapter(context);
    mGalleryAdapter.registerDataSetObserver(new DataSetObserver() {
      @Override
      public void onChanged() {
        mFaceView.setVisibility(View.VISIBLE);
      }
      @Override
      public void onInvalidated() {
      }
    });
    
    // Gallery
    mDatingGallery = new DatingGallery(context);
    mDatingGallery.setAdapter(mGalleryAdapter);
    mDatingGallery.setSpacing(0);
    mDatingGallery.setFadingEdgeLength(0);
    
    // Hide controls
    mDatingGallery.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> arg0,View arg1,int position,long arg3) {
        if(position==0)
          hideAllControls();
        else
          hideBack();
      }
    });
    addView(mDatingGallery);
    
    mBackButton = new Button(context);
    mBackButton.setText("Back");
    mBackButton.setVisibility(View.INVISIBLE);
    mBackButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mDatingGallery.setSelection(0,true);
        hideAllControls();
      }
    });
    addView(mBackButton);
    
    // Info
    mFaceView = new FaceView(context);
    mFaceView.setVisibility(View.INVISIBLE);
    addView(mFaceView);
    
    // Power and Money
    mResView = new ResourcesView(context);
    addView(mResView);
    
    // Rate Control
    mRateControl = new RateControl(context);
    addView(mRateControl);

  }
  //---------------------------------------------------------------------------
  @Override
  protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    
    int count = getChildCount();
    for(int i=0;i<count;i++)
      getChildAt(i).measure(widthMeasureSpec,heightMeasureSpec);
    
    mBackButton.measure(0,0);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onLayout(boolean changed,int left,int top,int right,int bottom) {
    mFaceView.layout(left,top,right,bottom);
    
    int offset_y = DatingActivity.mHeaderBar.getHeight();
    
    mBackButton.layout(0,100,mBackButton.getMeasuredWidth(),100+mBackButton.getMeasuredHeight());
    
    mDatingGallery.layout(0,0,mDatingGallery.getMeasuredWidth(),mDatingGallery.getMeasuredHeight());
    
    mResView.layout(0,offset_y,mResView.getMeasuredWidth(),offset_y+mResView.getMeasuredHeight());
    mRateControl.layout(0,offset_y,mRateControl.getMeasuredWidth(),offset_y+mRateControl.getMeasuredHeight());
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<SearchUser> dataList) {
    mDataList.addAll(dataList);
    if(!mStart) {
      mStart=true;
      next();
    }
  }
  //---------------------------------------------------------------------------
  public void rate() {
    next();
  }
  //---------------------------------------------------------------------------
  public void next() {
    SearchUser user = mDataList.get(++mPosition);
    
    mFaceView.age    = user.age;
    mFaceView.city   = user.city_name;
    mFaceView.name   = user.first_name;
    mFaceView.online = user.online;
    mFaceView.status = user.status;

    mDatingGallery.setSelection(0);
    mGalleryAdapter.setUserData(user);
    mGalleryAdapter.notifyDataSetChanged();
  }
  //---------------------------------------------------------------------------
  public int getUserId() {
    return mDataList.get(mPosition).uid;
  }
  //---------------------------------------------------------------------------
  public void hideBack() {
    int visibility=DatingActivity.mHeaderBar.getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE;
    DatingActivity.mHeaderBar.setVisibility(visibility);
    
    visibility=mBackButton.getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE;
    mBackButton.setVisibility(visibility);
  }
  //---------------------------------------------------------------------------
  public void hideAllControls() {
    int visibility=mFaceView.getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE;
    mFaceView.setVisibility(visibility);
    
    visibility=mResView.getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE;
    mResView.setVisibility(visibility);
    
    visibility=mRateControl.getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE;
    mRateControl.setVisibility(visibility);
    
    visibility=DatingActivity.mHeaderBar.getVisibility()==View.VISIBLE?View.INVISIBLE:View.VISIBLE;
    DatingActivity.mHeaderBar.setVisibility(visibility);
    
    if(mBackButton.getVisibility()==View.VISIBLE);
      mBackButton.setVisibility(View.INVISIBLE);
  }
  //---------------------------------------------------------------------------
}

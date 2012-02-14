package com.sonetica.topface.ui.dating;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class DatingGallery extends BaseGallery {
  // Data
  private DatingLayout mDatingLayout;  // фото с контролом для оценки
  private ImageView    mFirstView;     // первая дополнительная фотография
  private ImageView    mSecondView;    // вторая дополнительная фотография
  private LinkedList<SearchUser> mSearchUserList;  // список плользователей для оценивания
  private SearchUser mUser;   // текущий поьзователь
  private int curr_id = -1;
  //---------------------------------------------------------------------------
  public DatingGallery(Context context,AttributeSet attrs) {
    super(context,attrs);

    mSearchUserList = new LinkedList<SearchUser>();
    mDatingLayout   = new DatingLayout(context);
    addView(mDatingLayout);
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<SearchUser> userList) {
    //mSearchUserList = userList;
    mSearchUserList.addAll(userList);
    if(curr_id == -1)
      next();
  }
  //---------------------------------------------------------------------------
  public void next() {
    DatingActivity.mPaintView.setVisibility(View.INVISIBLE);
    mDatingLayout.progress(View.VISIBLE);
    mDatingLayout.setFaceVisibility(View.INVISIBLE);

    if(mFirstView!=null ) {
      removeView(mFirstView);
      mFirstView = null;
    }
    if(mSecondView!=null) {
      removeView(mSecondView);
      mSecondView = null;
    }
    
    mUser = getUser();
    
    DatingActivity.mPaintView.mCountPoints = mUser.avatars_big.length>=3?3:mUser.avatars_big.length;
    
    new Thread(new Runnable() {
      @Override
      public void run() {
        Bitmap bitmap0 = Http.bitmapLoader(mUser.avatars_big[0]);
        if(bitmap0==null)
          DatingGallery.this.post(new Runnable() {
            @Override
            public void run() {
              mDatingLayout.setImageData(BitmapFactory.decodeResource(DatingGallery.this.getResources(),R.drawable.icon_people),mUser);
              mDatingLayout.progress(View.INVISIBLE);
            }
          });
        
        bitmap0 = cutting(bitmap0);
        Bitmap bitmap1 = null;
        Bitmap bitmap2 = null;
        
        if(mUser.avatars_big.length > 1) {
          bitmap1 = Http.bitmapLoader(mUser.avatars_big[1]);
          mFirstView = new ImageView(DatingGallery.this.getContext());
        }
        
        if(mUser.avatars_big.length > 2) {
          bitmap2 = Http.bitmapLoader(mUser.avatars_big[2]);
          mSecondView = new ImageView(DatingGallery.this.getContext());
        }

        final Bitmap bmp0 = bitmap0;
        final Bitmap bmp1 = bitmap1;
        final Bitmap bmp2 = bitmap2;
        
        // UI update
        DatingGallery.this.post(new Runnable() {
          @Override
          public void run() {
            mDatingLayout.setImageData(bmp0,mUser);
            if(mFirstView!=null && bmp1!=null) {
              mFirstView.setImageBitmap(bmp1);
              DatingGallery.this.addView(mFirstView);
            }
            if(mSecondView!=null && bmp2!=null) {
              mSecondView.setImageBitmap(bmp2);
              DatingGallery.this.addView(mSecondView);
            }
            DatingActivity.mPaintView.setVisibility(View.VISIBLE);
            mDatingLayout.setFaceVisibility(View.VISIBLE);
            mDatingLayout.progress(View.INVISIBLE);
          }
        });
      }
    },"rate - update").start();
  }
  //---------------------------------------------------------------------------
  public int getUserId() {
    return mUser.uid;
  }
  //---------------------------------------------------------------------------
  public SearchUser getUser() {
    if(++curr_id>=mSearchUserList.size())
      ((DatingActivity)getContext()).update();
    
    SearchUser user = mSearchUserList.get(curr_id);
    
    if(curr_id == mSearchUserList.size()-5)
      ((DatingActivity)getContext()).update();
    
    return user;
  }
  //---------------------------------------------------------------------------
  private Bitmap cutting(Bitmap bitmap) {
    Bitmap clippedBitmap = null;
    Bitmap scaledBitmap  = null;
    try {
      int width  = getWidth();
      int height = getHeight();
  
      int w = bitmap.getWidth();
      int h = bitmap.getHeight();
  
      boolean LEG = false;
      if(w >= h) LEG = true;
      float ratio = Math.max(((float) width) / w, ((float) height) / h);
  
      Matrix matrix = new Matrix();
      matrix.postScale(ratio,ratio);
  
      scaledBitmap = Bitmap.createBitmap(bitmap,0,0,w,h,matrix,true);
  
      if(LEG) {
        int offset_x = (scaledBitmap.getWidth()-width)/2;
        clippedBitmap = Bitmap.createBitmap(scaledBitmap,offset_x,0,width,height,null,false);
      } else
        clippedBitmap = Bitmap.createBitmap(scaledBitmap,0,0,width,height,null,false);
    } catch (Exception e) {
      if(scaledBitmap != null)  scaledBitmap.recycle();
      if(clippedBitmap != null) clippedBitmap.recycle();
      
      return bitmap;
    }
    if(bitmap != null) bitmap.recycle();
    if(scaledBitmap != null)  scaledBitmap.recycle();
    
    return clippedBitmap;
  }
  //---------------------------------------------------------------------------
  @Override
  public void currentScreen(int whichScreen) {
    DatingActivity.mPaintView.mCurrentPoint = whichScreen;
  }
  //---------------------------------------------------------------------------
  public void release() {
    mDatingLayout.release();
    mDatingLayout = null;
    mFirstView  = null;
    mSecondView = null;
  }
  //---------------------------------------------------------------------------
}

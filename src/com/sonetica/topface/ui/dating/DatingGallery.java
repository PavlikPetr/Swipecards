package com.sonetica.topface.ui.dating;

import java.util.LinkedList;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class DatingGallery extends BaseGallery {
  // Data
  private DatingLayout mDatingLayout;
  private ImageView    mFirstView;
  private ImageView    mSecondView;
  private LinkedList<SearchUser> mSearchUserList;
  private SearchUser mUser;
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
    
    new Thread(new Runnable() {
      @Override
      public void run() {
        Bitmap bitmap0 = cutting(Http.bitmapLoader(mUser.avatars_big[0]));
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
            mDatingLayout.setFaceVisibility(View.VISIBLE);
            mDatingLayout.progress(View.INVISIBLE);
          }
        });
      }
    }).start();
  }
  //---------------------------------------------------------------------------
  public int getUserId() {
    return mUser.uid;
  }  
  //---------------------------------------------------------------------------
  public SearchUser getUser() {
    SearchUser user = mSearchUserList.get(++curr_id);
    
    if(curr_id == mSearchUserList.size()-5)
      ((DatingActivity)getContext()).update();
    
    return user;
  }
  //---------------------------------------------------------------------------
  private Bitmap cutting(Bitmap bitmap) {
    int width  = getWidth();
    int height = getHeight();

    int w = bitmap.getWidth();
    int h = bitmap.getHeight();

    boolean LEG = false;
    if(w >= h) LEG = true;
    float ratio = Math.max(((float) width) / w, ((float) height) / h);

    Matrix matrix = new Matrix();
    matrix.postScale(ratio,ratio);

    Bitmap scaledBitmap = Bitmap.createBitmap(bitmap,0,0,w,h,matrix,true);

    final Bitmap clippedBitmap;
    if(LEG) {
      int offset_x = (scaledBitmap.getWidth()-width)/2;
      clippedBitmap = Bitmap.createBitmap(scaledBitmap,offset_x,0,width,height,null,false);
    } else
      clippedBitmap = Bitmap.createBitmap(scaledBitmap,0,0,width,height,null,false);
    
    return clippedBitmap;
  }
  //---------------------------------------------------------------------------
}

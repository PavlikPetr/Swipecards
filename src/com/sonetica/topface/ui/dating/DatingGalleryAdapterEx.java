package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.LeaksManager;
import com.sonetica.topface.utils.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class DatingGalleryAdapterEx extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  //---------------------------------------------------------------------------
  static class ViewHolder {
    ImageView mImageView;
  };
  //---------------------------------------------------------------------------
  // Data
  private int mPrevPosition = -1; // предыдущая позиция в галерее
  private int mPrePosition  = -1; // пред загрузка позиция
  private Bitmap mRateBitmap;     // оцениваемое фото
  private Bitmap mPreBitmap;      // пред загрузка фото
  private SearchUser mUserData;               
  private LayoutInflater  mInflater;          
  private DatingControl   mDatingControl;
  private AlphaAnimation  mAlphaAnimation;
  //---------------------------------------------------------------------------
  public DatingGalleryAdapterEx(Context context,DatingControl datingControl) {
    mInflater = LayoutInflater.from(context);
    mDatingControl  = datingControl;
    mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
    mAlphaAnimation.setDuration(200L);
  }
  //---------------------------------------------------------------------------
  public void setUserData(SearchUser user) {
    mPrevPosition = -1;
    mPrePosition  = -1;
    mRateBitmap = null;
    mUserData = user;
  }
  //---------------------------------------------------------------------------
  public int getCount() {
    if(mUserData==null)
      return 0;
    return mUserData.avatars_big.length;
  }
  //---------------------------------------------------------------------------
  public Object getItem(int position) {
    if(mUserData==null)
      return null;
    return mUserData.avatars_big[position];
  }
  //---------------------------------------------------------------------------
  public long getItemId(int position) {
    if(mUserData==null)
      return 0;
    return position;
  }
  //---------------------------------------------------------------------------
  public View getView(final int position,View convertView, ViewGroup parent) {
    ViewHolder holder = null;
    if(convertView==null) {
      holder = new ViewHolder();
      convertView = (ViewGroup)mInflater.inflate(R.layout.album_item_gallery, null, false);
      convertView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
      holder.mImageView = (ImageView)convertView.findViewById(R.id.ivPreView);
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();
    
    if(mUserData!=null) {
      if(position==0 && mRateBitmap!=null) {
        holder.mImageView.setImageBitmap(mRateBitmap);
      } else {
        // текущее фото
        if(position==mPrePosition && mPreBitmap!=null)
          holder.mImageView.setImageBitmap(mPreBitmap);
        else
          loadingImage(position, holder.mImageView);
        // следующее фото
        mPrePosition = position>mPrevPosition ? position+1 : position-1;   // в какую сторону идем
        if(mPrePosition>=0 && mPrePosition<=(mUserData.avatars_big.length-1)) {  // если не на границе
          preLoading(mPrePosition, holder.mImageView.getWidth(), holder.mImageView.getHeight());
        }
      }
      mPrevPosition = position;
    }
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  public void loadingImage(final int position,final ImageView view) {
    Thread t = new Thread() {
      @Override
      public void run() {
        Bitmap rawBitmap  = null;
        Bitmap clipBitmap = null;
        
        if(position==0 && mRateBitmap==null) {
          rawBitmap = Http.bitmapLoader(mUserData.avatars_big[position]);
          if(rawBitmap!=null) {
            clipBitmap  = Utils.clipping(rawBitmap,view.getWidth(),view.getHeight());
            mRateBitmap = clipBitmap;
          }
        } else if(position==0 && mRateBitmap!=null) {
          clipBitmap = mRateBitmap;
        } else {
          rawBitmap = Http.bitmapLoader(mUserData.avatars_big[position]);
          if(rawBitmap!=null)
            clipBitmap = Utils.clipping(rawBitmap,view.getWidth(),view.getHeight());
        }
        
        final Bitmap bitmap = clipBitmap;
        
        view.post(new Runnable() {
          @Override
          public void run() {
            if(bitmap == null) {
              view.setImageResource(R.drawable.icon_people);
              mDatingControl.controlVisibility(DatingControl.V_SHOW_INFO);
              return;
            }
            if(position==0) {
              view.setAlpha(255);
              view.setImageBitmap(bitmap);
              view.startAnimation(mAlphaAnimation);
              mDatingControl.controlVisibility(DatingControl.V_SHOW_INFO);
            } else
              view.setImageBitmap(bitmap);
          }
        });
      }
    };
    LeaksManager.getInstance().monitorObject(t);
    t.start();
  }
  //---------------------------------------------------------------------------
  public void preLoading(final int position,final int w,final int h) {
    new Thread() {
      @Override
      public void run() {
        Bitmap rawBitmap = Http.bitmapLoader(mUserData.avatars_big[position]);
        if(rawBitmap!=null) {
          mPreBitmap = Utils.clipping(rawBitmap,w,h);
        }
      }
    }.start();
  }
  //---------------------------------------------------------------------------
  public void release() {
    if(mRateBitmap!=null) {
      mRateBitmap.recycle();
      mRateBitmap = null;
    }
    if(mPreBitmap!=null) {
      mPreBitmap.recycle();
      mPreBitmap = null;
    }
  }
  //---------------------------------------------------------------------------
}

//  Http.imageLoaderExp(url,view);

/*
// утечка памяти при работе с пулами
  mThreadsPool.execute(new Thread() {
    @Override
    public void run() {
      final Bitmap bitmap = Http.bitmapLoader(url);
      if(bitmap != null)
        view.post(new Runnable() {
          @Override
          public void run() {
            if(position==0) {
              view.setAlpha(255);
              view.setImageBitmap(bitmap);
              view.startAnimation(mAlphaAnimation);
              mDatingControl.controlVisibility(DatingControl.V_INFO);
            } else
              view.setImageBitmap(bitmap);
          }
        });
    }
  });
*/
package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.Http;
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

public class DatingGalleryAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  //---------------------------------------------------------------------------
  static class ViewHolder {
    ImageView mImageView;
  };
  //---------------------------------------------------------------------------
  // Data
  private SearchUser mUserData;
  private LayoutInflater  mInflater;
  private DatingControl   mDatingControl;
  private AlphaAnimation  mAlphaAnimation;
  //---------------------------------------------------------------------------
  public DatingGalleryAdapter(Context context,DatingControl datingControl) {
    mInflater = LayoutInflater.from(context);
    mDatingControl = datingControl;
    mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
    mAlphaAnimation.setDuration(200L);
  }
  //---------------------------------------------------------------------------
  public void setUserData(SearchUser user) {
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
    
    if(mUserData!=null)
      loadingImage(position,(String)getItem(position), holder.mImageView);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  public void loadingImage(final int position,final String url,final ImageView view) {
    //Http.imageLoaderExp(url,view);
    //Debug.log(this,"LO:"+url+",view:"+ view.toString());
    
    new Thread() {
      @Override
      public void run() {
        Bitmap rawBitmap = Http.bitmapLoader(url);
        final Bitmap bitmap = Utils.clipping(rawBitmap,view.getWidth(),view.getHeight());
        if(bitmap != null)
          view.post(new Runnable() {
            @Override
            public void run() {
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
    }.start();
  }
  //---------------------------------------------------------------------------
}

/*
  // утечка памяти в данной реализации
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
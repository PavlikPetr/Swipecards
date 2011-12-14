package com.sonetica.topface.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;
import com.sonetica.topface.R;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.ui.tops.TopButton;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;

/*
 *  Менеджер изображений закачивает, сторит и выдает через CacheManager
 *  предназанчен для окон "Топы" и "Я нравлюсь" 
 */
public class GalleryCachedManager {
  // Data
  public int mBitmapWidth;
  public int mBitmapHeight;
  private ArrayList<TopUser> mUserList;
  private HashMap<TopButton,Integer> mLinkCache;
  private ExecutorService mThreadsPool;
  private AbstractCache mBitmapCache;
  //---------------------------------------------------------------------------
  public GalleryCachedManager(Context context,IFrame frame,ArrayList<TopUser> userList) {
    mUserList = userList;
    mThreadsPool = Executors.newFixedThreadPool(4);
    mLinkCache = new HashMap<TopButton,Integer>();
    mBitmapCache = CacheManager.getCache(frame);
    mBitmapCache.release();
    mBitmapWidth  = Device.getDisplay(context).getWidth()/4;
    mBitmapHeight = (int)(mBitmapWidth * 1.25);
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,TopButton imageView) {
    mLinkCache.put(imageView,position);
    Bitmap bitmap = mBitmapCache.get(position);
    if(bitmap!=null) {
      imageView.setImageBitmap(bitmap);
    } else {
      setImageToQueue(new Pair<TopButton,Integer>(imageView,position));
      imageView.setImageResource(R.drawable.im_black_square);
    }
  }
  //---------------------------------------------------------------------------
  public TopUser get(int index) {
    return mUserList.get(index);
  }
  //---------------------------------------------------------------------------
  private void setImageToQueue(final Pair<TopButton,Integer> data) {
    mThreadsPool.execute(new Runnable() {
      @Override
      public void run() {
        if(imageViewReused(data))
          return;
        // закачка
        Bitmap rawBitmap = Http.bitmapLoader(mUserList.get(data.second).photo);
        if(rawBitmap==null)
          return;
        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap,mBitmapWidth,mBitmapHeight,false);
        if(imageViewReused(data))
          return;
        // изменение размера и запись
        mBitmapCache.put(data.second,scaledBitmap,mUserList.get(data.second).photo);
        // отрисовка
        data.first.post(new Runnable() {
          @Override
          public void run() {
            if(imageViewReused( data))
              return;
            if(scaledBitmap!=null)
              data.first.setImageBitmap(mBitmapCache.get(data.second));
            else
              data.first.setImageResource(R.drawable.im_black_square);
          }
        });
      }
    });
  }
  //-------------------------------------------------------------------------
  boolean imageViewReused(Pair<TopButton,Integer> data){
    int index=mLinkCache.get(data.first);
    if(index!=data.second)
      return true;
    return false;
  }
  //---------------------------------------------------------------------------
  public int size() {
    return mUserList.size();
  }
  //---------------------------------------------------------------------------
  public void release() {
    mBitmapCache.release();
  }
//---------------------------------------------------------------------------
}
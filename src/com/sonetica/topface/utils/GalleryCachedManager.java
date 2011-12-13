package com.sonetica.topface.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.*;
import com.sonetica.topface.R;
import com.sonetica.topface.data.User;
import com.sonetica.topface.net.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;
import android.widget.AbsListView;
import android.widget.ImageView;

/*
 *  Менеджер изображений закачивает, сторит и выдает через CacheManager
 *  предназанчен для окон "Топы" и "Я нравлюсь" 
 */
public class GalleryCachedManager implements AbsListView.OnScrollListener {
  // Data
  private ArrayList<User> mFullUserList;
  private HashMap<ImageView,Integer> mLinkCache;
  private ExecutorService mThreadsPool;
  private AbstractCache mCache;
  //---------------------------------------------------------------------------
  public GalleryCachedManager(Context context,IFrame frame,ArrayList<User> userList) {
    mFullUserList = userList;
    mThreadsPool  = Executors.newFixedThreadPool(4);
    mLinkCache    = new HashMap<ImageView,Integer>();
    mCache = CacheManager.getCache(frame);
    mCache.release();
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,ImageView imageView) {
    mLinkCache.put(imageView,position);
    Bitmap bitmap = mCache.get(position);
    if(bitmap!=null) {
      imageView.setImageBitmap(bitmap);
    } else {
      setImageToQueue(new Pair<ImageView,Integer>(imageView,position));
      imageView.setImageResource(R.drawable.im_black_square);
    }
  }
  //---------------------------------------------------------------------------
  private void setImageToQueue(final Pair<ImageView,Integer> data) {
    mThreadsPool.execute(new Runnable() {
      @Override
      public void run() {
        if(imageViewReused(data))
          return;
        // закачка
        Bitmap bitmap = Http.bitmapLoader(mFullUserList.get(data.second).photo);
        if(bitmap==null)
          return;
        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,80,100,false); //120-140
        if(imageViewReused(data))
          return;
        // изменение размера и запись
        mCache.put(data.second,scaledBitmap,mFullUserList.get(data.second).photo);
        // отрисовка
        data.first.post(new Runnable() {
          @Override
          public void run() {
            if(imageViewReused( data))
              return;
            if(scaledBitmap!=null)
              data.first.setImageBitmap(mCache.get(data.second));
            else
              data.first.setImageResource(R.drawable.im_black_square);
          }
        });
      }
    });
  }
  //-------------------------------------------------------------------------
  boolean imageViewReused(Pair<ImageView,Integer> data){
      int index=mLinkCache.get(data.first);
      if(index!=data.second)
          return true;
      return false;
  }
  //---------------------------------------------------------------------------
  public int getSize() {
    return mFullUserList.size();
  }
  //---------------------------------------------------------------------------
  public void release() {
    mThreadsPool.shutdown();
    mCache.release();
    mLinkCache.clear();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScrollStateChanged(AbsListView view,int scrollState) {
  }
  //---------------------------------------------------------------------------
}
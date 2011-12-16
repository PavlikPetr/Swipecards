package com.sonetica.topface.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.net.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/*
 *  Менеджер изображений закачивает и сохраняет в карте до закрытия
 *  Не использует CacheManager
 */
public class GalleryManager {
  // Data
  private ArrayList<String> mUrlList;
  private HashMap<Integer,Bitmap> mCache;
  private ExecutorService mThreadPool;
  private int mThreadCount;
  private byte[] states;
  //---------------------------------------------------------------------------
  public GalleryManager(Context context,ArrayList<String> urlList,int threadCount) {
    mUrlList     = urlList;
    mThreadCount = threadCount;
    mThreadPool  = Executors.newFixedThreadPool(mThreadCount);
    mCache       = new HashMap<Integer,Bitmap>();
    states = new byte[urlList.size()];
    Arrays.fill(states,(byte)0);
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position, final ImageView view) {
    if(mCache.containsKey(position))
      view.setImageBitmap(mCache.get(position));
    else {
      if(states[position]==1)
        return;
      mThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          states[position] = 1;
          Bitmap rawBitmap = Http.bitmapLoader(mUrlList.get(position));
          if(rawBitmap==null)
            return;
          //Scaled
          final Bitmap scaledBitmap = rawBitmap;
          view.post(new Runnable() {
            @Override
            public void run() {
              view.setImageBitmap(scaledBitmap);
            }
          });
          mCache.put(position,scaledBitmap);
        }
      });
    }
  }
  //---------------------------------------------------------------------------
  public int size() {
    return mUrlList.size();
  }
  //---------------------------------------------------------------------------
  public void preload(final int position) {
    if(position>=mUrlList.size())
      return;
    if(states[position]==1)
      return;
    mThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        states[position]=1;
        final Bitmap bitmap = Http.bitmapLoader(mUrlList.get(position));
        if(bitmap==null)
          return;
        mCache.put(position,bitmap);
      }
    });
  }
  //---------------------------------------------------------------------------
  public void release() {
    mThreadPool.shutdown();
    for(Bitmap bitmap : mCache.values())
      bitmap.recycle();
    mCache.clear();
  }
  //---------------------------------------------------------------------------
}





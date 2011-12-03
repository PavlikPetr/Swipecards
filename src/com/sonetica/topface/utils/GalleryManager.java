package com.sonetica.topface.utils;

import java.util.ArrayList;
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
  //---------------------------------------------------------------------------
  public GalleryManager(Context context,ArrayList<String> urlList,int threadCount) {
    mUrlList     = urlList;
    mThreadCount = threadCount;
    mThreadPool  = Executors.newFixedThreadPool(mThreadCount);
    mCache       = new HashMap<Integer,Bitmap>();
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position, final ImageView view) {
    if(mCache.containsKey(position)) {
      view.setImageBitmap(mCache.get(position));
    } else {
      mThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          final Bitmap bitmap = Http.bitmapLoader(mUrlList.get(position));
          if(bitmap==null)
            return;
          view.post(new Runnable() {
            @Override
            public void run() {
              view.setImageBitmap(bitmap);
              view.invalidate();
            }
          });
          mCache.put(position,bitmap);
        }
      });
    }
  }
  //---------------------------------------------------------------------------
  public void restart(ArrayList<String> urlList) {
    mUrlList    = urlList;
    mThreadPool = Executors.newFixedThreadPool(mThreadCount);
  }
  //---------------------------------------------------------------------------
  public int getSize() {
    return mUrlList.size();
  }
  //---------------------------------------------------------------------------
  public void preload(final int index) {
    if(index>=mUrlList.size())
      return;

    mThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        final Bitmap bitmap = Http.bitmapLoader(mUrlList.get(index));
        if(bitmap==null)
          return;
        mCache.put(index,bitmap);
      }
    });
  }
  //---------------------------------------------------------------------------
  public void stop() {
    mThreadPool.shutdown();
  }
  //---------------------------------------------------------------------------
  public void release() {
    for(Bitmap bitmap : mCache.values())
      bitmap.recycle();
  }
  //---------------------------------------------------------------------------
}





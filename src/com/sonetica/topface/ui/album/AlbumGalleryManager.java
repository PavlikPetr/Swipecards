package com.sonetica.topface.ui.album;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Data;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.MemoryCache;
import com.sonetica.topface.utils.StorageCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;
import android.widget.ImageView;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class AlbumGalleryManager {
  // Data
  private MemoryCache  mMemoryCache;
  private StorageCache mStorageCache;
  private ExecutorService mThreadsPool;
  private ArrayList<? extends Data> mData;
  //private HashMap<ImageView,Integer> mLinkCache;
  private int mThreadCount;
  //Constants
  private static final int THREAD_DEFAULT = 4;
  //---------------------------------------------------------------------------
  public AlbumGalleryManager(Context context,ArrayList<? extends Data> dataList) {
    mData = dataList;
    mThreadCount  = THREAD_DEFAULT;
    mMemoryCache  = new MemoryCache();
    mStorageCache = new StorageCache(context,StorageCache.EXTERNAL_CACHE);
    //mLinkCache    = new HashMap<ImageView,Integer>();
    mThreadsPool  = Executors.newFixedThreadPool(mThreadCount);
  }
  //---------------------------------------------------------------------------
  public Data get(int position) {
    return mData.get(position);
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,ImageView imageView) {
    //mLinkCache.put(imageView,position);
    Bitmap bitmap = mMemoryCache.get(position);
    if(bitmap!=null)
      imageView.setImageBitmap(bitmap);
      //imageView.setImageResource(R.drawable.ic_launcher);
    else {
      setImageToQueue(new Pair<ImageView,Integer>(imageView,position));
      imageView.setImageResource(R.drawable.im_black_square);
    }
  }
  //---------------------------------------------------------------------------
  private void setImageToQueue(final Pair<ImageView,Integer> data) {
    mThreadsPool.execute(new Runnable() {
      @Override
      public void run() {
        if(isViewReused(data))
          return;
        final Bitmap rawBitmap = Http.bitmapLoader(mData.get(data.second).getLink());
        if(rawBitmap==null)
          return;
        if(isViewReused(data))
          return;
        mMemoryCache.put(data.second,rawBitmap);
        data.first.post(new Runnable() {
          @Override
          public void run() {
            if(isViewReused( data))
              return;
            data.first.setImageBitmap(rawBitmap);
          }
        });
      }
    });
  }
  //-------------------------------------------------------------------------
  boolean isViewReused(Pair<ImageView,Integer> data){
//    int index=mLinkCache.get(data.first);
//    if(index!=data.second)
//      return true;
    return false;
  }
  //---------------------------------------------------------------------------
  public int size() {
    return mData.size();
  }
  //---------------------------------------------------------------------------
  public void release() {
    mThreadsPool.shutdown();
    mMemoryCache.clear();
  }
  //---------------------------------------------------------------------------
}





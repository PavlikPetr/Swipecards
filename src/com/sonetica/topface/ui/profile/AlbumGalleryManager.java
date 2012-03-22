package com.sonetica.topface.ui.profile;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.AbstractData;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.LeaksManager;
import com.sonetica.topface.utils.MemoryCache;
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
  //private StorageCache mStorageCache;
  //private ExecutorService mThreadsPool;
  private LinkedList<? extends AbstractData> mData;
  //private HashMap<ImageView,Integer> mLinkCache;
  //private int mThreadCount;
  //Constants
  //private static final int THREAD_DEFAULT = 4;
  //---------------------------------------------------------------------------
  public AlbumGalleryManager(Context context,LinkedList<? extends AbstractData> dataList) {
    mData = dataList;
    //mThreadCount  = THREAD_DEFAULT;
    mMemoryCache  = new MemoryCache();
    //mStorageCache = new StorageCache(context,StorageCache.EXTERNAL_CACHE);
    //mLinkCache    = new HashMap<ImageView,Integer>();
    //mThreadsPool  = Executors.newFixedThreadPool(mThreadCount);
  }
  //---------------------------------------------------------------------------
  public AbstractData get(int position) {
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
    //mThreadsPool.execute(new Runnable() {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        if(isViewReused(data))
          return;
        final Bitmap rawBitmap = Http.bitmapLoader(mData.get(data.second).getBigLink());
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
    LeaksManager.getInstance().monitorObject(t);
    t.start();
  }
  //-------------------------------------------------------------------------
  boolean isViewReused(Pair<ImageView,Integer> data){
//    int index=mLinkCache.get(data.first);
//    if(index!=data.second)
//      return true;
    return false;
  }
  //---------------------------------------------------------------------------
  public void preload(final Integer index) {
    if(index>=size())
      return;
    //mThreadsPool.execute(new Runnable() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        Bitmap bitmap = Http.bitmapLoader(mData.get(index).getBigLink());
        mMemoryCache.put(index,bitmap);
      }
    }).start();
  }
  //---------------------------------------------------------------------------
  public int size() {
    return mData.size();
  }
  //---------------------------------------------------------------------------
  public void release() {
    //mThreadsPool.shutdown();
    mMemoryCache.clear();
  }
  //---------------------------------------------------------------------------
}





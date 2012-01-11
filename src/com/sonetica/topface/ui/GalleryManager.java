package com.sonetica.topface.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.R;
import com.sonetica.topface.data.AbstractData;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Device;
import com.sonetica.topface.utils.MemoryCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;
import android.widget.ImageView;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class GalleryManager {
  // Data
  private MemoryCache mMemoryCache;
  private ExecutorService mThreadsPool;
  private LinkedList<? extends AbstractData> mData;
  private HashMap<ImageView,Integer> mLinkCache;
  private int mThreadCount;
  public  int mBitmapWidth;
  public  int mBitmapHeight;
  public  boolean mRunning = true;
  //Constants
  private static final int THREAD_DEFAULT = 4;
  //---------------------------------------------------------------------------
  public GalleryManager(Context context,LinkedList<? extends AbstractData> dataList) {
    this(context,dataList,THREAD_DEFAULT);
  }
  //---------------------------------------------------------------------------
  public GalleryManager(Context context,LinkedList<? extends AbstractData> dataList,int threadCount) {
    mData = dataList;
    mThreadCount  = threadCount;
    mMemoryCache  = new MemoryCache();
    mLinkCache    = new HashMap<ImageView,Integer>();
    mThreadsPool  = Executors.newFixedThreadPool(mThreadCount);
    mBitmapWidth  = Device.getDisplay(context).getWidth()/4;
    mBitmapHeight = (int)(mBitmapWidth*1.25);
  }
  //---------------------------------------------------------------------------
  public AbstractData get(int position) {
    return mData.get(position);
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,ImageView imageView) {
    mLinkCache.put(imageView,position);
    if(!mRunning) {
      imageView.setImageResource(R.drawable.im_black_square);
      return;
    }
    Bitmap bitmap = mMemoryCache.get(position);
    if(bitmap!=null)
      imageView.setImageBitmap(bitmap);
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
        Bitmap rawBitmap = Http.bitmapLoader(mData.get(data.second).getLink());
        if(rawBitmap==null)
          return;
        if(isViewReused(data))
          return;
        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap,mBitmapWidth,mBitmapHeight,false);
        mMemoryCache.put(data.second,scaledBitmap);
        data.first.post(new Runnable() {
          @Override
          public void run() {
            if(isViewReused( data))
              return;
            if(scaledBitmap!=null)
              data.first.setImageBitmap(mMemoryCache.get(data.second));
            else
              data.first.setImageResource(R.drawable.im_black_square);
          }
        });
      }
    });
  }
  //-------------------------------------------------------------------------
  boolean isViewReused(Pair<ImageView,Integer> data){
    int index=mLinkCache.get(data.first);
    if(index!=data.second)
      return true;
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





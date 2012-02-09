package com.sonetica.topface.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.R;
import com.sonetica.topface.data.AbstractData;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.CacheManager;
import com.sonetica.topface.utils.Device;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class GalleryManagerEx {
  // Data
  private CacheManager mCacheManager;
  private ExecutorService mThreadsPool;
  private LinkedList<? extends AbstractData> mDataList;
  private HashMap<ImageView,Integer> mLinkCache;
  private int mThreadCount;
  public  int mBitmapWidth;
  public  int mBitmapHeight;
  public  boolean mRunning = true;
  //Constants
  private static final int THREAD_DEFAULT = 4;
  //---------------------------------------------------------------------------
  public GalleryManagerEx(Context context,LinkedList<? extends AbstractData> dataList) {
    this(context,dataList,THREAD_DEFAULT);
  }
  //---------------------------------------------------------------------------
  public GalleryManagerEx(Context context,LinkedList<? extends AbstractData> dataList,int threadCount) {
    mDataList         = dataList;
    mThreadCount  = threadCount;
    mCacheManager = new CacheManager(context);
    mLinkCache    = new HashMap<ImageView,Integer>();
    mThreadsPool  = Executors.newFixedThreadPool(mThreadCount);
    int columnNumber = context.getResources().getInteger(R.integer.grid_column_number);
    mBitmapWidth  = Device.getDisplay(context).getWidth()/(columnNumber);
    mBitmapHeight = (int)(mBitmapWidth*1.25);
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<? extends AbstractData> dataList) {
    mDataList = dataList;
  }
  //---------------------------------------------------------------------------
  public AbstractData get(int position) {
    return mDataList.get(position);
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,final ImageView imageView) {
    mLinkCache.put(imageView,position);
    /*
    if(!mRunning) {
      imageView.setImageResource(R.drawable.im_black_square);
      return;
    }
    */
    final Bitmap bitmap = mCacheManager.get(position,mDataList.get(position).getBigLink());
    if(bitmap!=null)
      imageView.setImageBitmap(bitmap);
    else {
      setImageToQueue(position,imageView);
      imageView.setImageResource(R.drawable.im_black_square);
    }
  }
  //---------------------------------------------------------------------------
  private void setImageToQueue(final int position,final ImageView imageView) {
    if(mThreadsPool.isShutdown()) {
      imageView.post(new Runnable() {
        @Override
        public void run() {
          imageView.setImageResource(R.drawable.im_black_square);
        }
      });
      return;
    }
    mThreadsPool.execute(new Runnable() {
      @Override
      public void run() {
        if(isViewReused(position,imageView))
          return;
        Bitmap rawBitmap = Http.bitmapLoader(mDataList.get(position).getBigLink());
        if(rawBitmap==null)
          return;
        if(isViewReused(position,imageView))
          return;
        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap,mBitmapWidth,mBitmapHeight,false);
        mCacheManager.put(position,mDataList.get(position).getBigLink(),scaledBitmap);
        imageView.post(new Runnable() {
          @Override
          public void run() {
            if(isViewReused(position,imageView))
              return;
            if(scaledBitmap!=null)
              imageView.setImageBitmap(mCacheManager.get(position,mDataList.get(position).getBigLink()));
            else
              imageView.setImageResource(R.drawable.im_black_square);
          }
        });
      }
    });
  }
  //-------------------------------------------------------------------------
  boolean isViewReused(int position,ImageView imageView){
    int index = mLinkCache.get(imageView);
    if(index!=position)
      return true;
    return false;
  }
  //---------------------------------------------------------------------------
  public void restart() {
    mThreadsPool = Executors.newFixedThreadPool(mThreadCount);
  }
  //---------------------------------------------------------------------------
  public void stop() {
    mThreadsPool.shutdown();
  }
  //---------------------------------------------------------------------------
  public int size() {
    return mDataList.size();
  }
  //---------------------------------------------------------------------------
  public void release() {
    mThreadsPool.shutdown();
    //mCacheManager.clear();
  }
  //---------------------------------------------------------------------------
}





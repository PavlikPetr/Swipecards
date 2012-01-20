package com.sonetica.topface.ui;

import java.util.LinkedList; 
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.data.AbstractData;
import com.sonetica.topface.utils.Device;
import com.sonetica.topface.utils.MemoryCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class GalleryManagerEx {
  // Data
  private MemoryCache mCacheManager;
  private ExecutorService mThreadsPool;
  private LinkedList<? extends AbstractData> mData;
  private int mStartPos;
  private int mThreadCount;
  public  int mBitmapWidth;
  public  int mBitmapHeight;
  public  boolean mRunning = true;
  //Constants
  private static final int THREAD_DEFAULT = 1;
  //---------------------------------------------------------------------------
  public GalleryManagerEx(Context context,LinkedList<? extends AbstractData> dataList) {
    this(context,dataList,THREAD_DEFAULT);
  }
  //---------------------------------------------------------------------------
  public GalleryManagerEx(Context context,LinkedList<? extends AbstractData> dataList,int threadCount) {
    mData         = dataList;
    mThreadCount  = threadCount;
    mCacheManager = new MemoryCache();
    mThreadsPool  = Executors.newFixedThreadPool(mThreadCount);
    mBitmapWidth  = Device.getDisplay(context).getWidth()/4;
    mBitmapHeight = (int)(mBitmapWidth*1.25);
  }
  //---------------------------------------------------------------------------
  public AbstractData get(int position) {
    return mData.get(position);
  }
  //---------------------------------------------------------------------------
  public void setStartPos(int startPosition) {
    mStartPos = startPosition;
  }
  //---------------------------------------------------------------------------
  public void clearStartPos() {
    mStartPos = -1;
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,final ImageView imageView) {
    final Bitmap bitmap = mCacheManager.get(position);
    if(bitmap==null)
      setImageToQueue(position,imageView);
    imageView.setImageBitmap(bitmap);
  }
  //---------------------------------------------------------------------------
  public void setImageToQueue(int position,ImageView imageView ) {
    
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
    return mData.size();
  }
  //---------------------------------------------------------------------------
  public void release() {
    mThreadsPool.shutdown();
    //mCacheManager.clear();
  }
  //---------------------------------------------------------------------------
}





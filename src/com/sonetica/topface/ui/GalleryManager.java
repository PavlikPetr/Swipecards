package com.sonetica.topface.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.R;
import com.sonetica.topface.data.AbstractData;
import com.sonetica.topface.data.Like;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.CacheManager;
import com.sonetica.topface.utils.Device;
import com.sonetica.topface.utils.MemoryCache;
import com.sonetica.topface.utils.MemoryCacheEx;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class GalleryManager {
  // Data
  private ExecutorService mThreadsPool;
  private LinkedList<? extends AbstractData> mDataList;
  private MemoryCacheEx mCache;
  //private HashMap<String,Bitmap> mCache;
  public  int mBitmapWidth;
  public  int mBitmapHeight;
  public  boolean mRunning = true;
  //Constants
  private static final int THREAD_DEFAULT = 4;
  //---------------------------------------------------------------------------
  public GalleryManager(Context context,LinkedList<? extends AbstractData> dataList) {
    mDataList     = dataList;
    mThreadsPool  = Executors.newFixedThreadPool(THREAD_DEFAULT);
    int columnNumber = context.getResources().getInteger(R.integer.grid_column_number);
    mBitmapWidth  = Device.getDisplay(context).getWidth()/(columnNumber);
    mBitmapHeight = (int)(mBitmapWidth*1.25);
    
    //mCache = new HashMap<String,Bitmap>();
    mCache = new MemoryCacheEx();
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

    final Bitmap bitmap = mCache.get(mDataList.get(position).getLink());
    
    if(bitmap!=null)
      imageView.setImageBitmap(bitmap);
    else {
      setImageToQueue(position,imageView);
      imageView.setImageResource(R.drawable.im_black_square);
    }
  }
  //---------------------------------------------------------------------------
  private void setImageToQueue(final int position,final ImageView imageView) {

    mThreadsPool.execute(new Runnable() {
      @Override
      public void run() {
        Bitmap rawBitmap = Http.bitmapLoader(mDataList.get(position).getLink());
        if(rawBitmap==null)
          return;
        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap,mBitmapWidth,mBitmapHeight,false);
        mCache.put(mDataList.get(position).getLink(),scaledBitmap);
        imageView.post(new Runnable() {
          @Override
          public void run() {
            if(scaledBitmap!=null)
              imageView.setImageBitmap(mCache.get(mDataList.get(position).getLink()));
            else
              imageView.setImageResource(R.drawable.im_black_square);
          }
        });
      }
    });
  }
  //---------------------------------------------------------------------------
  public void stop() {
    //mThreadsPool.shutdown();
  }
  //---------------------------------------------------------------------------
  public int size() {
    return mDataList.size();
  }
  //---------------------------------------------------------------------------
  public void release() {
    //mThreadsPool.shutdown();
    //mCacheManager.clear();
  }
  //---------------------------------------------------------------------------
}





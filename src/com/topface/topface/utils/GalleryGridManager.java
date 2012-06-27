package com.topface.topface.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.topface.topface.Data;
import com.topface.topface.data.AbstractData;
import com.topface.topface.utils.CacheManager;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.MemoryCache;
import com.topface.topface.utils.StorageCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class GalleryGridManager<T extends AbstractData> implements OnScrollListener {
  //---------------------------------------------------------------------------
  class Queue {  // не используется
    // Data
    private HashMap<Integer,Bitmap> mQueue = new HashMap<Integer,Bitmap>(20);
    // Methods
    public Bitmap get(int key) {return mQueue.get(key);}
    public void put(int key,Bitmap value) {mQueue.put(key,value);}
    public void clear() {
      Debug.log(this,"memory cache clearing");
      int size = mQueue.size(); 
      for(int i=0; i<size; i++) {
        Bitmap bitmap = mQueue.get(i);
        if(bitmap!=null) {
          bitmap.recycle();
          mQueue.put(i,null); // хз
        }
      }
      mQueue.clear();
    }
  }
  //---------------------------------------------------------------------------
  // Data
  private LinkedList<T> mDataList;
  private ExecutorService mWorker;
  // кэш
  private MemoryCache mMemoryCache;
  private StorageCache mStorageCache;
  // размеры фотографии в гриде
  public int mBitmapWidth;
  public int mBitmapHeight;
  // скролинг
  public boolean mBusy;
  //---------------------------------------------------------------------------
  public GalleryGridManager(Context context,LinkedList<T> dataList) {
    mDataList     = dataList;
    mMemoryCache  = new MemoryCache();
    mStorageCache = new StorageCache(context, CacheManager.EXTERNAL_CACHE);
    mWorker = Executors.newFixedThreadPool(3);
    
    int columnNumber = Data.GRID_COLUMN;
    mBitmapWidth  = Device.getDisplay(context).getWidth()/(columnNumber);
    mBitmapHeight = (int)(mBitmapWidth*1.25);
  }
  //---------------------------------------------------------------------------
  public void update() {
    mMemoryCache.clear();
  }
  //---------------------------------------------------------------------------
  public AbstractData get(int position) {
    return mDataList.get(position);
  }
  //---------------------------------------------------------------------------
  public int size() {
    return mDataList.size();
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,final ImageView imageView) {
    Bitmap bitmap = mMemoryCache.get(position); 
    
    if(bitmap!=null)
      imageView.setImageBitmap(bitmap);
    else {
      imageView.setImageBitmap(null); // хз ??
      if(!mBusy) {
        bitmap = mStorageCache.load(mDataList.get(position).getSmallLink());
        if(bitmap!=null) {
          imageView.setImageBitmap(bitmap);
          mMemoryCache.put(position, bitmap);
        } else
          loadingImages(position ,imageView);
      }
    }
    bitmap = null;
  }
  //---------------------------------------------------------------------------
  private void loadingImages(final int position,final ImageView imageView) {
    mWorker.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if(mBusy) return;
            
          // качаем            
          Bitmap rawBitmap = Http.bitmapLoader(mDataList.get(position).getSmallLink()); // getBigLink() одно и тоже в Tops 

          if(rawBitmap==null) return;

          // вырезаем
          Bitmap clippedBitmap = Utils.clipping(rawBitmap,mBitmapWidth,mBitmapHeight);
          
          //rawBitmap.recycle();
          rawBitmap = null;
          
          // отображаем
          imagePost(imageView,clippedBitmap);
          
          // заливаем в кеш
          mMemoryCache.put(position,clippedBitmap);
          mStorageCache.save(mDataList.get(position).getSmallLink(),clippedBitmap);
          
          clippedBitmap = null;
          
        } catch (Exception e) {
          Debug.log(this,"thread error:"+e);
        }
      }
    });
  }
  //---------------------------------------------------------------------------
  private void imagePost(final ImageView imageView,final Bitmap bitmap) {
    imageView.post(new Runnable() {
      @Override
      public void run() {
        imageView.setImageBitmap(bitmap);
      }
    });
  }
  //---------------------------------------------------------------------------
  public void release() {
    mWorker.shutdown();
    mWorker = null;
    mMemoryCache.clear();
    mMemoryCache = null;
    mStorageCache = null;
    if(mDataList!=null)
      mDataList.clear();
    mDataList = null;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScrollStateChanged(AbsListView view,int scrollState) {
    switch(scrollState) {
      case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
        mBusy = true;
        break;
      case OnScrollListener.SCROLL_STATE_FLING:
        mBusy = true;
        break;
      case OnScrollListener.SCROLL_STATE_IDLE:
        mBusy = false;
        view.invalidateViews(); //  ПРАВИЛЬНО ???
        break;
    }
  }
  //---------------------------------------------------------------------------
}





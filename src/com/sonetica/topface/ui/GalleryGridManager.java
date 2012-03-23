package com.sonetica.topface.ui;

import java.util.HashMap;
import java.util.LinkedList;
import com.sonetica.topface.App;
import com.sonetica.topface.Data;
import com.sonetica.topface.data.AbstractData;
import com.sonetica.topface.utils.CacheManager;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Device;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.Imager;
import com.sonetica.topface.utils.LeaksManager;
import com.sonetica.topface.utils.MemoryCache;
import com.sonetica.topface.utils.StorageCache;
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
  class Queue {
    private HashMap<Integer,Bitmap> mQueue = new HashMap<Integer,Bitmap>(20);
    public Bitmap get(int key) {return mQueue.get(key);}
    public void put(int key,Bitmap value) {mQueue.put(key,value);}
  }
  //---------------------------------------------------------------------------
  // Data
  private LinkedList<T> mDataList;
  // кэш
  private MemoryCache  mMemoryCache;
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
    mStorageCache = new StorageCache(context,CacheManager.EXTERNAL_CACHE);
    
    int columnNumber = Data.s_gridColumn;
    mBitmapWidth  = Device.getDisplay(context).getWidth()/(columnNumber);
    mBitmapHeight = (int)(mBitmapWidth*1.25);
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<T> dataList) {
    mDataList = dataList;
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
      imageView.setImageBitmap(null);
      if(!mBusy) {
        bitmap = mStorageCache.load(mDataList.get(position).getSmallLink());
        if(bitmap!=null) {
          imageView.setImageBitmap(bitmap);
          mMemoryCache.put(position,bitmap);
        } else
          loadingImages(position,imageView);
      }
    }
    bitmap = null;
  }
  //---------------------------------------------------------------------------
  private void loadingImages(final int position,final ImageView imageView) {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        Bitmap rawBitmap = null;
        try {
          if(mBusy) return;
            
          // качаем            
          rawBitmap = Http.bitmapLoader(mDataList.get(position).getBigLink());

          if(rawBitmap==null) return;

          // вырезаем
          Bitmap clippedBitmap = Imager.clipping(rawBitmap,mBitmapWidth,mBitmapHeight);
          
          rawBitmap.recycle();
          rawBitmap = null;
          
          // отображаем
          imagePost(imageView,clippedBitmap);
          
          // заливаем в кеш
          mMemoryCache.put(position,clippedBitmap);
          mStorageCache.save(mDataList.get(position).getSmallLink(),clippedBitmap);
          
          clippedBitmap = null;
          
        } catch (Exception e) {
          Debug.log(App.TAG,"thread error:"+e);
        }
      } // run
    }); // thread
    LeaksManager.getInstance().monitorObject(t);
    t.start();
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





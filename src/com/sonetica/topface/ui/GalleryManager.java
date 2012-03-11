package com.sonetica.topface.ui;

import java.util.HashMap;
import java.util.LinkedList;
import com.sonetica.topface.App;
import com.sonetica.topface.R;
import com.sonetica.topface.data.AbstractData;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.CacheManager;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Device;
import com.sonetica.topface.utils.MemoryCache;
import com.sonetica.topface.utils.StorageCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class GalleryManager<T extends AbstractData> implements OnScrollListener {
  //---------------------------------------------------------------------------
  class Queue {
    private HashMap<Integer,Bitmap> mQueue = new HashMap<Integer,Bitmap>(20);
    public Bitmap get(int key) {return mQueue.get(key);}
    public void put(int key,Bitmap value) {mQueue.put(key,value);}
  }
  //---------------------------------------------------------------------------
  // Data
  //private int mStartPosition;
  //private int mEndPosition;
  //private ExecutorService mThreadsPool;  // что за хуйня с пулами потоков ???
  private LinkedList<T> mDataList;
  // кэш
  private MemoryCache  mMemoryCache;
  private StorageCache mStorageCache;
  // размеры фотографии в гриде
  public  int mBitmapWidth;
  public  int mBitmapHeight;
  // скролинг
  public  boolean mBusy;
  // Constants
  //private static final int THREAD_DEFAULT = 4;
  //---------------------------------------------------------------------------
  public GalleryManager(Context context,LinkedList<T> dataList) {
    mDataList     = dataList;
    //mThreadsPool  = Executors.newFixedThreadPool(THREAD_DEFAULT);
    mMemoryCache  = new MemoryCache();
    mStorageCache = new StorageCache(context,CacheManager.EXTERNAL_CACHE);
    
    int columnNumber = context.getResources().getInteger(R.integer.grid_column_number);
    mBitmapWidth  = Device.getDisplay(context).getWidth()/(columnNumber);
    mBitmapHeight = (int)(mBitmapWidth*1.25);
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<T> dataList) {
    mDataList = dataList;
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
      imageView.setImageResource(R.drawable.icon_people);
      if(!mBusy) {
        bitmap = mStorageCache.load(mDataList.get(position).getSmallLink());
        if(bitmap!=null) {
          imageView.setImageBitmap(bitmap);
          mMemoryCache.put(position,bitmap);
        } else
          loadingImages(position,imageView);
      }
    }
  }
  //---------------------------------------------------------------------------
  private void loadingImages(final int position,final ImageView imageView) {

    //mThreadsPool.execute(new Runnable() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          // Исходное загруженное изображение
          Bitmap rawBitmap = Http.bitmapLoader(mDataList.get(position).getBigLink());
          if(rawBitmap==null) 
            return;
  
          // Исходный размер загруженного изображения
          int width  = rawBitmap.getWidth();
          int height = rawBitmap.getHeight();
          
          // буль, длинная фото или высокая
          boolean LEG = false;
  
          if(width >= height) 
            LEG = true;
          
          if(mBusy) return;
       
          // коффициент сжатия фотографии
          float ratio = Math.max(((float)mBitmapWidth)/width,((float) mBitmapHeight)/height);
          
          // на получение оригинального размера по ширине или высоте
          if(ratio==0) ratio=1;
          
          // матрица сжатия
          Matrix matrix = new Matrix();
          matrix.postScale(ratio,ratio);
          
          // сжатие изображения
          Bitmap scaledBitmap = Bitmap.createBitmap(rawBitmap,0,0,width,height,matrix,true);
          
          // вырезаем необходимый размер
          final Bitmap clippedBitmap;
          if(LEG) {
            // у горизонтальной, вырезаем по центру
            int offset_x = (scaledBitmap.getWidth()-mBitmapWidth)/2;
            clippedBitmap = Bitmap.createBitmap(scaledBitmap,offset_x,0,mBitmapWidth,mBitmapHeight,null,false);
          } else
            // у вертикальной режим с верху
            clippedBitmap = Bitmap.createBitmap(scaledBitmap,0,0,mBitmapWidth,mBitmapHeight,null,false);
  
          // заливаем в кеш
          mMemoryCache.put(position,clippedBitmap);
          mStorageCache.save(mDataList.get(position).getSmallLink(),clippedBitmap);
          
          // ui draw
          imageView.post(new Runnable() {
            @Override
            public void run() {
              imageView.setImageBitmap(clippedBitmap);
            }
          });
        } catch (Exception e) {
          Debug.log(App.TAG,"thread error:"+e);
        }
      } // run
    }).start(); // thread

  }
  //---------------------------------------------------------------------------
  public void release() {
    //mThreadsPool.shutdown();
    mMemoryCache  = null;
    mStorageCache = null;
    if(mDataList!=null)
      mDataList.clear();
    mDataList = null;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
//    if(!mBusy) {
//      mStartPosition = firstVisibleItem;
//      mEndPosition   = mStartPosition + 10; 
//    }
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





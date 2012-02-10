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
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Device;
import com.sonetica.topface.utils.MemoryCache;
import com.sonetica.topface.utils.MemoryCacheEx;
import com.sonetica.topface.utils.StorageCache;
import com.sonetica.topface.utils.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.ImageView;

/*
 *  Менеджер изображений, загрузает и кеширует изображения
 */
public class GalleryManager {
  // Data
  private ExecutorService mThreadsPool;
  private LinkedList<? extends AbstractData> mDataList;
  private HashMap<ImageView,Integer> mLinkCache;
  private MemoryCacheEx mCache;
  private StorageCache mStorage;
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
    mLinkCache    = new HashMap<ImageView,Integer>();
    int columnNumber = context.getResources().getInteger(R.integer.grid_column_number);
    mBitmapWidth  = Device.getDisplay(context).getWidth()/(columnNumber);
    mBitmapHeight = (int)(mBitmapWidth*1.25);
    
    mCache = new MemoryCacheEx();
    mStorage = new StorageCache(context,CacheManager.EXTERNAL_CACHE);
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
    final Bitmap bitmap = mCache.get(mDataList.get(position).getBigLink());
    
    if(bitmap!=null) {
      imageView.setImageBitmap(bitmap);
      return;
    }
    Bitmap _bitmap = mStorage.load(Utils.md5(mDataList.get(position).getBigLink()));
    if(_bitmap!=null){
      imageView.setImageBitmap(_bitmap);
      mCache.put(mDataList.get(position).getBigLink(),_bitmap);
    } else {
      setImageToQueue(position,imageView);
      imageView.setImageResource(R.drawable.im_black_square);
    }
  }
  //---------------------------------------------------------------------------
  private void setImageToQueue(final int position,final ImageView imageView) {
    if(isViewReused(position,imageView))
      return;
    mThreadsPool.execute(new Runnable() {
      @Override
      public void run() {
        if(isViewReused(position,imageView))
          return;
        
        // Исходное загруженное изображение
        Bitmap rawBitmap = Http.bitmapLoader(mDataList.get(position).getBigLink());
        if(rawBitmap==null || isViewReused(position,imageView)) 
          return;

        // Исходный размер загруженного изображения
        int width  = rawBitmap.getWidth();
        int height = rawBitmap.getHeight();
        
        // буль, длиная фото или высокая
        boolean LEG = false;

        if(width >= height) 
          LEG = true;

      try {        
        
        // коффициент сжатия фотографии
        float ratio;
        /*
        if(LEG)
          ratio = (float)mBitmapHeight/height;
        else
          ratio = (float)mBitmapWidth/width;
        */
        
        ratio = Math.max(((float)mBitmapWidth)/width,((float) mBitmapHeight)/height);
        
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
        mCache.put(mDataList.get(position).getBigLink(),clippedBitmap);
        mStorage.save(Utils.md5(mDataList.get(position).getBigLink()),clippedBitmap);
        
        imageView.post(new Runnable() {
          @Override
          public void run() {
            if(isViewReused(position,imageView))
              return;
            if(clippedBitmap!=null)
              imageView.setImageBitmap(mCache.get(mDataList.get(position).getBigLink()));
            else
              imageView.setImageResource(R.drawable.im_black_square);
          }
        });
        
        
      } catch (Exception e) {
        Debug.log("!!!","Error w:"+rawBitmap.getWidth()+",h:"+rawBitmap.getHeight());
      }
        
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





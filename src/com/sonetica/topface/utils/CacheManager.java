package com.sonetica.topface.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.dbase.DbaseManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/*
 *  Менеджер управляет скачанными изорбражениями.
 *  Хранить в базе и на сторадже
 *  Хранение через слабые и мягкие ссылки
 */
public class CacheManager {
  // Data
  private Context mContext;
  private ExecutorService mThreadPool;
  private HashMap<IFrame,HashMap<Integer,Bitmap>> mCache;
  // Instance
  private static CacheManager mInstance;
  //---------------------------------------------------------------------------
  public static void create(Context context) {
    mInstance = new CacheManager(context);
  }
  //---------------------------------------------------------------------------
  public static AbstractCache getCache(IFrame frame) {
    switch(frame) {
      case TOPS:
        return new TopsCache(mInstance);
      case RATEIT:
        return new RateitCache(mInstance);
    }
    return null;
  }
  //---------------------------------------------------------------------------
  public CacheManager(Context context) {
    mContext = context;
    mThreadPool = Executors.newFixedThreadPool(3);
    //mDbase = new DbaseManager(context);
    mCache = new HashMap<IFrame,HashMap<Integer,Bitmap>>();
    for(IFrame frame : IFrame.values())
      mCache.put(frame,new HashMap<Integer,Bitmap>()); // create def map //load(frame)
    
    // загрузка кэша конкретного фрейма
    // load(IFrame.TOPS);
    // load(IFrame.RATEIT);
  }
  //---------------------------------------------------------------------------
  protected HashMap<Integer,Bitmap> getData(IFrame frame) {
    return mCache.get(frame);
  }
  //---------------------------------------------------------------------------
  protected void put(final IFrame frame,final int key,final Bitmap value,final String fileName) {
    switch(frame) {
      case TOPS:
        mThreadPool.execute(new Runnable() {
          @Override
          public void run() {
            // добавление в локальный кеш
            mCache.get(frame).put(key,value);
            
            //mDbase.insert(key,FileSystem.getFileName(fileName));
            
            // сохранение изображения в файл
            //if(saveBitmap(IFrame.TOPS,fileName,value));
              // сохранение имени изображения в базу
              //mDbase.insert(key,FileSystem.getFileName(fileName));
          }
        });
      break;
      case RATEIT:
      break;
    }
  }
  //---------------------------------------------------------------------------
  protected boolean save(final IFrame frame,final Bitmap bitmap,final String fileName) {
    mThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        BufferedOutputStream bos = null;
        File fdir = mContext.getFilesDir();
        fdir = new File(fdir,frame.name());
        if(!fdir.exists())
          fdir.mkdir();
        File file = new File(fdir,FileSystem.getFileName(fileName));
        if(file.exists())
          return;

        try {
          bos = new BufferedOutputStream(new FileOutputStream(file));
          bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos);
        } catch(FileNotFoundException e) {
          
        } 
          finally {
            try {
              if(bos!=null)
                bos.close();
            } catch(IOException e) {}
          }
        
      }
    });
    return true;
  }
  //---------------------------------------------------------------------------
  protected Bitmap load(IFrame frame,String fileName) {
    File file = mContext.getFilesDir();
    file = new File(new File(file,frame.name()),fileName);
    if(!file.exists())
      return null;
    
    BufferedInputStream bis = null;
    Bitmap bitmap = null;
    try {
      bis = new BufferedInputStream(new FileInputStream(file));
      bitmap = BitmapFactory.decodeStream(bis);
    } catch(FileNotFoundException e) {
      
    }
      finally {
        try {
          if(bis!=null)
            bis.close();
        } catch(IOException e) {}
      }
    return bitmap;
  }
  //---------------------------------------------------------------------------  
  protected boolean delete(IFrame frame,String fileName) {
    return false;
  }
  //---------------------------------------------------------------------------
  public static void close0() {
    mInstance.closeInt();
  }
  //---------------------------------------------------------------------------
  protected void closeInt() {
//    mContext = null;
//    mThreadPool.shutdown();
//    mThreadPool = null;
//    for(HashMap<Integer,Bitmap> cache : mCache.values())
//      if(cache!=null)
//        for(Bitmap bitmap : cache.values())
//          if(bitmap!=null)
//            bitmap.recycle();
//    mDbase.close();
//    mDbase = null;
//    mInstance = null;
  }
  //---------------------------------------------------------------------------
  protected void loadSyncCache(IFrame frame,ArrayList<TopUser> userList) {
//    switch(frame) {
//      case TOPS:
//        HashMap<Integer,Bitmap> temp = mCache.get(frame);
//        ArrayList<String> arr = mDbase.getTops();
//        for(int i=0;i<userList.size();i++) {
//          if(i>(arr.size()-1))
//            break;
//          if(FileSystem.getFileName(userList.get(i).photo).equals(arr.get(i)))
//            temp.put(i,load(frame,arr.get(i)));
//        }
//        //Utils.log(null,"sync: arr: "+arr.size()+" userList: "+userList.size()); 
//      break;
//    }
  }
  //---------------------------------------------------------------------------
  protected HashMap<Integer,Bitmap> loadCache(IFrame frame) {
    // подгрузка всех изображений из кеша для фрейма
    switch(frame) {
      case TOPS:
      break;
    }
    return null;
  }
}

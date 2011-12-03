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
import com.sonetica.topface.data.User;
import com.sonetica.topface.dbase.DbaseManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

/*
 *  Менеджер управляет скачанными изорбражениями.
 *  Хранить в базе и на сторадже
 *  Хранение через слабые и мягкие ссылки
 */
public class CacheManager {
  //---------------------------------------------------------------------------
  // class Cache
  //---------------------------------------------------------------------------
  public class Cache {
    private IFrame mFrame;
    private HashMap<Integer,Bitmap> mData;
    Cache(IFrame frame) {
      mFrame = frame;
      mData  = CacheManager.this.mCache.get(frame);
    }
    public boolean containsKey(int key) {
      return mData.containsKey(key);
    }
    public Bitmap get(int key) {
      return mData.get(key);
    }
    public void put(int key,Bitmap value,String name) {
      CacheManager.this.put(mFrame,key,value,name);
    }
    public void release() {
      for(Bitmap bitmap : mData.values())
        if(bitmap!=null)
        bitmap.recycle();
    }
  }
  //---------------------------------------------------------------------------
  // Data
  private Context mContext;
  private DbaseManager mDbase;
  private ExecutorService mThreadPool;
  private HashMap<IFrame,HashMap<Integer,Bitmap>> mCache;
  // instance
  private static CacheManager mInstance;
  //---------------------------------------------------------------------------
  public static void init(Context context) {
    mInstance = new CacheManager(context);
  }
  //---------------------------------------------------------------------------
  private CacheManager(Context context) {
    mContext = context;
    mThreadPool = Executors.newFixedThreadPool(2);
    mDbase = new DbaseManager(context);
    mCache = new HashMap<IFrame,HashMap<Integer,Bitmap>>();
    for(IFrame frame : IFrame.values())
      mCache.put(frame,new HashMap<Integer,Bitmap>()); //load(frame)
    
    // загрузка кэша конкретного фрейма
    // load(IFrame.TOPS);
    // load(IFrame.RATEIT);
  }
  //---------------------------------------------------------------------------
  public static Cache getCache(IFrame frame,ArrayList<User> userList) {
    return mInstance.getCacheInt(frame,userList);
  }
  //---------------------------------------------------------------------------
  private Cache getCacheInt(IFrame frame,ArrayList<User> userList) {
   // loadSyncCache(frame,userList);
    return new Cache(frame);
  }
  //---------------------------------------------------------------------------
  private void put(final IFrame frame,final int key,final Bitmap value,final String fileName) {
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
  private void loadSyncCache(IFrame frame,ArrayList<User> userList) {
    switch(frame) {
      case TOPS:
        HashMap<Integer,Bitmap> temp = mCache.get(frame);
        ArrayList<String> arr = mDbase.getTops();
        for(int i=0;i<userList.size();i++) {
          if(i>(arr.size()-1))
            break;
          if(FileSystem.getFileName(userList.get(i).link).equals(arr.get(i)))
            temp.put(i,loadBitmap(frame,arr.get(i)));
        }
        //Utils.log(null,"sync: arr: "+arr.size()+" userList: "+userList.size()); 
      break;
    }
  }
  //---------------------------------------------------------------------------
  private HashMap<Integer,Bitmap> loadCache(IFrame frame) {
    // подгрузка всех изображений из кеша для фрейма
    switch(frame) {
      case TOPS:
      break;
    }
    return null;
  }
  //---------------------------------------------------------------------------
  public boolean saveBitmap(IFrame frame,String fileName, Bitmap bitmap) {
    BufferedOutputStream bos = null;
    File fdir = mContext.getFilesDir();
    fdir = new File(fdir,frame.name());
    if(!fdir.exists())
      fdir.mkdir();
    File file = new File(fdir,FileSystem.getFileName(fileName));
    if(file.exists())
      return false;

    try {
      bos = new BufferedOutputStream(new FileOutputStream(file));
      bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos);
    } catch(FileNotFoundException e) { Utils.log(null,"error: "+e.getMessage()); } 
      finally {
        try {
          if(bos!=null)
            bos.close();
        } catch(IOException e) {}
      }
    Utils.log(null,"saveBitmap: "+fileName);
    return true;
  }
  //---------------------------------------------------------------------------
  public Bitmap loadBitmap(IFrame frame,String fileName) {
    File file = mContext.getFilesDir();
    file = new File(new File(file,frame.name()),fileName);
    if(!file.exists())
      return null;
    
    BufferedInputStream bis = null;
    Bitmap bitmap = null;
    try {
      bis = new BufferedInputStream(new FileInputStream(file));
      bitmap = BitmapFactory.decodeStream(bis);
    } catch(FileNotFoundException e) { Utils.log(null,"error: "+e.getMessage()); }
      finally {
        try {
          if(bis!=null)
            bis.close();
        } catch(IOException e) {}
      }
    Utils.log(null,"loadBitmap: "+fileName);
    return bitmap;
  }
  //---------------------------------------------------------------------------
  public static void close() {
    mInstance.closeInt();
  }
  //---------------------------------------------------------------------------
  private void closeInt() {
    mContext = null;
    mThreadPool.shutdown();
    mThreadPool = null;
    for(HashMap<Integer,Bitmap> cache : mCache.values())
      if(cache!=null)
        for(Bitmap bitmap : cache.values())
          if(bitmap!=null)
            bitmap.recycle();
    mDbase.close();
    mInstance = null;
  }
  //---------------------------------------------------------------------------
//  public void release() {
//    Collection<Bitmap> values = mCache.values();
//    for(Bitmap bitmap : values)
//      bitmap.recycle();
//    mCache.clear();
//  }
  //---------------------------------------------------------------------------
}

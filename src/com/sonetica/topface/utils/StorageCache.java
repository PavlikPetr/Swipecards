package com.sonetica.topface.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/*
 *  Класс для сохранения и загрузки изображений на карту памяти
 */
public class StorageCache {
  // Data
  private Context mContext;
  private ExecutorService mThreadPool;
  private int mCacheType;
  // Constants
  public static final int INTERNAL_CACHE = 0;
  public static final int EXTERNAL_CACHE = 1;
  //---------------------------------------------------------------------------
  public StorageCache(Context context,int cacheType){
    mContext    = context;
    mCacheType  = cacheType;
    mThreadPool = Executors.newFixedThreadPool(3);
  }
  //---------------------------------------------------------------------------
  public boolean isExist(String fileName) {
    File file = new File(getCacheDirectory(),fileName);
    if(file.exists())
      return true;
    return false;
  }
  //---------------------------------------------------------------------------
  public Bitmap load(String fileName) {
    File cacheDir = getCacheDirectory();
    File file = new File(cacheDir,fileName);
    if(!file.exists())
      return null;
    BufferedInputStream bis = null;
    Bitmap bitmap = null;
    try {
      bis = new BufferedInputStream(new FileInputStream(file));
      bitmap = BitmapFactory.decodeStream(bis);
    } catch(FileNotFoundException e) {
      Debug.log(this,"bitmap loading, file not found #1");
    } finally {
      try {
        if(bis!=null)
          bis.close();
      } catch(IOException e) {
        Debug.log(this,"bitmap loading, input stream not closed #2");
      }
    }
    return bitmap;
  }
  //---------------------------------------------------------------------------
  public void save(final String fileName, final Bitmap bitmap) {
    mThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        BufferedOutputStream bos = null;
        File cacheDir = getCacheDirectory();
        if(cacheDir==null) 
          return;
        File file = new File(cacheDir,fileName);
        if(file.exists()) 
          return;
        try {
          bos = new BufferedOutputStream(new FileOutputStream(file));
          bitmap.compress(Bitmap.CompressFormat.PNG, 85, bos);
        } catch(FileNotFoundException e) {
          Debug.log(this,"bitmap saving, file not found #1");
        } finally {
          try {
            if(bos!=null) bos.close();
          } catch(IOException e) {
            Debug.log(this,"bitmap saving, output stream not closed #2");    
          }
        }
      }
    });
  }
  //---------------------------------------------------------------------------
  private File getCacheDirectory() {
    return mCacheType==EXTERNAL_CACHE ? mContext.getCacheDir() : mContext.getCacheDir(); //getExternalCacheDir
  }
  //---------------------------------------------------------------------------  
  public void clear(){
    File[] files; 
    files = mContext.getCacheDir().listFiles();
    if(files!=null)
      for(File file : files)
        file.delete();
    files = mContext.getCacheDir().listFiles();
    if(files!=null)
      for(File file : files)
        file.delete();
  }
  //---------------------------------------------------------------------------
}

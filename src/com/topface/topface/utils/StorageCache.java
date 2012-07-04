package com.topface.topface.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/* Класс для сохранения и загрузки изображений на карту памяти */
public class StorageCache {
    // Data
    private Context mContext;
    //private ExecutorService mThreadPool;
    private File mCacheDir;
    private int mCacheType;
    // Constants
    public static final int INTERNAL_CACHE = 0;
    public static final int EXTERNAL_CACHE = 1;
    //---------------------------------------------------------------------------
    public StorageCache(Context context) {
        this(context, EXTERNAL_CACHE, 3);
    }
    //---------------------------------------------------------------------------
    public StorageCache(Context context,int cacheType) {
        this(context, cacheType, 3);
    }
    //---------------------------------------------------------------------------
    public StorageCache(Context context,int cacheType,int countThreads) {
        mContext = context;
        mCacheType = cacheType;
        //mThreadPool = Executors.newFixedThreadPool(countThreads);
        mCacheDir = getCacheDirectory();
        if (!mCacheDir.exists())
            mCacheDir.mkdirs();
    }
    //---------------------------------------------------------------------------
    public Bitmap load(String fileName) {
        Bitmap bitmap = null;
        BufferedInputStream bis = null;
        try {
            File file = new File(mCacheDir, Utils.md5(fileName));
            if (!file.exists())
                return null;
            bis = new BufferedInputStream(new FileInputStream(file));
            bitmap = BitmapFactory.decodeStream(bis);
        } catch(FileNotFoundException e) {
            Debug.log(this, "bitmap loading, file not found #1 " + e);
        } catch(Exception e) {
            Debug.log(this, "bitmap loading, exception: " + e);
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch(IOException e) {
                Debug.log(this, "bitmap loading, input stream not closed #2 " + e);
            }
        }
        return bitmap;
    }
    //---------------------------------------------------------------------------
    public Bitmap load(int id) {
        Bitmap bitmap = null;
        BufferedInputStream bis = null;
        try {
            File file = new File(mCacheDir, Integer.toString(id));
            if (!file.exists())
                return null;
            bis = new BufferedInputStream(new FileInputStream(file));
            bitmap = BitmapFactory.decodeStream(bis);
        } catch(FileNotFoundException e) {
            Debug.log(this, "bitmap loading, file not found #1 " + e);
        } catch(Exception e) {
            Debug.log(this, "bitmap loading, exception: " + e);
        } finally {
            try {
                if (bis != null)
                    bis.close();
            } catch(IOException e) {
                Debug.log(this, "bitmap loading, input stream not closed #2 " + e);
            }
        }
        return bitmap;
    }
    //---------------------------------------------------------------------------
    public void save(final String fileName,final Bitmap bitmap) {
        //mThreadPool.execute(new Runnable() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedOutputStream bos = null;
                try {
                    File file = new File(mCacheDir, Utils.md5(fileName));
                    if (file.exists())
                        return;
                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 85, bos);
                } catch(FileNotFoundException e) {
                    Debug.log(this, "bitmap saving, file not found #1 " + e);
                } catch(Exception e) {
                    Debug.log(this, "bitmap loading, exception: " + e);
                } finally {
                    try {
                        if (bos != null)
                            bos.close();
                    } catch(IOException e) {
                        Debug.log(this, "bitmap saving, output stream not closed #2 " + e);
                    }
                }
            }
        }).start();
    }
    //---------------------------------------------------------------------------
    private File getCacheDirectory() {
        return mCacheType == EXTERNAL_CACHE ? FileSystem.getExternalCacheDirectory() : mContext.getCacheDir();
    }
    //---------------------------------------------------------------------------  
    public void clear() {
        Debug.log(this, "clearing");
        File[] files;
        files = mContext.getCacheDir().listFiles();
        if (files != null)
            for (File file : files)
                file.delete();
        files = getCacheDirectory().listFiles();
        if (files != null)
            for (File file : files)
                file.delete();
    }
    //---------------------------------------------------------------------------
}

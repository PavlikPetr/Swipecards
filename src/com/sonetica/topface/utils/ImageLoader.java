package com.sonetica.topface.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

/*
 *  
 */
public class ImageLoader {
  // Data
  private ArrayList<String>  mUrlList;  //список ссылок на изображения полученные из JSON ответа topfase сервера
  private LinkedList<Pair<View,Integer>> mQueue; //очередь ожидающих вьюшек на подгрузку битмапа
  private final HashMap<Integer,Bitmap> mCache;
  // пул на 4 одновременно работающих потока, остальные добавленные сидят курят, пока освободится место
  private ExecutorService mThreadPool =  Executors.newFixedThreadPool(4);
  private Context mContext;
  
  //---------------------------------------------------------------------------
  public ImageLoader(Context context,ArrayList<String> urlList) {
    mContext = context;
    mUrlList = urlList;
    mQueue = new LinkedList<Pair<View,Integer>>();
    mCache = new HashMap<Integer,Bitmap>();
  }
  //---------------------------------------------------------------------------
  public int getSize() {
    return mUrlList.size();
  }
  //---------------------------------------------------------------------------
  public void getBitmap(final int position,final View view) {
    // вставка view в очередь на ожидание
    // mQueue.add(new Pair(view,position));
    // вызов манагера пула потоков
    
    if(!mCache.containsKey(position))
      mThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          String uriLink = mUrlList.get(position);
          final Bitmap bitmap = Http.bitmapLoader(uriLink);
          File file = mContext.getFilesDir();
          BufferedOutputStream bos = null;
          
          // отдельный кеширующий поток менеджер, который сохраняет и подгружает
          // BitmapFactory.decodeFile
          /*
          try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
          } catch(FileNotFoundException e1) {e1.printStackTrace();}
          */
          mCache.put(position,bitmap);
          
          view.post(new Runnable() {
            @Override
            public void run() {
              ((ImageView)view).setImageBitmap(mCache.get(position));
              view.invalidate();
            }
          });
          //try {bos.close();}catch(IOException e){e.printStackTrace();}
        }
      });
    else
      ((ImageView)view).setImageBitmap(mCache.get(position));

  }
  //---------------------------------------------------------------------------
  private Bitmap downloadImage() {
    return null;
  }
  //---------------------------------------------------------------------------
}

// сохранять фотки в кэш
// проверять, есть ли в кеше перед загрузкой
/*
    new Thread(new Runnable() {
      @Override
      public void run() {
        if(!mCache.containsKey(position)) {
          Bitmap bmp = Http.httpBitmapLoader(mUrlList.get(position));
          mCache.put(position,bmp);
        }
          
        view.post(new Runnable() {
          @Override
          public void run() {
            ((ImageView)view).setImageBitmap(mCache.get(position));
          }
        });
        
      }
    }).start();
*/
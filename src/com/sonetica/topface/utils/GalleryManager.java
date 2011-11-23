package com.sonetica.topface.utils;

import java.util.ArrayList;
import java.util.concurrent.*;
import com.sonetica.topface.data.User;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/*
 *  Менеджер изображений закачивает, сторит и выдает
 */
public class GalleryManager {
  // Data
  private Context mContext;
  private String mName;
  private ArrayList<User> mUserList;  //список ссылок на изображения полученные из JSON ответа topfase сервера
  private CacheManager mCacheManager;
  // пул на 4 одновременно работающих потока, остальные добавленные сидят курят, пока освободится место
  private ExecutorService mThreadPool =  Executors.newFixedThreadPool(4);
  //private LinkedList<Pair<View,Integer>> mQueue; //очередь ожидающих вьюшек на подгрузку изображения
  //---------------------------------------------------------------------------
  public GalleryManager(Context context, String name, ArrayList<User> userList) {
    mContext = context;
    mName  = name;
    //mQueue = new LinkedList<Pair<View,Integer>>();
    mUserList = userList;
    mCacheManager = CacheManager.getInstance();
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position, final ImageView view) {
    if(mCacheManager.containsKey(position)) {
      view.setImageBitmap(mCacheManager.get(position));
    } else {
      mThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          final Bitmap bitmap = Http.bitmapLoader(mUserList.get(position).link);
          if(bitmap==null)
            return;
          view.post(new Runnable() {
            @Override
            public void run() {
              view.setImageBitmap(bitmap);
              view.invalidate();
            }
          });
          mCacheManager.put(position,bitmap);
        }
      });
    }
  }
  //---------------------------------------------------------------------------
  public void restart(ArrayList<User> userList) {
    mUserList   = userList;
    mThreadPool = Executors.newFixedThreadPool(4);
  }
  //---------------------------------------------------------------------------
  public int getSize() {
    return mUserList.size();
  }
  //---------------------------------------------------------------------------
  public void stop() {
    mThreadPool.shutdown();
  }
  //---------------------------------------------------------------------------
  public void release() {
    mCacheManager.release();
  }
  //---------------------------------------------------------------------------
}

//view.invalidate();
/*
view.post(new Runnable() {
  @Override
  public void run() {
    view.setImageBitmap(mCacheManager.get(position));
    //view.invalidate();
  }
});
*/      
/*
public void getBitmap(final int position,final View view) {
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
        * /
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
*/

/*
public void imageLoader(final int position,final View view) {
  // отдельный кеширующий поток менеджер, который сохраняет и подгружает
  if(!mCache.containsKey(position)) {
  if(!mThreadPool.isShutdown())
    mThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        Bitmap bitmap = Http.bitmapLoader(mUrlList.get(position));
        mCache.put(position,bitmap);
        
        view.post(new Runnable() {
          @Override
          public void run() {
            ((ImageView)view).setImageBitmap(mCache.get(position));
            view.invalidate();
          }
        });
      }
    });
  } else {
    ((ImageView)view).setImageBitmap(mCache.get(position));
  }
}
*/

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


//BitmapFactory.decodeFile
/*
try {
       File file = mContext.getFilesDir();
       BufferedOutputStream bos = null;
bos = new BufferedOutputStream(new FileOutputStream(file));
} catch(FileNotFoundException e1) {e1.printStackTrace();}
       //try {bos.close();}catch(IOException e){e.printStackTrace();}
*/


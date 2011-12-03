package com.sonetica.topface.utils;

import java.util.ArrayList;
import java.util.concurrent.*;
import com.sonetica.topface.data.User;
import com.sonetica.topface.net.Http;
import android.graphics.Bitmap;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/*
 *  Менеджер изображений закачивает, сторит и выдает через CacheManager
 *  предназанчен для окон "Топы" и "Я нравлюсь" 
 */
public class GalleryCachedManager implements AbsListView.OnScrollListener {
  // Data
  private ArrayList<User> mFullUserList;
  private ArrayList<User> mWorkUserList;
  private CacheManager.Cache mCache;
  private ExecutorService mThreadPool;
  //---------------------------------------------------------------------------
  public GalleryCachedManager(IFrame frame,ArrayList<User> userList) {
    mFullUserList  = userList;
    mWorkUserList  = new ArrayList<User>();
    for(int i=0;i<16;++i)
      mWorkUserList.add(mFullUserList.get(i));
    mThreadPool = Executors.newFixedThreadPool(4);
    mCache = CacheManager.getCache(frame,userList);
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,final ImageView view) {
    if(mCache.containsKey(position))
      view.setImageBitmap(mCache.get(position));
    else {
      mThreadPool.execute(new Runnable() {
        @Override
        public void run() {
          final Bitmap bitmap = Http.bitmapLoader(mWorkUserList.get(position).link);
          if(bitmap==null)
            return;
          //if(view.isShown())
            view.post(new Runnable() {
              @Override
              public void run() {
                view.setImageBitmap(bitmap);
                view.invalidate();
              }
            });
          mCache.put(position,bitmap,mWorkUserList.get(position).link);
        }
      });
    }
  }
  //---------------------------------------------------------------------------
  public int getSize() {
    return mWorkUserList.size();
  }
  //---------------------------------------------------------------------------
  private int mPrevFirstVisibleItem;
  @Override
  public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
    Utils.log(null,"enter fvi:"+firstVisibleItem+" prev:"+mPrevFirstVisibleItem);
    if(firstVisibleItem>mPrevFirstVisibleItem) {
      int num = firstVisibleItem+visibleItemCount;
      for(int i=num;i<num+4||i<=mFullUserList.size();++i) {
        Utils.log(null,"i:"+i);
        mWorkUserList.add(mFullUserList.get(i));
      }
      mPrevFirstVisibleItem = firstVisibleItem;
      BaseAdapter adapter = (BaseAdapter)view.getAdapter();
      if(adapter!=null)
        adapter.notifyDataSetChanged();
      Utils.log(null,"exit fvi:"+firstVisibleItem);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScrollStateChanged(AbsListView view,int scrollState) {
  }
  //---------------------------------------------------------------------------
}
//mQueue = new LinkedList<Pair<View,Integer>>();
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



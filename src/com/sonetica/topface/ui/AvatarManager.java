package com.sonetica.topface.ui;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.R;
import com.sonetica.topface.data.AbstractData;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.CacheManager;
import com.sonetica.topface.utils.MemoryCacheEx;
import com.sonetica.topface.utils.StorageCache;
import com.sonetica.topface.utils.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.AbsListView.OnScrollListener;

/*
 *  Менеджер аватарок, загрузает и кеширует изображения
 */
public class AvatarManager<T extends AbstractData> implements AbsListView.OnScrollListener {
  // Data
  private LinkedList<T> mDataList;
  private MemoryCacheEx mCache;
  private StorageCache  mStorage;
  private ExecutorService mThreadsPool;
  private boolean mBusy; 
  //Constants
  private static final int THREAD_DEFAULT = 3;
  //---------------------------------------------------------------------------
  public AvatarManager(Context context,LinkedList<T> dataList) {
    mThreadsPool = Executors.newFixedThreadPool(THREAD_DEFAULT);
    mDataList = dataList;
    mCache    = new MemoryCacheEx();
    mStorage  = new StorageCache(context,CacheManager.EXTERNAL_CACHE);
  }
  //---------------------------------------------------------------------------
  public Bitmap getImage(String url) {
    Bitmap bitmap = mCache.get(url);
    if(bitmap==null) {
      bitmap = mStorage.load(Utils.md5(url));
      mCache.put(url,bitmap);
    }
    return bitmap;
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<T> dataList) {
    mDataList = dataList;
  }
  //---------------------------------------------------------------------------
  public T get(int position) {
    return mDataList.get(position);
  }
  //---------------------------------------------------------------------------
  public int size() {
    return mDataList.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
    //int mStartItem  = firstVisibleItem;
    //int mCountItems = visibleItemCount; 
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScrollStateChanged(final AbsListView view,int scrollState) {
    switch(scrollState) {
      case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
        mBusy = true;
        break;
      case OnScrollListener.SCROLL_STATE_FLING:
        mBusy = true;
        break;
      case OnScrollListener.SCROLL_STATE_IDLE: {
        mBusy = false;
        
        final int first = view.getFirstVisiblePosition();
        final int count = view.getChildCount();
        
        for(int i=0;i<count;i++) {
          final int index = i;
          mThreadsPool.execute(new Runnable() {
            @Override
            public void run() {
              View item = view.getChildAt(index);
              if(item==null) return;
              final ImageView iv = (ImageView)(item.findViewById(R.id.ivAvatar));
              if(iv==null) return;
              if(iv.getTag()!=null) {
                if(mBusy)
                  return;
                final Bitmap rawBitmap = Http.bitmapLoader(mDataList.get(first+index).getSmallLink());
                if(rawBitmap!=null) {
                  mStorage.save(Utils.md5(mDataList.get(first+index).getBigLink()),rawBitmap);
                  iv.post(new Runnable() {
                    @Override
                    public void run() {
                      iv.setImageBitmap(rawBitmap);
                    }
                  });
                }
                iv.setTag(null);
              } 
            }
          });//thread
        }
      }
      break;
    }
  }
  //---------------------------------------------------------------------------
}

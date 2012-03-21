package com.sonetica.topface.ui;

import java.util.HashMap;
import java.util.LinkedList;
import com.sonetica.topface.App;
import com.sonetica.topface.data.AbstractData;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.Imager;
import com.sonetica.topface.utils.LeaksManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.AbsListView.OnScrollListener;

/*
 *  Менеджер аватарок, загрузает и кеширует изображения
 */
public class AvatarManager<T extends AbstractData> implements AbsListView.OnScrollListener {
  // Data
  private LinkedList<T> mDataList;
  private HashMap<Integer,Bitmap> mCache;
  //private ExecutorService mThreadsPool;
  private boolean mBusy;
  private int mRadius = 12;  // хард кор !!!!!!!
  //Constants
  //private static final int THREAD_DEFAULT = 2;
  //---------------------------------------------------------------------------
  public AvatarManager(Context context,LinkedList<T> dataList) {
    mDataList = dataList;
    mCache = new HashMap<Integer,Bitmap>();
    //mThreadsPool = Executors.newFixedThreadPool(THREAD_DEFAULT);
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<T> dataList) {
    mDataList = dataList;
    
    clear();
  }
  //---------------------------------------------------------------------------
  private void clear() {
    int size = mCache.size(); 
    for(int i=0;i<size;++i) {
      Bitmap bitmap = mCache.get(i);
      if(bitmap!=null)
        bitmap.recycle();
    }
    mCache.clear();
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
        view.invalidateViews(); //  ПРАВИЛЬНО ???
      }
      break;
    }
  }
  //---------------------------------------------------------------------------
  public void getImage(final int position,final ImageView imageView) {
    Bitmap bitmap = mCache.get(position);
    if(bitmap!=null)
      imageView.setImageBitmap(bitmap);
    else {
      //imageView.setImageResource(R.drawable.icon_people);
      imageView.setImageBitmap(null);
      if(!mBusy)
        loadingImages(position,imageView);
    }
  }
  //---------------------------------------------------------------------------
  public void loadingImages(final int position,final ImageView imageView) {
    //mThreadsPool.execute(new Runnable() {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        Bitmap rawBitmap = null;
        Bitmap clippedBitmap = null;
        try {
          if(mBusy) return;
        
          rawBitmap = Http.bitmapLoader(mDataList.get(position).getSmallLink());
          
          clippedBitmap = Imager.clipping(rawBitmap,imageView.getWidth(),imageView.getHeight());
          if(clippedBitmap==null)
            return;
          
          final Bitmap roundBitmap = Imager.getRoundedCornerBitmap(clippedBitmap,clippedBitmap.getWidth(),clippedBitmap.getHeight(),mRadius);
          
          if(roundBitmap!=null) {
            imageView.post(new Runnable() {
              @Override
              public void run() {
                imageView.setImageBitmap(roundBitmap);
              }
            });
            if(mCache!=null)
              mCache.put(position,roundBitmap);
          }
        } catch (Exception e) {
          Debug.log(App.TAG,"thread error:"+e);
        } finally {
          if(rawBitmap!=null)
            rawBitmap.recycle();
          if(clippedBitmap!=null)
            clippedBitmap.recycle();
        }
      } 
    });
    LeaksManager.getInstance().monitorObject(t);
    t.start();
  }
  //---------------------------------------------------------------------------
  public void release() {
    clear();
    mCache = null;
    mDataList = null;
    
//    if(mThreadsPool!=null)
//      mThreadsPool.shutdown();
//    mThreadsPool = null;
  }
  //---------------------------------------------------------------------------
}

/*
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
*/
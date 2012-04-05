package com.topface.topface.ui;

import java.util.HashMap;
import java.util.LinkedList;
import com.topface.topface.App;
import com.topface.topface.data.AbstractData;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
import com.topface.topface.utils.Imager;
import com.topface.topface.utils.LeaksManager;
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
  private boolean mBusy;
  private int mRadius = 12;  // хард кор !!!!!!!
  //---------------------------------------------------------------------------
  public AvatarManager(Context context,LinkedList<T> dataList) {
    mDataList = dataList;
    mCache = new HashMap<Integer,Bitmap>();
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<T> dataList) {
    clear();
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
  public void getImage(final int position,final ImageView imageView) {
    Bitmap bitmap = mCache.get(position);
    
    if(bitmap!=null)
      imageView.setImageBitmap(bitmap);
    else {
      imageView.setImageBitmap(null);
      if(!mBusy)
        loadingImages(position,imageView);
    }
  }
  //---------------------------------------------------------------------------
  private void loadingImages(final int position,final ImageView imageView) {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        Bitmap rawBitmap = null;
        try {
          if(mBusy) return;
        
          //качаем
          rawBitmap = Http.bitmapLoader(mDataList.get(position).getSmallLink());
          
          // округляем
          final Bitmap roundBitmap = Imager.getRoundedCornerBitmap(rawBitmap,imageView.getWidth(),imageView.getHeight(),mRadius);
          
          imagePost(imageView,roundBitmap);
          
          mCache.put(position,roundBitmap);

        } catch (Exception e) {
          Debug.log(App.TAG,"thread error:"+e);
        } finally {
          if(rawBitmap!=null)
            rawBitmap.recycle();
          rawBitmap=null;
        }
      } 
    });
    LeaksManager.getInstance().monitorObject(t);
    t.start();
  }
  //---------------------------------------------------------------------------
  private void imagePost(final ImageView imageView,final Bitmap bitmap) {
    imageView.post(new Runnable() {
      @Override
      public void run() {
        imageView.setImageBitmap(bitmap);
      }
    });
  }
  //---------------------------------------------------------------------------
  public void release() {
    clear();
    mCache = null;
    mDataList = null;
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
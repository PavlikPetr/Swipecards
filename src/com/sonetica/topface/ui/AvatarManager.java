package com.sonetica.topface.ui;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import com.sonetica.topface.R;
import com.sonetica.topface.data.AbstractData;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.MemoryCacheEx;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.AbsListView.OnScrollListener;

/*
 *  Менеджер аватарок, загрузает и кеширует изображения
 */
public class AvatarManager<T extends AbstractData> implements AbsListView.OnScrollListener {
  // Data
  private ExecutorService mThreadsPool;
  private LinkedList<T> mDataList;
  private MemoryCacheEx mCache;
  private int mStartItem;
  private int mCountItems;
  private boolean mBusy; 
  //Constants
  private static final int THREAD_DEFAULT = 4;
  //---------------------------------------------------------------------------
  public AvatarManager(Context context,LinkedList<T> dataList) {
    mDataList = dataList;
    //mThreadsPool = Executors.newFixedThreadPool(THREAD_DEFAULT);
    mCache = new MemoryCacheEx();
  }
  //---------------------------------------------------------------------------
  public Bitmap getImage(String url) {
    return mCache.get(url);
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
    mStartItem  = firstVisibleItem;
    mCountItems = visibleItemCount; 
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
        int first = view.getFirstVisiblePosition();
        int count = view.getChildCount();
        int size = first + count;
        for (int i=0;i<count;i++) {
          View v = view.getChildAt(i);
          if(v==null)
            continue;
          ImageView iv = (ImageView)(v.findViewById(R.id.ivAvatar));
          if(iv==null)
            continue;
          if(iv.getTag()!=null) {
            int z = first+i;
            Bitmap rawBitmap = Http.bitmapLoader(mDataList.get(z).getSmallLink());
            if(rawBitmap!=null)
              iv.setImageBitmap(rawBitmap);
            iv.setTag(null);
          } 
        }
      }break;
    }
  }
  //---------------------------------------------------------------------------
}


/*
 * int first = view.getFirstVisiblePosition();
      int count = view.getChildCount();
for (int i=0; i<count; i++) {
          View t = view.getChildAt(i);
          if (t.getTag() != null) {
            final Bitmap rawBitmap = Http.bitmapLoader(mDataList.get(i).getSmallLink());
            InboxListAdapter.ViewHolder holder = (InboxListAdapter.ViewHolder)t.getTag();
            holder.mAvatar.setImageBitmap(rawBitmap);
          }
      }
 */

/*
private void setImageToQueue(final int position,final ImageView imageView) {
  mThreadsPool.execute(new Runnable() {
    @Override
    public void run() {
      final Bitmap rawBitmap = Http.bitmapLoader(mDataList.get(position).getLink());
      if(rawBitmap==null)
        return;
      //final Bitmap scaledBitmap = Bitmap.createScaledBitmap(rawBitmap,mBitmapWidth,mBitmapHeight,false);
      mCache.put(mDataList.get(position).getLink(),rawBitmap);
      imageView.post(new Runnable() {
        @Override
        public void run() {
          if(rawBitmap!=null)
            imageView.setImageBitmap(mCache.get(mDataList.get(position).getLink()));
          else
            imageView.setImageResource(R.drawable.im_black_square);
        }
      });
    }
  });
}
*/



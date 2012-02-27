package com.sonetica.topface.ui.profile;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.net.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView.ScaleType;

public class PhotoEroGalleryAdapter extends BaseAdapter implements  OnScrollListener  {
  // Data
  private boolean mOwner;
  private Context mContext;
  private LinkedList<Album> mAlbumList;
  private HashMap<Integer,Bitmap> mCache;
  private ExecutorService mThreadsPool;
  private boolean mBusy; 
  //---------------------------------------------------------------------------
  public PhotoEroGalleryAdapter(Context context,boolean bOwner) {
    mContext = context;
    mOwner = bOwner;
    mCache = new HashMap<Integer,Bitmap>();
    mAlbumList = new LinkedList<Album>();
    mThreadsPool = Executors.newFixedThreadPool(2);
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<Album> dataList) {
    mAlbumList = dataList;
  };
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mAlbumList.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public Object getItem(int position) {
    return mAlbumList.get(position);
  }
  //---------------------------------------------------------------------------
  @Override
  public long getItemId(int position) {
    return 0;
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position,View convertView,ViewGroup parent) {
    if(convertView == null) {
      convertView = new ProfileEroThumbView(mContext);
      ((ProfileEroThumbView)convertView).setScaleType(ScaleType.CENTER_CROP);
      ((ProfileEroThumbView)convertView).mOwner = mOwner;
      convertView.setBackgroundResource(R.drawable.profile_bg_gallery);
    }

    
    if(position==0 && mOwner==true) {
      ((ProfileEroThumbView)convertView).mIsAddButton = true;
      ((ProfileEroThumbView)convertView).setPadding(0,0,0,20);
      ((ProfileEroThumbView)convertView).setScaleType(ScaleType.CENTER_INSIDE);
      ((ProfileEroThumbView)convertView).setImageResource(R.drawable.profile_add_photo);
      return convertView;
    } else
      ((ProfileEroThumbView)convertView).mIsAddButton = false;
    
    Bitmap bitmap = mCache.get(position);
    if(bitmap!=null)
      ((ProfileEroThumbView)convertView).setImageBitmap(bitmap);
    else {
      ((ProfileEroThumbView)convertView).setImageResource(R.drawable.profile_bg_gallery);
      loadingImage(position,((ProfileEroThumbView)convertView));
    }
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  private void loadingImage(final int position,final ProfileEroThumbView view) {
    final Album album = (Album)getItem(position);
    view.cost = album.cost;
    view.likes = album.likes;
    view.dislikes = album.dislikes;
    mThreadsPool.execute(new Runnable() {
      @Override
      public void run() {
        if(!mBusy) {
          final Bitmap bitmap = Http.bitmapLoader(album.getSmallLink());
          if(bitmap!=null)
            mCache.put(position,bitmap);
            view.post(new Runnable() {
              @Override
              public void run() {
                view.setImageBitmap(bitmap);
              }
            });
        }
      }
    });
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount) {
  }
  //---------------------------------------------------------------------------
  @Override
  public void onScrollStateChanged(AbsListView view,int scrollState) {
    switch(scrollState) {
      case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
        mBusy = true;
        break;
      case OnScrollListener.SCROLL_STATE_FLING:
        mBusy = true;
        break;
      case OnScrollListener.SCROLL_STATE_IDLE:
        mBusy = false;
        view.invalidateViews(); //  ПРАВИЛЬНО ???
        break;
    }
  }
  //---------------------------------------------------------------------------
}

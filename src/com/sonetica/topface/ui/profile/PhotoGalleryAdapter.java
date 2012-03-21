package com.sonetica.topface.ui.profile;

import java.util.HashMap;
import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.LeaksManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView.ScaleType;

public class PhotoGalleryAdapter extends BaseAdapter implements  OnScrollListener  {
  // Data
  private boolean mOwner;
  private Context mContext;
  private LinkedList<Album> mAlbumList;
  private HashMap<Integer,Bitmap> mCache;
  //private ExecutorService mThreadsPool;
  private boolean mBusy; 
  //---------------------------------------------------------------------------
  public PhotoGalleryAdapter(Context context,boolean bOwner) {
    mContext = context;
    mOwner = bOwner;
    mCache = new HashMap<Integer,Bitmap>();
    mAlbumList = new LinkedList<Album>();
    //mThreadsPool = Executors.newFixedThreadPool(2);
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<Album> dataList) {
    mAlbumList = dataList;
    mCache.clear();
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
    Debug.log(">>>>>>>>>>>>>>>>>>>>>>","HORIZONT convertView:"+convertView);
    
    if(convertView == null) {
      convertView = new ProfileThumbView(mContext);
      ((ProfileThumbView)convertView).setScaleType(ScaleType.CENTER_CROP);
      //convertView.setBackgroundResource(R.drawable.profile_bg_gallery);
    }

    if(position==0 && mOwner==true) {
      ((ProfileThumbView)convertView).mIsAddButton = true;
      ((ProfileThumbView)convertView).setPadding(0,0,0,20);
      ((ProfileThumbView)convertView).setScaleType(ScaleType.CENTER_INSIDE);
      ((ProfileThumbView)convertView).setImageResource(R.drawable.profile_add_photo);
      return convertView;
    } else
      ((ProfileThumbView)convertView).mIsAddButton = false;
    
    Bitmap bitmap = mCache.get(position);
    if(bitmap!=null)
      ((ProfileThumbView)convertView).setImageBitmap(bitmap);
    else {
      //((ProfileThumbView)convertView).setImageResource(R.drawable.profile_bg_gallery);
      loadingImage(position,((ProfileThumbView)convertView));
    }
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  private void loadingImage(final int position,final ProfileThumbView view) {
    final Album album = (Album)getItem(position);
    //mThreadsPool.execute(new Runnable() {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        if(!mBusy) {
          final Bitmap bitmap = Http.bitmapLoader(album.getSmallLink());
          if(bitmap!=null && mCache!=null)
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
    LeaksManager.getInstance().monitorObject(t);
    t.start();
  }
  //---------------------------------------------------------------------------
  public void release() {
    mContext=null;
    if(mAlbumList!=null)
      mAlbumList.clear();
    mAlbumList=null;
    if(mCache!=null)
      mCache.clear();
    mCache=null;    
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

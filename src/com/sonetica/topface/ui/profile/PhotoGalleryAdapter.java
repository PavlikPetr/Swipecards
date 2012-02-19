package com.sonetica.topface.ui.profile;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.net.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView.ScaleType;

public class PhotoGalleryAdapter extends BaseAdapter {
  // Data
  private Context mContext;
  private LinkedList<Album> mAlbumList;
  private HashMap<Integer,Bitmap> mCache;
  private ExecutorService mThreadsPool;
  private boolean mBusy; 
  //---------------------------------------------------------------------------
  public PhotoGalleryAdapter(Context context) {
    mContext = context;
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
      convertView = new ProfileThumbView(mContext);
      ((ProfileThumbView)convertView).setScaleType(ScaleType.CENTER_CROP);
      convertView.setBackgroundColor(Color.BLUE);
    }
    
    loadImage(position,((ProfileThumbView)convertView));
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  private void loadImage(int position,ProfileThumbView view) {
    Album album = (Album)getItem(position);
    Http.imageLoaderExp(album.getSmallLink(),view);
  }
  //---------------------------------------------------------------------------
}

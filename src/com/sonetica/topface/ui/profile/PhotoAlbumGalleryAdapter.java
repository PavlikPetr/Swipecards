package com.sonetica.topface.ui.profile;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.LeaksManager;
import com.sonetica.topface.utils.MemoryCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class PhotoAlbumGalleryAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  //---------------------------------------------------------------------------
  static class ViewHolder {
    ImageView mImageView;
  };
  //---------------------------------------------------------------------------
  // Data
  private int mPrevPosition;         // предыдущая позиция фото в альбоме
  private int mPreRunning;           // текущая пред загружаемое фото
  private MemoryCache mCache;        // кеш фоток
  private LinkedList<Album> mAlbumsList;
  private LayoutInflater mInflater;          
  //---------------------------------------------------------------------------
  public PhotoAlbumGalleryAdapter(Context context,LinkedList<Album> albumList) {
    mAlbumsList = albumList;
    mInflater = LayoutInflater.from(context);
    mCache = new MemoryCache();
  }
  //---------------------------------------------------------------------------
  public int getCount() {
    return mAlbumsList.size();
  }
  //---------------------------------------------------------------------------
  public Object getItem(int position) {
    return mAlbumsList.get(position);
  }
  //---------------------------------------------------------------------------
  public long getItemId(int position) {
    return position;
  }
  //---------------------------------------------------------------------------
  public View getView(final int position,View convertView, ViewGroup parent) {
    ViewHolder holder = null;
    
    if(convertView==null) {
      holder = new ViewHolder();
      convertView = (ViewGroup)mInflater.inflate(R.layout.item_album_gallery, null, false);
      holder.mImageView = (ImageView)convertView.findViewById(R.id.ivPreView);
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();
    
    Bitmap bitmap = mCache.get(position);
    if(bitmap!=null && position==0) {
      holder.mImageView.setImageBitmap(bitmap);
    } else if(bitmap!=null && position!=0)
      holder.mImageView.setImageBitmap(bitmap);
    else {
      loadingImage(position, holder.mImageView);
    }
    
    int prePosition = position>=mPrevPosition ? position+1 : position-1;
    if(prePosition>0 && position<(getCount()-1))
      preLoading(prePosition);
    
    mPrevPosition = position;

    return convertView;
  }
  //---------------------------------------------------------------------------
  public void loadingImage(final int position,final ImageView view) {
    Thread t = new Thread() {
      @Override
      public void run() {
        final Bitmap rawBitmap = Http.bitmapLoader(mAlbumsList.get(position).getBigLink());
        view.post(new Runnable() {
          @Override
          public void run() {
            if(rawBitmap!=null)
              view.setImageBitmap(rawBitmap);
            else
              view.setImageResource(R.drawable.icon_people);
          }
        });
        if(mCache!=null && rawBitmap!=null)
          mCache.put(position,rawBitmap);
      }
    };
    t.setPriority(Thread.MAX_PRIORITY);
    LeaksManager.getInstance().monitorObject(t);
    t.start();
  }
  //---------------------------------------------------------------------------
  public void preLoading(final int position) {
    if(position==mPreRunning)
      return;
    
    if(mCache.containsKey(position))
      return;
    
    Debug.log(this,"preloader:"+mPrevPosition+":"+position);
    
    Thread t = new Thread() {
      @Override
      public void run() {
        Bitmap rawBitmap = Http.bitmapLoader(mAlbumsList.get(position).getBigLink());
        if(mCache!=null)
          mCache.put(position,rawBitmap);
      }
    };
    t.setPriority(Thread.MIN_PRIORITY);
    LeaksManager.getInstance().monitorObject(t);
    t.start();
    
    mPreRunning = position;
  }
  //---------------------------------------------------------------------------
  public void release() {
    mCache.clear();
    mCache = null;
  }
  //---------------------------------------------------------------------------
}

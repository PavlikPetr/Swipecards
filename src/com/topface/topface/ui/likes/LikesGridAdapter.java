package com.topface.topface.ui.likes;

import com.topface.topface.R;
import com.topface.topface.data.Like;
import com.topface.topface.ui.GalleryGridManager;
import com.topface.topface.ui.ThumbView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/*
 *  Класс адаптера для отображения галереи в Like активити
 */
public class LikesGridAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  //---------------------------------------------------------------------------
  static class ViewHolder {
    ThumbView mThumbView;
  };
  //---------------------------------------------------------------------------
  // Data
  Context mContext;
  private LayoutInflater mInflater;
  private GalleryGridManager<Like> mGalleryManager;
  //---------------------------------------------------------------------------
  public LikesGridAdapter(Context context,GalleryGridManager<Like> galleryManager) {
    mContext = context;
    mInflater = LayoutInflater.from(context);
    mGalleryManager = galleryManager;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mGalleryManager.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position,View convertView,ViewGroup parent) {
    ViewHolder holder = null;
    
    if(convertView==null) {
      convertView = (ViewGroup)mInflater.inflate(R.layout.item_grid_gallery, null, false);
      holder = new ViewHolder();
      holder.mThumbView = (ThumbView)convertView.findViewById(R.id.ivTG);
      holder.mThumbView.setMinimumWidth(mGalleryManager.mBitmapWidth);
      holder.mThumbView.setMinimumHeight(mGalleryManager.mBitmapHeight);
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();

    Like like = ((Like)mGalleryManager.get(position));
    
    holder.mThumbView.mAge    = like.age;
    holder.mThumbView.mName   = like.first_name;
    holder.mThumbView.mOnline = like.online;
    holder.mThumbView.mCity   = like.city_id;
    
    mGalleryManager.getImage(position,holder.mThumbView);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  @Override
  public Object getItem(int position) {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  public long getItemId(int position) {
    return position;
  }
  //---------------------------------------------------------------------------
  public void release() {
    mContext=null;
    mInflater=null;
    mGalleryManager=null;
  }
  //---------------------------------------------------------------------------
}

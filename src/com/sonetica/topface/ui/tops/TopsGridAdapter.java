package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.ui.ThumbView;
import com.sonetica.topface.ui.GalleryGridManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/*
 * Класс адаптера для отображения галереи в Топ активити
 */
public class TopsGridAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  //---------------------------------------------------------------------------
  static class ViewHolder {
    ThumbView mThumbView;
  };
  //---------------------------------------------------------------------------
  // Data
  private LayoutInflater mInflater;
  private GalleryGridManager<TopUser> mGalleryManager;
  //---------------------------------------------------------------------------
  public TopsGridAdapter(Context context,GalleryGridManager<TopUser> galleryManager) {
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

    holder.mThumbView.mPercent = ((TopUser)mGalleryManager.get(position)).liked;
    
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
}

package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.ui.ThumbView;
import com.sonetica.topface.ui.GalleryManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/*
 * Класс адаптера для отображения галереи в Топ активити
 */
public class TopsGridAdapter extends BaseAdapter {
  // Data
  private LayoutInflater mInflater;
  private GalleryManager mGalleryManager;
  // class ViewHolder
  static class ViewHolder {
    ThumbView mThumbButton;
  };
  //---------------------------------------------------------------------------
  public TopsGridAdapter(Context context,GalleryManager galleryManager) {
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
      holder = new ViewHolder();
      convertView = (ViewGroup)mInflater.inflate(R.layout.gallery_item, null, false);
      holder.mThumbButton = (ThumbView)convertView.findViewById(R.id.ivTG);
      holder.mThumbButton.setMinimumWidth(mGalleryManager.mBitmapWidth);
      holder.mThumbButton.setMinimumHeight(mGalleryManager.mBitmapHeight);
      //holder.miv.setScaleType(ScaleType.CENTER);
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();

    holder.mThumbButton.mPercent = ((TopUser)mGalleryManager.get(position)).liked; 

    mGalleryManager.getImage(position,holder.mThumbButton);
    
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

package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.GalleryManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class RateitGalleryAdapter extends BaseAdapter {
  // Data
  private LayoutInflater mInflater;
  private GalleryManager mGalleryManager; // менеджер изображений
  //---------------------------------------------------------------------------
  public RateitGalleryAdapter(Context context,GalleryManager galleryManager) {
    mGalleryManager = galleryManager;
    mInflater = LayoutInflater.from(context);
  }
  //---------------------------------------------------------------------------
  public int getCount() {
    return mGalleryManager.size();
  }
  //---------------------------------------------------------------------------
  public Object getItem(int position) {
    return position;
  }
  //---------------------------------------------------------------------------
  public long getItemId(int position) {
    return position;
  }
  //---------------------------------------------------------------------------
  public View getView(final int position,View convertView, ViewGroup parent) {
    if(convertView == null) {
      convertView = (ImageView)mInflater.inflate(R.layout.rateit_gallery_item, null, false);
      convertView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }
    
    if(mGalleryManager.size()==0)
      return convertView;
    
    mGalleryManager.getImage(position,(ImageView)convertView);
    mGalleryManager.preload(position+1);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
}

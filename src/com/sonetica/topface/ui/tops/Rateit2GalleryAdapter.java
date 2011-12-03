package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.GalleryManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class Rateit2GalleryAdapter extends BaseAdapter {
  // Data
  private Context mContext;
  private GalleryManager mGalleryManager; // менеджер изображений
  //---------------------------------------------------------------------------
  public Rateit2GalleryAdapter(Context context,GalleryManager bitmapManager) {
    mContext = context;
    mGalleryManager = bitmapManager;
  }
  //---------------------------------------------------------------------------
  public int getCount() {
    return mGalleryManager.getSize();
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
      convertView = (ImageView)View.inflate(mContext, R.layout.rateit2_gallery_item, null);
      convertView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }
    
    if(mGalleryManager.getSize()==0)
      return convertView;
    
    mGalleryManager.getImage(position,(ImageView)convertView);
    mGalleryManager.preload(position+1);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
}

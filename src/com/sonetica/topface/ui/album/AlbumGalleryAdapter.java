package com.sonetica.topface.ui.album;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class AlbumGalleryAdapter extends BaseAdapter {
  // Data
  private LayoutInflater mInflater;
  private AlbumGalleryManager mGalleryManager; // менеджер изображений
  // class ViewHolder
  static class ViewHolder {
    ImageView mImageView;
  };
  //---------------------------------------------------------------------------
  public AlbumGalleryAdapter(Context context,AlbumGalleryManager galleryManager) {
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
    ViewHolder holder = null;
    if(convertView==null) {
      holder = new ViewHolder();
      convertView = (ViewGroup)mInflater.inflate(R.layout.album_gallery_item, null, false);
      convertView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
      holder.mImageView = (ImageView)convertView.findViewById(R.id.ivPreView);
      Debug.log(null,"new view");
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();
    
    if(mGalleryManager.size()==0)
      return convertView;
    
    mGalleryManager.getImage(position,holder.mImageView);
    mGalleryManager.preload(position+1);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
}

package com.sonetica.topface.ui.likes;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.GalleryManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/*
 * Класс адаптера для отображения галереи в Топ активити
 */
public class LikesGridAdapter extends BaseAdapter {
  // Data
  Context mContext;
  private LayoutInflater mInflater;
  private GalleryManager mGalleryManager;
  // class ViewHolder
  static class ViewHolder {
    ImageView mTopButton;
  };
  //---------------------------------------------------------------------------
  public LikesGridAdapter(Context context,GalleryManager galleryManager) {
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
      holder = new ViewHolder();
      //convertView = (ViewGroup)mInflater.inflate(R.layout.tops_gallery_item, null, false);
      convertView = new ImageView(mContext);
      convertView.setMinimumWidth(80);
      convertView.setMinimumHeight(100);
      //holder.miv.setScaleType(ScaleType.CENTER);
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();

    //holder.mTopButton.mPercent = mGalleryCachedManager.get(position).liked; 

    mGalleryManager.getImage(position,(ImageView)convertView);
    
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

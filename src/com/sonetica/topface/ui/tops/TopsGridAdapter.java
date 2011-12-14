package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.GalleryCachedManager;
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
  private GalleryCachedManager mGalleryCachedManager;
  // class ViewHolder
  static class ViewHolder {
    TopButton mTopButton;
  };
  //---------------------------------------------------------------------------
  public TopsGridAdapter(Context context,GalleryCachedManager galleryManager) {
    mInflater = LayoutInflater.from(context);
    mGalleryCachedManager = galleryManager;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mGalleryCachedManager.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position,View convertView,ViewGroup parent) {
    ViewHolder holder = null;
    if(convertView==null) {
      holder = new ViewHolder();
      convertView = (ViewGroup)mInflater.inflate(R.layout.tops_gallery_item, null, false);
      holder.mTopButton = (TopButton)convertView.findViewById(R.id.ivTG);
      holder.mTopButton.setMinimumWidth(mGalleryCachedManager.mBitmapWidth);
      holder.mTopButton.setMinimumHeight(mGalleryCachedManager.mBitmapHeight);
      //holder.miv.setScaleType(ScaleType.CENTER);
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();

    holder.mTopButton.mPercent = mGalleryCachedManager.get(position).liked; 

    mGalleryCachedManager.getImage(position,holder.mTopButton);
    
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

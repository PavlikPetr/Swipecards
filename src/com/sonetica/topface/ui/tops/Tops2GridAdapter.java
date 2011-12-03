package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.GalleryCachedManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/*
 * Класс адаптера для отображения галереи в Топ активити
 */
public class Tops2GridAdapter extends BaseAdapter {
  // Data
  private LayoutInflater mInflater;
  private GalleryCachedManager mGalleryCachedManager;
  // class ViewHolder
  static class ViewHolder {
    ImageView miv;
  };
  //---------------------------------------------------------------------------
  public Tops2GridAdapter(Context context,GalleryCachedManager galleryCachedManager) {
    mInflater = LayoutInflater.from(context);
    mGalleryCachedManager = galleryCachedManager;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mGalleryCachedManager.getSize();
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position,View convertView,ViewGroup parent) {
    ViewHolder holder = null;
    if(convertView == null) {
      holder = new ViewHolder();
      convertView = (ViewGroup)mInflater.inflate(R.layout.tops_gallery_item, null, false);
      holder.miv = (ImageView)convertView.findViewById(R.id.ivTG);
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();
    
   // holder.miv.setImageResource(R.drawable.ic_launcher);
    
    mGalleryCachedManager.getImage(position,holder.miv);
    
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

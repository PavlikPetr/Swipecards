package com.sonetica.topface.ui.tops;

import com.sonetica.topface.utils.GalleryManager;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/*
 * Класс адаптера для отображения галереи в Топ активити
 */
public class TopsGridAdapter extends BaseAdapter {
  // Data
  private Context mContext;
  private GalleryManager mBitmapManager; // менеджер изображений
  //---------------------------------------------------------------------------
  public TopsGridAdapter(Context context, GalleryManager bitmapManager) {
    mContext = context;
    mBitmapManager = bitmapManager;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mBitmapManager.getSize();
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position,View convertView,ViewGroup parent) {
    if(convertView == null) {
      convertView = new TopButton(mContext);
      convertView.setMinimumWidth(120);
      convertView.setMinimumHeight(160);
    }
    ((TopButton)convertView).setImageBitmap(null);

    mBitmapManager.getImage(position, (ImageView)convertView);

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
    return 0;
  }
  //---------------------------------------------------------------------------
}

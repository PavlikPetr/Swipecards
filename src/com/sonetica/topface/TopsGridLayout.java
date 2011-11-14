package com.sonetica.topface;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/*
 * Класс адаптера для отображения галлереи в Топ активити
 */
public class TopsGridLayout extends BaseAdapter {
  // Data
  Context mContext;
  //---------------------------------------------------------------------------
  public TopsGridLayout(Context context) {
    mContext = context;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return 40;
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
  @Override
  public View getView(int position,View convertView,ViewGroup parent) {
    ImageView iv = new ImageView(mContext);
    iv.setImageResource(R.drawable.ic_launcher);
    return iv;
  }
  //---------------------------------------------------------------------------
}

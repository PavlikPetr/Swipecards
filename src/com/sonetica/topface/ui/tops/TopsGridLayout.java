package com.sonetica.topface.ui.tops;

import java.util.ArrayList;
import com.sonetica.topface.R;
import com.sonetica.topface.R.drawable;
import com.sonetica.topface.net.BitmapManager;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * Класс адаптера для отображения галлереи в Топ активити
 */
public class TopsGridLayout extends BaseAdapter {
  // Data
  private Context mContext;
  private BitmapManager mBitmapManager;
  // ViewHolder
  static class ViewHolder {
    TopButton mAvatar;
  }
  //---------------------------------------------------------------------------
  public TopsGridLayout(Context context, ArrayList<String> urlList) {
    mContext = context;
    mBitmapManager = new BitmapManager(context,urlList);
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mBitmapManager.getSize();
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
    final ViewHolder holder;
    if(convertView == null) {
      holder = new ViewHolder();
    } else
      holder = (ViewHolder)convertView.getTag();
    
    //ImageView iv = new ImageView(mContext);
    //iv.setImageResource(R.drawable.ic_launcher);
    convertView = new TopButton(mContext);
    convertView.setMinimumWidth(115);
    convertView.setMinimumHeight(115);
    // перелоадер
    ((ImageView)convertView).setImageResource(R.drawable.ic_launcher);
    mBitmapManager.getBitmap(position,convertView);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
}

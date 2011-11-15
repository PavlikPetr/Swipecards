package com.sonetica.topface.tops;

import java.util.ArrayList;
import com.sonetica.topface.R;
import com.sonetica.topface.R.drawable;
import com.sonetica.topface.net.BitmapManager;
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
  private Context mContext;
  private BitmapManager mBitmapManager;
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
    //ImageView iv = new ImageView(mContext);
    //iv.setImageResource(R.drawable.ic_launcher);
    convertView = new ImageView(mContext);
    convertView.setMinimumWidth(115);
    convertView.setMinimumHeight(115);
    convertView.setBackgroundResource(R.drawable.ic_launcher);
    mBitmapManager.getBitmap(position,convertView);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
}

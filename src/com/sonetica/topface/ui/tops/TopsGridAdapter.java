package com.sonetica.topface.ui.tops;

import java.util.ArrayList;
import com.sonetica.topface.data.User;
import com.sonetica.topface.utils.GalleryCachedManager;
import com.sonetica.topface.utils.IFrame;
import android.content.Context;
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
  private GalleryCachedManager mGalleryCachedManager;
  //---------------------------------------------------------------------------
  public TopsGridAdapter(Context context,ArrayList<User> userList) {
    mContext = context;
    mGalleryCachedManager = null;//new GalleryCachedManager(this,IFrame.TOPS,userList/*,4*/);
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mGalleryCachedManager.getSize();
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position,View convertView,ViewGroup parent) {
    //if(convertView == null) {
      convertView = new TopButton(mContext);
      convertView.setMinimumWidth(120);
      convertView.setMinimumHeight(160);
    //}
    ((TopButton)convertView).setImageBitmap(null);

    //mGalleryCachedManager.getImage(position,((TopButton)convertView).iv);

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

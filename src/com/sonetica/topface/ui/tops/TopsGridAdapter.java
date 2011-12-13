package com.sonetica.topface.ui.tops;

import java.util.ArrayList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.User;
import com.sonetica.topface.utils.GalleryCachedManager;
import com.sonetica.topface.utils.IFrame;
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
  private ArrayList<User> mUserList;
  private GalleryCachedManager mGalleryCachedManager;
  // class ViewHolder
  static class ViewHolder {
    TopButton miv;
  };
  //---------------------------------------------------------------------------
  public TopsGridAdapter(Context context,ArrayList<User> userList) {
    mUserList = userList;
    mInflater = LayoutInflater.from(context);
    mGalleryCachedManager = new GalleryCachedManager(context,IFrame.TOPS,userList);
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mUserList.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position,View convertView,ViewGroup parent) {
    ViewHolder holder = null;
    if(convertView==null) {
      holder = new ViewHolder();
      convertView = (ViewGroup)mInflater.inflate(R.layout.tops_gallery_item, null, false);
      holder.miv  = (TopButton)convertView.findViewById(R.id.ivTG);
      //holder.miv.setMinimumWidth(mContext.getResources().getInteger(R.integer.tops_width));
      holder.miv.setMinimumWidth(80);
      //holder.miv.setMinimumHeight(mContext.getResources().getInteger(R.integer.tops_height));
      holder.miv.setMinimumHeight(100);
      //holder.miv.setScaleType(ScaleType.CENTER);
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();

    User user = mUserList.get(position);
    holder.miv.mPercent = user.liked; 

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

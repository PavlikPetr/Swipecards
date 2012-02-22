package com.sonetica.topface.ui.dating;

import com.sonetica.topface.R;
import com.sonetica.topface.data.SearchUser;
import com.sonetica.topface.net.Http;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class DatingGalleryAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  //---------------------------------------------------------------------------
  static class ViewHolder {
    ImageView mImageView;
  };
  //---------------------------------------------------------------------------
  // Data
  private SearchUser mUserData;
  private LayoutInflater mInflater;
  //---------------------------------------------------------------------------
  public DatingGalleryAdapter(Context context) {
    mInflater = LayoutInflater.from(context);
  }
  //---------------------------------------------------------------------------
  public void setUserData(SearchUser user) {
    mUserData = user;
  }
  //---------------------------------------------------------------------------
  public int getCount() {
    if(mUserData==null)
      return 0;
    return mUserData.avatars_big.length;
  }
  //---------------------------------------------------------------------------
  public Object getItem(int position) {
    if(mUserData==null)
      return null;
    return mUserData.avatars_big[position];
  }
  //---------------------------------------------------------------------------
  public long getItemId(int position) {
    if(mUserData==null)
      return 0;
    return position;
  }
  //---------------------------------------------------------------------------
  public View getView(final int position,View convertView, ViewGroup parent) {
    ViewHolder holder = null;
    if(convertView == null) {
      holder = new ViewHolder();
      convertView = (ViewGroup)mInflater.inflate(R.layout.album_item_gallery, null, false);
      convertView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
      holder.mImageView = (ImageView)convertView.findViewById(R.id.ivPreView);
      convertView.setTag(holder);
    } else 
      holder = (ViewHolder)convertView.getTag();
    
    if(mUserData!=null)
      loadingImage((String)getItem(position), holder.mImageView);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  public void loadingImage(String url, ImageView view) {
    Http.imageLoaderExp(url,view); 
  }
  //---------------------------------------------------------------------------
}

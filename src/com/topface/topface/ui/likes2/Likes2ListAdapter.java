package com.topface.topface.ui.likes2;

import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.ui.AvatarManager;
import com.topface.topface.ui.RoundedImageView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class Likes2ListAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  //---------------------------------------------------------------------------
  static class ViewHolder {
    public RoundedImageView mAvatar;
    public TextView  mName;
    public TextView  mTime;
    public ImageView mHeart;
    public ImageView mArrow;
  }
  //---------------------------------------------------------------------------
  // Data
  private LayoutInflater mInflater;
  private AvatarManager<FeedLike> mAvatarManager;
  private int mOwnerCityID;
  // Constants
  private static final int T_ALL   = 0;
  private static final int T_CITY  = 1;
  private static final int T_COUNT = 2;
  //---------------------------------------------------------------------------
  public Likes2ListAdapter(Context context,AvatarManager<FeedLike> avatarManager) {
    mAvatarManager = avatarManager;
    mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mOwnerCityID = CacheProfile.city_id;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mAvatarManager.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public FeedLike getItem(int position) {
    return mAvatarManager.get(position);
  }
  //---------------------------------------------------------------------------
  @Override
  public long getItemId(int position) {
    return position;
  }
  //---------------------------------------------------------------------------
  @Override 
  public int getViewTypeCount() {
    return T_COUNT;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getItemViewType(int position) {
    return mAvatarManager.get(position).city_id==mOwnerCityID ? T_CITY : T_ALL;
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    
    int type = getItemViewType(position);

    if(convertView==null) {
      holder = new ViewHolder();
      
      convertView = mInflater.inflate(R.layout.item_likes2_gallery, null, false);
      
      holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.ivAvatar);
      holder.mName   = (TextView)convertView.findViewById(R.id.tvName);
      holder.mTime   = (TextView)convertView.findViewById(R.id.tvTime);
      holder.mHeart  = (ImageView)convertView.findViewById(R.id.ivHeart);
      holder.mArrow  = (ImageView)convertView.findViewById(R.id.ivArrow);
      
      switch(type) {
        case T_ALL:
          convertView.setBackgroundResource(R.drawable.gallery_item_all_selector);
          break;
        case T_CITY:
          convertView.setBackgroundResource(R.drawable.gallery_item_city_selector);
          break;
      }

      convertView.setTag(holder);
    } else
      holder = (ViewHolder)convertView.getTag();
    

    mAvatarManager.getImage(position,holder.mAvatar);

    FeedLike likes = getItem(position);
    holder.mName.setText(likes.first_name+" "+likes.age+", "+likes.city_name);
    Utils.formatTime(holder.mTime,likes.created);
    holder.mArrow.setImageResource(R.drawable.im_item_gallery_arrow);
    if(likes.rate==10)
      holder.mHeart.setImageResource(R.drawable.mutual_heart_top);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  public void release() {
    mInflater=null;
    mAvatarManager=null;
  }
  //---------------------------------------------------------------------------
}

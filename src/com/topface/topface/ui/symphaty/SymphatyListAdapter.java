package com.topface.topface.ui.symphaty;

import com.topface.topface.R;
import com.topface.topface.data.FeedSymphaty;
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

public class SymphatyListAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  //---------------------------------------------------------------------------
  static class ViewHolder {
    public RoundedImageView mAvatar;
    public TextView  mName;
    public TextView  mTime;
    public ImageView mArrow;
  }
  //---------------------------------------------------------------------------
  // Data
  private LayoutInflater mInflater;
  private AvatarManager<FeedSymphaty> mAvatarManager;
  private int mOwnerCityID;
  // Constants
  private static final int T_ALL   = 0;
  private static final int T_CITY  = 1;
  private static final int T_COUNT = 2;
  //---------------------------------------------------------------------------
  public SymphatyListAdapter(Context context,AvatarManager<FeedSymphaty> avatarManager) {
    mAvatarManager = avatarManager;
    mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //mInflater = LayoutInflater.from(context);
    mOwnerCityID = CacheProfile.city_id;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mAvatarManager.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public FeedSymphaty getItem(int position) {
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
      
      convertView = mInflater.inflate(R.layout.item_symphaty_gallery, null, false);
      
      holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.ivAvatar);
      holder.mName   = (TextView)convertView.findViewById(R.id.tvName);
      holder.mTime   = (TextView)convertView.findViewById(R.id.tvTime);
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
    
    FeedSymphaty symphaty = getItem(position);
    /*
    Bitmap bitmap = mAvatarManager.getImage(inbox.avatars_small);
    if(bitmap!=null) {
      holder.mAvatar.setImageBitmap(bitmap);
      holder.mAvatar.setTag(null);
    } else {
      holder.mAvatar.setImageResource(R.drawable.ic_launcher);
      holder.mAvatar.setTag(this);
    }
    */
    mAvatarManager.getImage(position,holder.mAvatar);
    
    holder.mName.setText(symphaty.first_name+", "+symphaty.age);
    Utils.formatTime(holder.mTime,symphaty.created);
    holder.mArrow.setImageResource(R.drawable.im_item_gallery_arrow);
    
    /*
    switch(inbox.type) {
      case Inbox.DEFAULT:
        holder.mText.setText(inbox.text);
        break;
      case Inbox.PHOTO:
        holder.mText.setText("PHOTO");
        break;
      case Inbox.GIFT:
        holder.mText.setText("GIFT");
        break;
      case Inbox.MESSAGE:
        holder.mText.setText(inbox.text);
        break;
      case Inbox.MESSAGE_WISH:
        holder.mText.setText("WISH");
        break;
      case Inbox.MESSAGE_SEXUALITY:
        holder.mText.setText("SEXUALITY");
        break;
    }
    */
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  public void release() {
    mInflater=null;
    mAvatarManager=null;
  }
  //---------------------------------------------------------------------------
}

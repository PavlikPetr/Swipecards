package com.sonetica.topface.ui.rates;

import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Rate;
import com.sonetica.topface.ui.AvatarManager;
import com.sonetica.topface.ui.RoundedImageView;
import com.sonetica.topface.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RatesListAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  //---------------------------------------------------------------------------
  public static class ViewHolder {
    public RoundedImageView mAvatar;
    public TextView  mName;
    public StarView  mStar;
    public TextView  mTime;
    public ImageView mArrow;
  }
  //---------------------------------------------------------------------------
  // Data
  private LayoutInflater mInflater;
  private AvatarManager<Rate> mAvatarManager;
  private int mOwnerCityID;
  // Constants
  private static final int T_ALL   = 0;
  private static final int T_CITY  = 1;
  private static final int T_COUNT = 2;
  //private static final String TIME_TEMPLATE = "dd MMM, kk:mm";
  //---------------------------------------------------------------------------
  public RatesListAdapter(Context context,AvatarManager<Rate> avatarManager) {
    mAvatarManager = avatarManager;
    mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    //mInflater = LayoutInflater.from(context);
    mOwnerCityID = Data.s_Profile.city_id;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mAvatarManager.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public Rate getItem(int position) {
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
      
      convertView = mInflater.inflate(R.layout.rates_item_gallery, null, false);
      
      holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.ivAvatar);
      holder.mName   = (TextView)convertView.findViewById(R.id.tvName);
      holder.mStar   = (StarView)convertView.findViewById(R.id.ivStar);
      holder.mTime   = (TextView)convertView.findViewById(R.id.tvTime);
      holder.mArrow  = (ImageView)convertView.findViewById(R.id.ivArrow);
      
      switch(type) {
        case T_ALL:
          convertView.setBackgroundResource(R.drawable.item_gallery_all_selector);
          break;
        case T_CITY:
          convertView.setBackgroundResource(R.drawable.item_gallery_city_selector);
          break;
      }

      convertView.setTag(holder);
    } else
      holder = (ViewHolder)convertView.getTag();
    
    Rate rate = getItem(position);
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
    
    holder.mName.setText(rate.first_name+", "+rate.age);
    holder.mStar.mRate = rate.rate;
    //holder.mTime.setText(DateFormat.format(TIME_TEMPLATE,inbox.created));
    Utils.formatTime(holder.mTime,rate.created);
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

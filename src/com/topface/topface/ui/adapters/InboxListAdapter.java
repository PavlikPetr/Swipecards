package com.topface.topface.ui.adapters;

import com.topface.topface.R;
import com.topface.topface.data.FeedInbox;
import com.topface.topface.ui.views.RoundedImageView;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InboxListAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  //---------------------------------------------------------------------------
  static class ViewHolder {
    public RoundedImageView mAvatar;
    public TextView  mName;
    public TextView  mText;
    public TextView  mTime;
    public ImageView mArrow;
  }
  //---------------------------------------------------------------------------
  // Data
  private Context mContext;
  private LayoutInflater mInflater;
  private AvatarManager<FeedInbox> mAvatarManager;
  private int mOwnerCityID;
  // Constants
  private static final int T_ALL   = 0;
  private static final int T_CITY  = 1;
  private static final int T_COUNT = 2;
  //private static final String TIME_TEMPLATE = "dd MMM, kk:mm";
  //---------------------------------------------------------------------------
  public InboxListAdapter(Context context,AvatarManager<FeedInbox> avatarManager) {
    mContext = context;
    mAvatarManager = avatarManager;
    //mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mInflater = LayoutInflater.from(context);
    mOwnerCityID = CacheProfile.city_id;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mAvatarManager.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public FeedInbox getItem(int position) {
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
      
      convertView = mInflater.inflate(R.layout.item_inbox_gallery, null, false);

      holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.ivAvatar);
      holder.mName   = (TextView)convertView.findViewById(R.id.tvName);
      holder.mText   = (TextView)convertView.findViewById(R.id.tvText);
      holder.mTime   = (TextView)convertView.findViewById(R.id.tvTime);
      holder.mArrow  = (ImageView)convertView.findViewById(R.id.ivArrow);
      
      switch(type) {
        case T_ALL:
          convertView.setBackgroundResource(R.drawable.item_all_selector);
          break;
        case T_CITY:
          convertView.setBackgroundResource(R.drawable.item_city_selector);
          break;
      }

      convertView.setTag(holder);
    } else
      holder = (ViewHolder)convertView.getTag();
    
    FeedInbox inbox = getItem(position);
    
    mAvatarManager.getImage(position, holder.mAvatar);
    holder.mName.setText(inbox.first_name + ", " + inbox.age + ", " + inbox.city_name);
    
    // text
    switch(inbox.type) {
      case FeedInbox.DEFAULT:
        holder.mText.setText(inbox.text);
        break;
      case FeedInbox.PHOTO:
        if(inbox.code>100500) {
          holder.mText.setText(mContext.getString(R.string.chat_money_in) + /*" " + msg.code +*/ ".");
          break;
        }
        holder.mText.setText(mContext.getString(R.string.chat_rate_in) + " " + inbox.code + ".");
        break;
      case FeedInbox.GIFT:
        holder.mText.setText(mContext.getString(R.string.chat_gift_in));
        break;
      case FeedInbox.MESSAGE:
        holder.mText.setText(inbox.text);
        break;
      case FeedInbox.MESSAGE_WISH:
        holder.mText.setText(mContext.getString(R.string.chat_wish_in));
        break;
      case FeedInbox.MESSAGE_SEXUALITY:
        holder.mText.setText(mContext.getString(R.string.chat_sexuality_in));
        break;
    }

    Utils.formatTime(holder.mTime,inbox.created);
    //holder.mArrow.setImageResource(R.drawable.im_item_arrow);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  public void release() {
    mInflater=null;
    mAvatarManager=null;
  }
  //---------------------------------------------------------------------------
}

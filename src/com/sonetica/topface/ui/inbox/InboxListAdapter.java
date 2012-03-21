package com.sonetica.topface.ui.inbox;

import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.ui.AvatarManager;
import com.sonetica.topface.ui.RoundedImageView;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Utils;
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
  public class ViewHolder {
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
  private AvatarManager<Inbox> mAvatarManager;
  private int mOwnerCityID;
  // Constants
  private static final int T_ALL   = 0;
  private static final int T_CITY  = 1; // PITER
  private static final int T_COUNT = 2;
  //private static final String TIME_TEMPLATE = "dd MMM, kk:mm";
  //---------------------------------------------------------------------------
  public InboxListAdapter(Context context,AvatarManager<Inbox> avatarManager) {
    mContext = context;
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
  public Inbox getItem(int position) {
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
    
    Debug.log(">>>>>>>>>>>>>>>>>>>>>>","GRID convertView:"+convertView); 
    
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
          convertView.setBackgroundResource(R.drawable.gallery_item_all_selector);
          break;
        case T_CITY:
          convertView.setBackgroundResource(R.drawable.gallery_item_city_selector);
          break;
      }

      convertView.setTag(holder);
    } else
      holder = (ViewHolder)convertView.getTag();
    
    Inbox inbox = getItem(position);
    /*
    // avatar
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
    holder.mName.setText(inbox.first_name+", "+inbox.age);
    
    // text
    switch(inbox.type) {
      case Inbox.DEFAULT:
        holder.mText.setText(inbox.text);
        break;
      case Inbox.PHOTO:
        holder.mText.setText(mContext.getString(R.string.chat_rate_in) + " " + inbox.code + ".");
        break;
      case Inbox.GIFT:
        holder.mText.setText(mContext.getString(R.string.chat_gift_in));
        break;
      case Inbox.MESSAGE:
        holder.mText.setText(inbox.text);
        break;
      case Inbox.MESSAGE_WISH:
        holder.mText.setText(mContext.getString(R.string.chat_wish_in));
        break;
      case Inbox.MESSAGE_SEXUALITY:
        holder.mText.setText(mContext.getString(R.string.chat_sexuality_in));
        break;
    }
    //holder.mTime.setText(DateFormat.format(TIME_TEMPLATE,inbox.created));
    Utils.formatTime(holder.mTime,inbox.created);
    holder.mArrow.setImageResource(R.drawable.im_item_gallery_arrow);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
  public void release() {
    mInflater=null;
    mAvatarManager=null;
  }
  //---------------------------------------------------------------------------
}

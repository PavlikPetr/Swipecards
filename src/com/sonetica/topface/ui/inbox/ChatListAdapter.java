package com.sonetica.topface.ui.inbox;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.History;
import com.sonetica.topface.ui.RoundedImageView;
import com.sonetica.topface.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatListAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // class ViewHolder
  static class ViewHolder {
      RoundedImageView mAvatar;
      TextView  mMessage;
      TextView  mDate;
  }
  //---------------------------------------------------------------------------
  // Data
  private Context mContext;
  private int mUserId;
  private LinkedList<History> mList;
  private View.OnClickListener mOnAvatarListener;
  private LayoutInflater mInflater;
  private LinkedList<Integer> mItemLayoutList = new LinkedList<Integer>();
  // Type Item
  private static final int T_USER_PHOTO   = 0;
  private static final int T_USER_EXT     = 1;
  private static final int T_FRIEND_PHOTO = 2;
  private static final int T_FRIEND_EXT   = 3;
  private static final int T_COUNT = 4;
  //---------------------------------------------------------------------------
  public ChatListAdapter(Context context,int userId,LinkedList<History> dataList) {
    mContext = context;
    mList = dataList;
    mUserId = userId;
    mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    prepare(dataList);
  }
  //---------------------------------------------------------------------------
  public void setOnAvatarListener(View.OnClickListener onAvatarListener){
    mOnAvatarListener = onAvatarListener;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mList.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public History getItem(int position) {
    return mList.get(position);
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
    return mItemLayoutList.get(position);
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    int type = getItemViewType(position);

    if(convertView == null) { 
      holder = new ViewHolder();
      switch(type) {
        case T_FRIEND_PHOTO: {
            convertView     = mInflater.inflate(R.layout.chat_friend, null, false);
            holder.mAvatar  = (RoundedImageView)convertView.findViewById(R.id.left_icon);
            holder.mMessage = (TextView)convertView.findViewById(R.id.chat_message);
            holder.mDate    = (TextView)convertView.findViewById(R.id.chat_date);
            holder.mAvatar.setOnClickListener(mOnAvatarListener);
            if(Data.s_UserDrw!=null)
              holder.mAvatar.setImageDrawable(Data.s_UserDrw);
        } break;
        case T_FRIEND_EXT: {
            convertView     = mInflater.inflate(R.layout.chat_friend_ext, null, false);
            holder.mAvatar  = (RoundedImageView)convertView.findViewById(R.id.left_icon);
            holder.mMessage = (TextView)convertView.findViewById(R.id.chat_message);
            holder.mDate    = (TextView)convertView.findViewById(R.id.chat_date);
        } break;
        case T_USER_PHOTO: {
            convertView     = mInflater.inflate(R.layout.chat_user, null, false);
            holder.mAvatar  = (RoundedImageView)convertView.findViewById(R.id.left_icon);
            holder.mMessage = (TextView)convertView.findViewById(R.id.chat_message);
            holder.mDate    = (TextView)convertView.findViewById(R.id.chat_date);
            holder.mAvatar.setImageDrawable(Data.s_OwnerDrw);
        } break;
        case T_USER_EXT: {
            convertView     = mInflater.inflate(R.layout.chat_user_ext, null, false);
            holder.mAvatar  = (RoundedImageView)convertView.findViewById(R.id.left_icon);
            holder.mMessage = (TextView)convertView.findViewById(R.id.chat_message);
            holder.mDate    = (TextView)convertView.findViewById(R.id.chat_date);
        } break;
      }
      
      convertView.setTag(holder);
      
    } else
      holder = (ViewHolder) convertView.getTag();
    
    History msg = getItem(position);

    switch(msg.type) {
      case History.DEFAULT:
        holder.mMessage.setText(msg.text);
        break;
      case History.PHOTO: {
        switch(type) {
          case T_FRIEND_PHOTO:
          case T_FRIEND_EXT:{ 
            holder.mMessage.setText(mContext.getString(R.string.chat_rate_in) + " " + msg.code + ".");
          } break;
          case T_USER_PHOTO:
          case T_USER_EXT: {
            holder.mMessage.setText(mContext.getString(R.string.chat_rate_out) + " " + msg.code + ".");
          } break;
        }
      } break;
      case History.GIFT:
        switch(type) {
          case T_FRIEND_PHOTO:
          case T_FRIEND_EXT:{ 
            holder.mMessage.setText(mContext.getString(R.string.chat_gift_in));
          } break;
          case T_USER_PHOTO:
          case T_USER_EXT: {
            holder.mMessage.setText(mContext.getString(R.string.chat_gift_out));
          } break;
        }
        break;
      case History.MESSAGE:
        holder.mMessage.setText(msg.text);
        break;
      case History.MESSAGE_WISH:
        switch(type) {
          case T_FRIEND_PHOTO:
          case T_FRIEND_EXT:{ 
            holder.mMessage.setText(mContext.getString(R.string.chat_wish_in));
          } break;
          case T_USER_PHOTO:
          case T_USER_EXT: {
            holder.mMessage.setText(mContext.getString(R.string.chat_wish_out));
          } break;
        }
        break;
      case History.MESSAGE_SEXUALITY:
        switch(type) {
          case T_FRIEND_PHOTO:
          case T_FRIEND_EXT:{ 
            holder.mMessage.setText(mContext.getString(R.string.chat_sexuality_in));
          } break;
          case T_USER_PHOTO:
          case T_USER_EXT: {
            holder.mMessage.setText(mContext.getString(R.string.chat_sexuality_out));
          } break;
        }
        break;
    }
    
    Utils.formatTime(holder.mDate,msg.created);

    return convertView;
  }
  //---------------------------------------------------------------------------
  public void addSentMessage(History msg) {
    int position = mList.size()-1;
    if(position<0)
      mItemLayoutList.add(T_USER_PHOTO);
    else {
      History history = mList.get(mList.size()-1);
      if(history.owner_id == mUserId)
        mItemLayoutList.add(T_USER_PHOTO);
      else
        mItemLayoutList.add(T_USER_EXT);
    }
    
    mList.add(msg);
  }
  //---------------------------------------------------------------------------
  public void setDataList(LinkedList<History> dataList) {
    prepare(dataList);
    mList.addAll(dataList);
  }
  //---------------------------------------------------------------------------
  public void prepare(LinkedList<History> dataList) {
    int count = dataList.size();
    int prev_id = 0;
    for(int i=0;i<count;i++) {
      History history = dataList.get(i); 
      if(history.owner_id==mUserId)
        if(history.owner_id==prev_id)
          mItemLayoutList.add(T_FRIEND_EXT);
        else
          mItemLayoutList.add(T_FRIEND_PHOTO);
      else
        if(history.owner_id==prev_id)
          mItemLayoutList.add(T_USER_EXT);
        else
          mItemLayoutList.add(T_USER_PHOTO);
      prev_id = history.owner_id;
    }
  }
  //---------------------------------------------------------------------------
  public void release() {
    if(mList!=null)
      mList.clear();
    mList=null;
    mInflater=null;
    if(mItemLayoutList!=null)
      mItemLayoutList.clear();
    mItemLayoutList=null;
  }
  //---------------------------------------------------------------------------
}

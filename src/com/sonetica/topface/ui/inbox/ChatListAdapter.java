package com.sonetica.topface.ui.inbox;

import java.util.HashMap;
import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.History;
import com.sonetica.topface.data.Inbox;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatListAdapter extends BaseAdapter {
  //---------------------------------------------------------------------------
  // ViewHolder
  //---------------------------------------------------------------------------
  public static class ViewHolder {
      TextView  mName;
      ImageView mAvatar;
      ImageView mStatus;
      TextView  mMessage;
      ImageView mImage;
      TextView  mDate;
  }
  //---------------------------------------------------------------------------
  // Data
  private Context mContext;
  private LinkedList<History> mList;
  /* содержит тип итема для отрисовки необходимого слоя */
  private HashMap<Integer, Integer> mItemLayoutMap = new HashMap<Integer, Integer>();
  private LayoutInflater mInflater;
  // Type Item
  private static final int T_USER_PHOTO = 0;
  private static final int T_USER_EXT = 1;
  private static final int T_FRIEND_PHOTO = 2;
  private static final int T_FRIEND_EXT = 3;
  private static final int T_COUNT = 4;
  //---------------------------------------------------------------------------
  public ChatListAdapter(Context context,LinkedList<History> list) {
    mContext=context;
    mList=list;
    mInflater = LayoutInflater.from(context);
    
    int count=0;
    for(History msg : mList)
      if(msg.owner_id==32574380)
        mItemLayoutMap.put(count++,T_USER_PHOTO);
      else
        mItemLayoutMap.put(count++,T_FRIEND_PHOTO);
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
    return mItemLayoutMap.get(position);
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    int type = getItemViewType(position);

    if(convertView == null) { 
      holder = new ViewHolder();
      switch(type) {
        case T_FRIEND_PHOTO:
            convertView = mInflater.inflate(R.layout.message_user, null, false);
            holder.mName = (TextView) convertView.findViewById(R.id.name_entry);
            holder.mAvatar = (ImageView) convertView.findViewById(R.id.left_icon);
            holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
            holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
            break;
        case T_FRIEND_EXT:
            convertView = mInflater.inflate(R.layout.message_user_ext, null, false);
            holder.mName = (TextView) convertView.findViewById(R.id.name_entry);
            holder.mAvatar = (ImageView) convertView.findViewById(R.id.left_icon);
            holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
            holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
            break;
        case T_USER_PHOTO:
            convertView = mInflater.inflate(R.layout.message_friend, null, false);
            holder.mName = (TextView) convertView.findViewById(R.id.name_entry);
            holder.mAvatar = (ImageView) convertView.findViewById(R.id.left_icon);
            holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
            holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
            break;
        case T_USER_EXT:
            convertView = mInflater.inflate(R.layout.message_friend_ext, null, false);
            holder.mName = (TextView) convertView.findViewById(R.id.name_entry);
            holder.mAvatar = (ImageView) convertView.findViewById(R.id.left_icon);
            holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
            holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
            break;
      }
      
      convertView.setTag(holder);
      
    } else
      holder = (ViewHolder) convertView.getTag();
    
    
    History msg = getItem(position);
    
    //holder.mName.setText(msg.);
    //holder.mAvatar = (ImageView) convertView.findViewById(R.id.left_icon);
    holder.mMessage.setText(msg.text);
    holder.mDate.setText(""+msg.created);
    
    /*
    switch(msg.type) {
      case Inbox.DEFAULT:
        text = msg.text;
        break;
      case Inbox.PHOTO:
        text = " PHOTO ";
        break;
      case Inbox.GIFT:
        text = " GIFT ";
        break;
      case Inbox.MESSAGE:
        text = msg.text;
        break;
      case Inbox.MESSAGE_WISH:
        text = " WISH ";
        break;
      case Inbox.MESSAGE_SEXUALITY:
        text = " SEXUALITY ";
        break;
    }
    */    
    return convertView;
  }
  //---------------------------------------------------------------------------
}

package com.sonetica.topface.ui.inbox;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Inbox;
import com.sonetica.topface.utils.Debug;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InboxListAdapter extends BaseAdapter {
  // class ViewHolder
  public static class ViewHolder {
    ImageView mAvatar;
    TextView  mName;
    TextView  mText;
    TextView  mTime;
    ImageView mArrow;
  }
  // Data
  private LayoutInflater mInflater;
  private LinkedList<Inbox> mList;
  // Constants
  private static final int T_ALL   = 0;
  private static final int T_CITY  = 2; // PITER
  private static final int T_COUNT = 2;
  //---------------------------------------------------------------------------
  public InboxListAdapter(Context context,LinkedList<Inbox> list) {
    mList=list;
    //mInflater = LayoutInflater.from(context);
    mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mList.size();
  }
  //---------------------------------------------------------------------------
  @Override
  public Inbox getItem(int position) {
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
    return mList.get(position).city_id==T_CITY ? 1 : 0; // T_CITY
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    
    int type = getItemViewType(position);

    if(convertView==null) {
      holder = new ViewHolder();
      switch (type) {              // один лайаут, только смена бекграунда у рута
        case 0:
          convertView = mInflater.inflate(R.layout.item_gallery_all, null, false);
          holder.mAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
          holder.mName   = (TextView)convertView.findViewById(R.id.tvName);
          holder.mText   = (TextView)convertView.findViewById(R.id.tvText);
          holder.mTime   = (TextView)convertView.findViewById(R.id.tvTime);
          holder.mArrow  = (ImageView)convertView.findViewById(R.id.ivArrow);
          break;
        case 1:
          convertView = mInflater.inflate(R.layout.item_gallery_city, null, false);
          holder.mAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
          holder.mName   = (TextView)convertView.findViewById(R.id.tvName);
          holder.mText   = (TextView)convertView.findViewById(R.id.tvText);
          holder.mTime   = (TextView)convertView.findViewById(R.id.tvTime);
          holder.mArrow  = (ImageView)convertView.findViewById(R.id.ivArrow);
          break;
      }

      convertView.setTag(holder);
    } else
      holder = (ViewHolder)convertView.getTag();
    
    Inbox inbox = getItem(position);
      
    holder.mAvatar.setImageResource(R.drawable.ic_launcher);
    holder.mName.setText(inbox.first_name+", "+inbox.age);
    holder.mText.setText(inbox.text);
    holder.mTime.setText(""+inbox.created);
    holder.mArrow.setImageResource(R.drawable.im_item_gallery_arrow);
      
    
    /*
    Inbox msg = getItem(position);
    String text = null;
    
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

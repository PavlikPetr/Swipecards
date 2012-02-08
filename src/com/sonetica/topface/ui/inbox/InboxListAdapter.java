package com.sonetica.topface.ui.inbox;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Inbox;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InboxListAdapter extends BaseAdapter {
  // Data
  private LayoutInflater mInflater;
  private LinkedList<Inbox> mList;
  // class ViewHolder
  public static class ViewHolder {
    ImageView mAvatar;
    TextView  mName;
    TextView  mText;
    TextView  mTime;
    ImageView mArrow;
  }
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
    return 2;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getItemViewType(int position) {
    return position;
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    
    if(convertView==null) {
      holder = new ViewHolder();
      
      convertView = mInflater.inflate(R.layout.item_gallery, null, false);
      
      holder.mAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
      holder.mName   = (TextView)convertView.findViewById(R.id.tvName);
      holder.mText   = (TextView)convertView.findViewById(R.id.tvText);
      holder.mTime   = (TextView)convertView.findViewById(R.id.tvTime);
      holder.mArrow  = (ImageView)convertView.findViewById(R.id.ivArrow);
      
      convertView.setTag(holder);
    } else
      holder = (ViewHolder)convertView.getTag();
      
    holder.mAvatar.setImageResource(R.drawable.ic_launcher);
    holder.mName.setText("0");
    holder.mText.setText("1");
    holder.mTime.setText("2");
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

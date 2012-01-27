package com.sonetica.topface.ui.inbox;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Inbox;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class InboxListAdapter extends BaseAdapter {
  // Data
  private Context mContext;
  private LinkedList<Inbox> mList;
  //---------------------------------------------------------------------------
  public InboxListAdapter(Context context,LinkedList<Inbox> list) {
    mContext=context;
    mList=list;
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
    return 1;
  }
  //---------------------------------------------------------------------------
  @Override
  public int getItemViewType(int position) {
    return position;
  }
  //---------------------------------------------------------------------------
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if(convertView==null)
      convertView=new TextView(mContext);
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
    
    ((TextView)convertView).setText(text);
    return convertView;
  }
  //---------------------------------------------------------------------------
}

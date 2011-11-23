package com.sonetica.topface.ui.chat;

import java.util.List;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChatListAdapter extends ArrayAdapter {
  // Data
  private Context mContext;
  private List mList;
  //---------------------------------------------------------------------------
  public ChatListAdapter(Context context,List list) {
    super(context,0,list);
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
  public Object getItem(int position) {
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
    ((TextView)convertView).setText((String)getItem(position));
    return convertView;
  }
  //---------------------------------------------------------------------------
}

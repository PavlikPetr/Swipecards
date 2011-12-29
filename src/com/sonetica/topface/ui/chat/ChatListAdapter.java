package com.sonetica.topface.ui.chat;

import java.util.ArrayList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Inbox;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChatListAdapter extends ArrayAdapter<Inbox> {
  // Data
  private Context mContext;
  private ArrayList<Inbox>  mList;
  //---------------------------------------------------------------------------
  public ChatListAdapter(Context context,ArrayList<Inbox> list) {
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
    String text = msg.first_name+"("+msg.age+"):"+(msg.text!=null?msg.text:(msg.gift!=null?mContext.getString(R.string.chat_gift):"code"));
    ((TextView)convertView).setText(text);
    return convertView;
  }
  //---------------------------------------------------------------------------
}

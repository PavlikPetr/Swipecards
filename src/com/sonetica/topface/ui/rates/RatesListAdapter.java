package com.sonetica.topface.ui.rates;

import java.util.LinkedList;
import com.sonetica.topface.data.Rate;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RatesListAdapter extends BaseAdapter {
  // Data
  private Context mContext;
  private LinkedList<Rate> mList;
  //---------------------------------------------------------------------------
  public RatesListAdapter(Context context,LinkedList<Rate> list) {
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
  public Rate getItem(int position) {
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
      convertView = new TextView(mContext);

    Rate rate = getItem(position);
    String text = rate.first_name+"("+rate.age+"): "+rate.rate;
    ((TextView)convertView).setText(text);
    
    return convertView;
  }
  //---------------------------------------------------------------------------
}

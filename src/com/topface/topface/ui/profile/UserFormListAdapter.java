package com.topface.topface.ui.profile;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.data.User;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;
import com.topface.topface.utils.Triple;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserFormListAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private LinkedList<FormItem> mUserForms;
    
    private static final int T_TITLE = 0;
    private static final int T_DIVIDER = 1;
    private static final int T_DATA   = 2;
    private static final int T_COUNT  = 3;
    
    static class ViewHolder {
        public ImageView mState;
        public TextView mTitle;
        public TextView mHeader;
        public TextView mValue;
    }

    public UserFormListAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUserForms = new LinkedList<FormItem>();
        mItemLayoutList = new LinkedList<Integer>();
    }

    @Override
    public int getCount() {
        return mUserForms.size();
    }

    @Override
    public Triple<String, String, Boolean> getItem(int position) {
        return mUserForms.get(mFormKeys[position]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
    	return T_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0)
            return T_TITLE;
        return mFormKeys[position].equals("") ? T_TITLE : T_DATA;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);        
    	if (convertView == null) {
            holder = new ViewHolder();

            switch (type) {
                case T_DIVIDER:
                    convertView = mInflater.inflate(R.layout.item_divider, null, false);
                    break;
                case T_TITLE:
                case T_DATA:
                    convertView = mInflater.inflate(R.layout.item_user_list, null, false);
                    holder.mState  = (ImageView)convertView.findViewById(R.id.ivState);
                    holder.mTitle  = (TextView)convertView.findViewById(R.id.tvTitle);
                    holder.mHeader = (TextView)convertView.findViewById(R.id.tvHeader);
                    holder.mValue  = (TextView)convertView.findViewById(R.id.tvData);
                    break;
            }
            
            switch (type) {
                case T_TITLE:
                    convertView.setBackgroundResource(R.drawable.user_list_title_bg);
                    break;
                case T_DATA:
                    convertView.setBackgroundResource(R.drawable.user_list_cell_bg);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

    	Triple<String, String, Boolean> item = getItem(position);
    	
    	switch (type) {
            case T_TITLE:
                holder.mTitle.setText(item.first);
                holder.mState.setImageResource(R.drawable.user_title);
                break;
            case T_DATA:
                holder.mHeader.setText(item.first);
                holder.mValue.setText(item.second);
                if(item.third)
                    holder.mState.setImageResource(R.drawable.user_cell);  // GREEN POINT
                else
                    holder.mState.setImageResource(R.drawable.user_cell);
                break;
        }

//        if (likes.online)
//            holder.mOnline.setVisibility(View.VISIBLE);
//        else
//            holder.mOnline.setVisibility(View.INVISIBLE);
        
        return convertView;
    }
    
    public void setUserData(User user) {
        prepare(user);
    }
    
    private void prepare(User user) {
        FormInfo formInfo = new FormInfo(mContext, user);
    }
}

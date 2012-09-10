package com.topface.topface.ui.profile;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileFormListAdapter extends BaseAdapter {
    // Data
    private LayoutInflater mInflater;
    private LinkedList<FormItem> mProfileForms;
    
    // Constants
    private static final int T_HEADER  = 0;
    private static final int T_DIVIDER = 1;
    private static final int T_DATA    = 2;
    private static final int T_COUNT   = T_DATA + 1;
    
    // class ViewHolder
    private static class ViewHolder {
        public ImageView mState;
        public TextView mTitle;
        public TextView mHeader;
        public TextView mValue;
        public Button   mFill;
    }

    public ProfileFormListAdapter(Context context) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mProfileForms = CacheProfile.forms;
    }

    @Override
    public int getCount() {
        return mProfileForms.size();
    }

    @Override
    public FormItem getItem(int position) {
        return mProfileForms.get(position);
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
        switch (getItem(position).type) {
            case FormItem.HEADER:
                return T_HEADER;
            case FormItem.DATA:
                return T_DATA;
            case FormItem.DIVIDER:
                return T_DIVIDER;
            default:
              return T_HEADER;
        }
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
                case T_HEADER:
                case T_DATA:
                    convertView = mInflater.inflate(R.layout.item_user_list, null, false);
                    holder.mState  = (ImageView)convertView.findViewById(R.id.ivState);
                    holder.mTitle  = (TextView)convertView.findViewById(R.id.tvTitle);
                    holder.mHeader = (TextView)convertView.findViewById(R.id.tvHeader);
                    holder.mValue  = (TextView)convertView.findViewById(R.id.tvData);
                    holder.mFill   = (Button)convertView.findViewById(R.id.btnFill);
                    break;
            }
            
            switch (type) {
                case T_HEADER:
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

    	FormItem item = getItem(position);
    	
    	switch (type) {
            case T_HEADER:
                holder.mTitle.setText(item.title);
                holder.mState.setImageResource(R.drawable.user_title);
                break;
            case T_DATA:
                holder.mHeader.setText(item.title);
                if(item.data != null) {
                    holder.mValue.setText(item.data);
                    holder.mValue.setVisibility(View.VISIBLE);
                    holder.mFill.setVisibility(View.INVISIBLE);
                } else {
                    holder.mValue.setVisibility(View.INVISIBLE);
                    holder.mFill.setVisibility(View.VISIBLE);
                }
                holder.mState.setImageResource(R.drawable.user_cell);
                break;
        }
        
        return convertView;
    }
}

package com.topface.topface.ui.profile;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;

import java.util.LinkedList;

public class ProfileFormListAdapter extends BaseAdapter {
    // Data
    private LayoutInflater mInflater;
    private LinkedList<FormItem> mProfileForms;
    private View.OnClickListener mOnFillListener;

    // Constants
    private static final int T_HEADER = 0;
    private static final int T_DIVIDER = 1;
    private static final int T_DATA = 2;
    private static final int T_COUNT = T_DATA + 1;

    // class ViewHolder
    private static class ViewHolder {
        public ImageView mState;
        public TextView mTitle;
        public TextView mHeader;
        public TextView mValue;
        public Button mFill;
    }

    public ProfileFormListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                    holder.mState = (ImageView) convertView.findViewById(R.id.ivState);
                    holder.mHeader = (TextView) convertView.findViewById(R.id.tvHeader);
                    holder.mTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                    holder.mValue = (TextView) convertView.findViewById(R.id.tvValue);
                    holder.mFill = (Button) convertView.findViewById(R.id.btnFill);
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
            holder = (ViewHolder) convertView.getTag();
        }

        FormItem item = getItem(position);

        switch (type) {
            case T_HEADER:
                holder.mHeader.setText(item.title);
                holder.mState.setImageResource(R.drawable.user_header);
                break;
            case T_DATA:
                holder.mTitle.setText(item.title.toUpperCase());
                if (item.value != null) {
                	holder.mState.setImageResource(R.drawable.user_cell);
                    holder.mValue.setText(item.value);
                    holder.mValue.setVisibility(View.VISIBLE);
                    holder.mFill.setVisibility(View.INVISIBLE);
                } else {
                	holder.mState.setImageResource(R.drawable.user_cell_off);
                    holder.mValue.setVisibility(View.INVISIBLE);
                    holder.mFill.setVisibility(View.VISIBLE);
                    holder.mFill.setOnClickListener(mOnFillListener);
                    holder.mFill.setTag(item);
                }                
                holder.mState.setVisibility(View.VISIBLE);
                break;
        }                
        return convertView;
    }

    public void setOnFillListener(View.OnClickListener onFillListener) {
        mOnFillListener = onFillListener;
    }
}


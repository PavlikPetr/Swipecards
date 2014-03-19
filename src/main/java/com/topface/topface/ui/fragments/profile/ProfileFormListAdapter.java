package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.text.TextUtils;
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
    // Constants
    private static final int T_HEADER = 0;
    private static final int T_DIVIDER = 1;
    private static final int T_DATA = 2;
    private static final int T_STATUS = 3;
    private static final int T_COUNT = T_STATUS + 1;
    // Data
    private LayoutInflater mInflater;
    private LinkedList<FormItem> mProfileForms = new LinkedList<>();
    private View.OnClickListener mOnFillListener;

    public ProfileFormListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        refillData();
    }

    public void refillData() {
        mProfileForms.clear();
        if (CacheProfile.forms != null) {
            mProfileForms.addAll(CacheProfile.forms);
        }
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
            case FormItem.STATUS:
                return T_STATUS;
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
                    holder.state = (ImageView) convertView.findViewById(R.id.ivState);
                    holder.header = (TextView) convertView.findViewById(R.id.tvHeader);
                    holder.title = (TextView) convertView.findViewById(R.id.tvTitle);
                    holder.value = (TextView) convertView.findViewById(R.id.tvValue);
                    holder.fill = (Button) convertView.findViewById(R.id.btnFill);
                    break;
                case T_STATUS:
                    convertView = mInflater.inflate(R.layout.item_user_list, null, false);
                    holder.state = (ImageView) convertView.findViewById(R.id.ivState);
                    holder.header = (TextView) convertView.findViewById(R.id.tvHeader);
                    holder.title = (TextView) convertView.findViewById(R.id.tvTitle);
                    holder.value = (TextView) convertView.findViewById(R.id.tvValue);
                    holder.fill = (Button) convertView.findViewById(R.id.btnFill);
                    break;
            }

            if (convertView != null) {
                convertView.setTag(holder);
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FormItem item = getItem(position);

        switch (type) {
            case T_HEADER:
                holder.header.setText(item.title);
                holder.state.setImageResource(getHeaderPicture(item));
                holder.title.setVisibility(View.GONE);
                holder.fill.setVisibility(View.GONE);
                holder.value.setVisibility(View.GONE);
                break;
            case T_DATA:
                holder.header.setVisibility(View.GONE);
                holder.title.setText(item.title.toUpperCase());
                if (item.value != null && !TextUtils.isEmpty(item.value.trim()) && item.dataId != FormItem.NOT_SPECIFIED_ID) {
                    holder.state.setImageResource(R.drawable.user_cell);
                    holder.value.setText(item.value.toLowerCase());
                    holder.value.setVisibility(View.VISIBLE);
                    holder.fill.setVisibility(View.GONE);
                } else {
                    holder.state.setImageResource(R.drawable.user_cell_off);
                    holder.value.setVisibility(View.GONE);
                    holder.fill.setVisibility(View.VISIBLE);
                    holder.fill.setOnClickListener(mOnFillListener);
                    holder.fill.setTag(item);
                }
                break;
            case T_STATUS:
                holder.header.setVisibility(View.GONE);
                holder.title.setText(item.title.toUpperCase());
                holder.state.setImageResource(R.drawable.user_cell);
                if (item.value != null && !TextUtils.isEmpty(item.value.trim())) {
                    holder.value.setText(item.value);
                    holder.value.setVisibility(View.VISIBLE);
                } else {
                    holder.value.setVisibility(View.GONE);
                }

                holder.fill.setText(R.string.edit_refresh_status);
                holder.fill.setVisibility(View.VISIBLE);
                holder.fill.setOnClickListener(mOnFillListener);
                holder.fill.setTag(item);
                break;
        }
        if (convertView != null) {
            convertView.requestLayout();
        }
        return convertView;
    }

    private int getHeaderPicture(FormItem item) {
        switch (item.titleId) {
            case R.string.form_main:
                return R.drawable.user_main;
            case R.string.form_habits:
                return R.drawable.user_habits;
            case R.string.form_physique:
                return R.drawable.user_physical;
            case R.string.form_social:
                return R.drawable.user_social;
            case R.string.form_detail:
                return R.drawable.user_details;
        }
        return 0;
    }

    public void setOnFillListener(View.OnClickListener onFillListener) {
        mOnFillListener = onFillListener;
    }

    // class ViewHolder
    private static class ViewHolder {
        public ImageView state;
        public TextView title;
        public TextView header;
        public TextView value;
        public Button fill;
    }
}


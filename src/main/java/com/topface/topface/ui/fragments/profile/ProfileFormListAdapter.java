package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;

import java.util.LinkedList;
import java.util.Locale;

public class ProfileFormListAdapter extends BaseAdapter {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.item_user_list, parent, false);
            holder.value = (TextView) convertView.findViewById(R.id.tvValue);
            holder.title = (TextView) convertView.findViewById(R.id.tvTitle);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FormItem item = getItem(position);

        holder.title.setText(item.title);
        if (TextUtils.isEmpty(item.value)) {
            holder.value.setText(R.string.form_not_specified);
        } else if (App.getContext().getResources().getString(R.string.form_main_about_status_2).equals(item.title)) {
            holder.value.setText(item.value);
        } else {
            holder.value.setText(item.value.toLowerCase(Locale.getDefault()));
        }

        holder.value.setOnClickListener(mOnFillListener);
        holder.value.setTag(item);
        holder.title.setOnClickListener(mOnFillListener);
        holder.title.setTag(item);

        return convertView;
    }

    public void setOnFillListener(View.OnClickListener onFillListener) {
        mOnFillListener = onFillListener;
    }

    // class ViewHolder
    private static class ViewHolder {
        public TextView title;
        public TextView value;
    }
}


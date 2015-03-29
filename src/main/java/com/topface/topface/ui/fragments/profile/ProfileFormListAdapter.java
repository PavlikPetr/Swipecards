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
            // fake forms for profile main data
            mProfileForms.add(new FormItem(R.string.edit_name, CacheProfile.first_name, FormItem.NAME) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.first_name = formItem.value;
                }
            });
            mProfileForms.add(new FormItem(R.string.general_sex, CacheProfile.sex, FormItem.SEX) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.sex = Integer.valueOf(formItem.value);
                }
            });
            mProfileForms.add(new FormItem(R.string.edit_age, CacheProfile.age, FormItem.AGE) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.age = Integer.valueOf(formItem.value);
                }
            });
            mProfileForms.add(new FormItem(R.string.general_city, CacheProfile.city.getName(), FormItem.CITY));
            mProfileForms.add(new FormItem(R.string.edit_status, CacheProfile.getStatus(), FormItem.STATUS) {
                @Override
                public void copy(FormItem formItem) {
                    super.copy(formItem);
                    CacheProfile.setStatus(formItem.value);
                }
            });

            // real forms
            mProfileForms.addAll(CacheProfile.forms);
        }
    }

    public LinkedList<FormItem> getFormItems() {
        return mProfileForms;
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
        String itemTitle = item.getTitle();

        holder.title.setText(itemTitle);
        if (TextUtils.isEmpty(item.value)) {
            holder.value.setText(R.string.form_not_specified);
        } else if (App.getContext().getResources().getString(R.string.form_main_about_status_2).equals(itemTitle)) {
            holder.value.setText(item.value);
        } else {
            holder.value.setText(item.value.toLowerCase(Locale.getDefault()));
        }
        holder.value.setOnClickListener(mOnFillListener);
        holder.value.setTag(item);
        holder.title.setOnClickListener(mOnFillListener);
        holder.title.setTag(item);

        convertView.setEnabled(!item.isEditing);

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


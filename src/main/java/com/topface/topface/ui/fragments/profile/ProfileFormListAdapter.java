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

    private static final int NAME_TYPE = 0;
    private static final int SEX_TYPE = 1;
    private static final int AGE_TYPE = 2;
    private static final int CITY_TYPE = 3;
    private static final int STATUS_TYPE = 4;
    private static final int FORM_TYPE = 5;
    private static final int TYPE_COUNT = 6;
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
        return mProfileForms.size() + TYPE_COUNT - 1;
    }

    @Override
    public FormItem getItem(int position) {
        FormItem formItem;
        switch (getItemViewType(position)) {
            case NAME_TYPE:
                formItem = new FormItem(R.string.edit_name, CacheProfile.first_name, FormItem.NAME);
                break;
            case SEX_TYPE:
                formItem = new FormItem(R.string.general_sex, CacheProfile.sex, FormItem.SEX);
                break;
            case AGE_TYPE:
                formItem = new FormItem(R.string.edit_age, CacheProfile.age, FormItem.AGE);
                break;
            case CITY_TYPE:
                formItem = new FormItem(R.string.general_city, CacheProfile.city.getName(), FormItem.CITY);
                break;
            case STATUS_TYPE:
                formItem = new FormItem(R.string.edit_status, CacheProfile.getStatus(), FormItem.STATUS);
                break;
            default:
                formItem = mProfileForms.get(position - TYPE_COUNT + 1);
                break;
        }
        return formItem;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position / (TYPE_COUNT - 1) > 0) {
            return FORM_TYPE;
        } else {
            return position;
        }
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
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


package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.utils.FormItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Abstract class for own and user's form adapters
 */
public abstract class AbstractFormListAdapter extends BaseAdapter {
    // Data
    private LayoutInflater mInflater;
    private LinkedList<FormItem> mForms = new LinkedList<>();

    public AbstractFormListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setUserData(LinkedList<FormItem> forms) {
        mForms = prepareForm(new LinkedList<>(forms));
    }

    protected abstract LinkedList<FormItem> prepareForm(LinkedList<FormItem> forms);

    public LinkedList<FormItem> getFormItems() {
        return mForms;
    }

    @Override
    public int getCount() {
        return mForms.size();
    }

    @Override
    public FormItem getItem(int position) {
        return mForms.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        FormItem item = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.item_user_list, parent, false);

            holder.value = (TextView) convertView.findViewById(R.id.tvValue);
            holder.title = (TextView) convertView.findViewById(R.id.tvTitle);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String itemTitle = item.getTitle();
        holder.title.setText(itemTitle);
        if (TextUtils.isEmpty(item.value)) {
            holder.value.setText(R.string.form_not_specified);
        } else if (App.getContext().getResources().getString(R.string.form_main_about_status_2).equals(itemTitle) ||
                item.type == FormItem.NAME || item.type == FormItem.STATUS) {
            holder.value.setText(item.value);
        } else if (item.type == FormItem.CITY) {
            holder.value.setText(JsonUtils.fromJson(item.value, City.class).name);
        } else {
            holder.value.setText(item.value.toLowerCase(Locale.getDefault()));
        }

        configureHolder(holder, item);

        return convertView;
    }

    protected abstract void configureHolder(ViewHolder holder, FormItem item);

    public ArrayList<FormItem> saveState() {
        return mForms != null ? new ArrayList<>(mForms) : null;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public void restoreState(ArrayList<Parcelable> userForms) {
        mForms = new LinkedList<>();
        for (Parcelable form : userForms) {
            mForms.add((FormItem) form);
        }
    }

    public boolean isFormEmpty() {
        return mForms.isEmpty();
    }

    // class ViewHolder
    protected static class ViewHolder {
        public TextView title;
        public TextView value;
    }
}

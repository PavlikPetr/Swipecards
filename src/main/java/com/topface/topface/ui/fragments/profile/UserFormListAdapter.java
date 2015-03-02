package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.utils.FormItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class UserFormListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private LinkedList<FormItem> mUserForms;

    public UserFormListAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mUserForms = new LinkedList<>();
    }

    @Override
    public int getCount() {
        return mUserForms != null ? mUserForms.size() : 0;
    }

    @Override
    public FormItem getItem(int position) {
        return mUserForms != null ? mUserForms.get(position) : null;
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
            holder.mTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.mValue = (TextView) convertView.findViewById(R.id.tvValue);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FormItem item = getItem(position);

        holder.mTitle.setText(item.title);
        holder.mValue.setText(item.value);

        return convertView;
    }

    @SuppressWarnings("unchecked")
    public void setUserData(LinkedList<FormItem> forms) {
        mUserForms = removeEmptyForms((LinkedList<FormItem>) forms.clone());
    }

    private LinkedList<FormItem> removeEmptyForms(LinkedList<FormItem> userForms) {
        Iterator<FormItem> itemsIterator = userForms.iterator();
        while (itemsIterator.hasNext()) {
            FormItem formItem = itemsIterator.next();
            if (TextUtils.isEmpty(formItem.value) || formItem.dataId == 0) {
                itemsIterator.remove();
            }
        }
        return userForms;
    }

    public ArrayList<FormItem> saveState() {
        return mUserForms != null ? new ArrayList<>(mUserForms) : null;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public void restoreState(ArrayList<Parcelable> userForms) {
        mUserForms = new LinkedList<>();
        for (Parcelable form : userForms) {
            mUserForms.add((FormItem) form);
        }
    }

    // class ViewHolder
    private static class ViewHolder {
        public TextView mTitle;
        public TextView mValue;
    }

}

package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.FormItem;

/**
 * Factory for edit dialog adapters
 */
public class EditAdapterFactory {

    public AbstractEditAdapter createAdapterFor(Context context, Object data) {
        if (data instanceof Profile.TopfaceNotifications) {
            return new NotificationEditAdapter(context, (Profile.TopfaceNotifications) data);
        } else if (data instanceof FormItem) {
            FormItem formItem = (FormItem) data;
            if (formItem.type == FormItem.SEX) {
                return new SexEditAdapter(context, formItem);
            } else if (formItem.dataId != FormItem.NO_RESOURCE_ID) {
                return new FormItemEditAdapter(context, formItem);
            } else {
                return new TextFormEditAdapter(context, formItem);
            }
        } else {
            return new AbstractEditAdapter(context) {
                @Override
                public Object getData() {
                    return null;
                }

                @Override
                public void saveData() {

                }

                @Override
                protected int getItemLayoutRes() {
                    return R.layout.about_view;
                }

                @Override
                public int getCount() {
                    return 1;
                }

                @Override
                public Object getItem(int position) {
                    return null;
                }

                @Override
                public long getItemId(int position) {
                    return 0;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = inflate(parent);
                    }
                    return convertView;
                }
            };
        }
    }
}

package com.topface.topface.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.NotificationSelectorTypes;

/**
 * Adapter for notification options
 */
public class NotificationSelectorAdapter extends AbstractSelectorAdapter<Profile.TopfaceNotifications> {

    private Profile.TopfaceNotifications mNotification;

    public NotificationSelectorAdapter(Profile.TopfaceNotifications notification) {
        mNotification = new Profile.TopfaceNotifications(notification.apns, notification.mail, notification.type);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Boolean getItem(int position) {
        if (position == 0) {
            return mNotification.apns;
        } else {
            return mNotification.mail;
        }
    }

    private void setItem(int position, boolean value) {
        if (position == 0) {
            mNotification.apns = value;
        } else {
            mNotification.mail = value;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflate(R.layout.settings_selector_checkbox, parent);

            Holder holder = new Holder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.selector_check);
            convertView.setTag(holder);
        }

        Holder holder = (Holder) convertView.getTag();
        holder.checkBox.setText(App.getContext().getString(NotificationSelectorTypes.values()[position].getName()));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setItem(position, isChecked);
            }
        });
        holder.checkBox.setChecked(getItem(position));

        return convertView;
    }

    @Override
    public Profile.TopfaceNotifications getData() {
        return new Profile.TopfaceNotifications(getItem(0), getItem(1), mNotification.type);
    }

    private static class Holder {
        CheckBox checkBox;
    }
}

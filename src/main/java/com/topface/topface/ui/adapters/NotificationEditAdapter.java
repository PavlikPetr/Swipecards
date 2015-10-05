package com.topface.topface.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.NotificationSelectorTypes;
import com.topface.topface.utils.MarketApiManager;

/**
 * Adapter for notification options
 */
public class NotificationEditAdapter extends AbstractEditAdapter<Profile.TopfaceNotifications> {

    private static final int NOTIFICATION_OPTIONS_COUNT = 2;

    private Profile.TopfaceNotifications mNotification;
    private boolean mIsPhoneNotificationEnabled = new MarketApiManager().isMarketApiAvailable();
    private int mMainColor;
    private int mDisabledColor;
    private Boolean[] mItems;

    public NotificationEditAdapter(Context context, Profile.TopfaceNotifications notification) {
        super(context);
        mNotification = new Profile.TopfaceNotifications(notification.apns, notification.mail, notification.type);
        Resources resources = App.getContext().getResources();
        mMainColor = resources.getColor(R.color.text_color_gray);
        mDisabledColor = resources.getColor(R.color.text_color_gray_transparent);
        mItems = new Boolean[]{mNotification.apns && mIsPhoneNotificationEnabled, mNotification.mail};
    }

    @Override
    public int getCount() {
        return NOTIFICATION_OPTIONS_COUNT;
    }

    @Override
    public Boolean getItem(int position) {
        return mItems[position];
    }

    private void setItem(int position, boolean value) {
        mItems[position] = value;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflate(parent);

            Holder holder = new Holder();
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.editor_check);
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
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (position == 0 && !mIsPhoneNotificationEnabled) {
            holder.checkBox.setChecked(false);
            holder.checkBox.setEnabled(false);
            holder.checkBox.setTextColor(mDisabledColor);
        } else {
            holder.checkBox.setChecked(getItem(position));
            holder.checkBox.setEnabled(true);
            holder.checkBox.setTextColor(mMainColor);
        }

        return convertView;
    }

    @Override
    public Profile.TopfaceNotifications getData() {
        return new Profile.TopfaceNotifications(mItems[0], mItems[1], mNotification.type);
    }

    /**
     * This adapter saves data automatically
     */
    @Override
    public void saveData() {

    }

    @Override
    protected int getItemLayoutRes() {
        return R.layout.edit_dialog_checkbox;
    }

    private static class Holder {
        CheckBox checkBox;
    }
}

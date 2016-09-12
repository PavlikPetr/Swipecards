package com.topface.topface.viewModels;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;

import com.topface.topface.data.leftMenu.LeftMenuData;
import com.topface.topface.utils.Utils;

/**
 * Created by ppavlik on 05.05.16.
 * Left menu item ViewModel, redraw all view across it
 */
public class LeftMenuItemViewModel {

    public ObservableInt badgeVisibility = new ObservableInt(View.GONE);
    public ObservableField<String> badgeCount = new ObservableField<>(Utils.EMPTY);
    public ObservableField<SpannableString> title = new ObservableField<>(new SpannableString(Utils.EMPTY));
    public ObservableInt dividerVisibility = new ObservableInt(View.GONE);
    public ObservableField<String> iconSrc = new ObservableField<>(null);
    public ObservableBoolean isSelected = new ObservableBoolean(false);

    private String mIcon;
    private String mBadge;
    private SpannableString mTitle;
    private boolean mIsDividerEnabled;
    private boolean mIsSelected;

    public LeftMenuItemViewModel(LeftMenuData data) {
        setBadgeCount(data.getBadge());
        setTitle(data.getTitle());
        setDividerEnable(data.isDividerEnabled());
        setIcon(data.getIcon());
        setSelected(data.isSelected());
    }

    private void setBadgeCount(String value) {
        if (mBadge == null || !mBadge.equals(value)) {
            mBadge = value;
            int count;
            try {
                count = Integer.valueOf(value);
                if (count <= 0) {
                    value = Utils.EMPTY;
                }
            } catch (NumberFormatException e) {
                // ничего не делаем, в бэйдж может быть установлена строка
            }
            badgeVisibility.set(!TextUtils.isEmpty(value) ? View.VISIBLE : View.GONE);
            badgeCount.set(String.valueOf(value));
        }
    }

    public void setTitle(SpannableString title) {
        if (mTitle == null || (title != null && !mTitle.toString().equals(title.toString()))) {
            mTitle = title;
            this.title.set(title);
        }
    }

    private void setDividerEnable(boolean isEnable) {
        if (mIsDividerEnabled != isEnable) {
            mIsDividerEnabled = isEnable;
            dividerVisibility.set(isEnable ? View.VISIBLE : View.GONE);
        }
    }

    public void setIcon(String iconSrc) {
        if (mIcon == null || !mIcon.equals(iconSrc)) {
            mIcon = iconSrc;
            this.iconSrc.set(iconSrc);
        }
    }

    public String getIcon() {
        return mIcon;
    }

    private void setSelected(boolean isSelected) {
        if (isSelected != mIsSelected) {
            mIsSelected = isSelected;
            this.isSelected.set(isSelected);
        }
    }
}

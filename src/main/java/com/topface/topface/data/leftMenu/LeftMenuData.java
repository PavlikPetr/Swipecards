package com.topface.topface.data.leftMenu;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.text.SpannableString;

import com.topface.topface.App;
import com.topface.topface.utils.Utils;

/**
 * Created by ppavlik on 06.05.16.
 * Left menu items data
 */
public class LeftMenuData {

    private String mIcon;
    private SpannableString mTitle;
    private int mBadgeCount;
    private boolean mIsDividerEnabled;
    private boolean mIsSelected;
    private LeftMenuSettingsData mSettings;

    /**
     * Create new left menu item
     *
     * @param iconRes          icon drawable resource
     * @param title            visible item name
     * @param badgeCount       unreaded feeds
     * @param isDividerEnabled set visibility of divider
     * @param settings         item settings
     */
    public LeftMenuData(@DrawableRes int iconRes, String title, int badgeCount, boolean isDividerEnabled, LeftMenuSettingsData settings) {
        this(Utils.getLocalResUrl(iconRes), new SpannableString(title), badgeCount, isDividerEnabled, settings);
    }

    /**
     * Create new left menu item
     *
     * @param iconRes          icon drawable resource
     * @param title            visible item name
     * @param badgeCount       unreaded feeds
     * @param isDividerEnabled set visibility of divider
     * @param settings         item settings
     */
    public LeftMenuData(@DrawableRes int iconRes, SpannableString title, int badgeCount, boolean isDividerEnabled, LeftMenuSettingsData settings) {
        this(Utils.getLocalResUrl(iconRes), new SpannableString(title), badgeCount, isDividerEnabled, settings);
    }

    /**
     * Create new left menu item
     *
     * @param iconRes          icon drawable resource
     * @param title            item StringRes
     * @param badgeCount       unreaded feeds
     * @param isDividerEnabled set visibility of divider
     * @param settings         item settings
     */
    public LeftMenuData(@DrawableRes int iconRes, @StringRes int title, int badgeCount, boolean isDividerEnabled, LeftMenuSettingsData settings) {
        this(Utils.getLocalResUrl(iconRes), new SpannableString(App.getContext().getResources().getString(title)), badgeCount, isDividerEnabled, settings);
    }

    /**
     * Create new left menu item
     *
     * @param icon             url or local path to icon
     * @param title            visible item name
     * @param badgeCount       unreaded feeds
     * @param isDividerEnabled set visibility of divider
     * @param settings         item settings
     */
    public LeftMenuData(String icon, SpannableString title, int badgeCount, boolean isDividerEnabled, LeftMenuSettingsData settings) {
        mIcon = icon;
        mTitle = title;
        mBadgeCount = badgeCount;
        mIsDividerEnabled = isDividerEnabled;
        mSettings = settings;
        mIsSelected = false;
    }

    /**
     * get url or local path to icon
     *
     * @return url or local path to icons
     */
    public String getIcon() {
        return mIcon;
    }

    /**
     * set url or local path to icon
     *
     * @param icon url or local path to icons
     */
    public void setIcon(String icon) {
        mIcon = icon;
    }

    /**
     * get visible item name
     *
     * @return visible item name
     */
    public SpannableString getTitle() {
        return mTitle;
    }

    /**
     * set visible item name
     *
     * @param title visible item name
     */
    public void setTitle(SpannableString title) {
        mTitle = title;
    }

    /**
     * get unreaded feeds count
     *
     * @return unreaded feeds
     */
    public int getBadgeCount() {
        return mBadgeCount;
    }

    /**
     * set unreaded feeds count
     *
     * @param count unreaded feeds
     */
    public void setBadgeCount(int count) {
        mBadgeCount = count;
    }

    /**
     * get visibility of items divider (Show on top of current item)
     *
     * @return divider visibility
     */
    public boolean isDividerEnabled() {
        return mIsDividerEnabled;
    }

    /**
     * get settings of left menu item
     *
     * @return item settings
     */
    public LeftMenuSettingsData getSettings() {
        return mSettings;
    }

    /**
     * set item selected state
     *
     * @param isSelected item state
     */
    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    /**
     * get item selected state
     *
     * @return item state
     */
    public boolean isSelected() {
        return mIsSelected;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LeftMenuData)) return false;
        LeftMenuData data = (LeftMenuData) o;
        if (mIcon != null ? !mIcon.equals(data.getIcon()) : data.getIcon() != null) return false;
        if (mTitle != null ? !mTitle.toString().equals(data.getTitle().toString()) : data.getTitle() != null)
            return false;
        if (mBadgeCount != data.getBadgeCount()) return false;
        if (mIsDividerEnabled != data.isDividerEnabled()) return false;
        return mSettings != null ? mSettings.equals(data.getSettings()) : data.getSettings() != null;
    }

    @Override
    public int hashCode() {
        int result = mIcon != null ? mIcon.hashCode() : 0;
        result = 31 * result + (mTitle != null ? mTitle.hashCode() : 0);
        result = 31 * result + mBadgeCount;
        result = 31 * result + (mIsDividerEnabled ? 1 : 0);
        return 31 * result + mSettings.hashCode();
    }
}

package com.topface.topface.utils;

import android.app.Activity;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.topface.topface.R;

public class NavigationDrawerController {

    private Activity mActivity;
    private DrawerLayout mDrawerMenu;
    private HideKeyboardToggle mNoficationToggle;
    private HideKeyboardToggle mNoNotificationToggle;
    private boolean mHasNotifications;

    private class HideKeyboardToggle extends ActionBarDrawerToggle {

        public HideKeyboardToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes,
                                  int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            super.onDrawerStateChanged(newState);
            Utils.hideSoftKeyboard(mActivity, mDrawerMenu.getWindowToken());
        }
    }

    public NavigationDrawerController(Activity activity, DrawerLayout drawerLayout) {
        mActivity = activity;
        mDrawerMenu = drawerLayout;
        initElements();
    }

    private void initElements() {
        mNoficationToggle = new HideKeyboardToggle(
                mActivity,                  /* host Activity */
                mDrawerMenu,         /* DrawerLayout object */
                R.drawable.ic_home_notification,  /* nav drawer icon to replace 'Up' caret */
                R.string.app_name,  /* "open drawer" description */
                R.string.app_name  /* "close drawer" description */
        );
        mNoNotificationToggle = new HideKeyboardToggle(
                mActivity,                  /* host Activity */
                mDrawerMenu,         /* DrawerLayout object */
                R.drawable.ic_home,  /* nav drawer icon to replace 'Up' caret */
                R.string.app_name,  /* "open drawer" description */
                R.string.app_name  /* "close drawer" description */
        );
    }

    public void refreshNotificators() {
        if (CacheProfile.unread_messages > 0 || CacheProfile.unread_mutual > 0) {
            mDrawerMenu.setDrawerListener(mNoficationToggle);
            mNoficationToggle.syncState();
            mHasNotifications = true;
        } else {
            mDrawerMenu.setDrawerListener(mNoNotificationToggle);
            mNoNotificationToggle.syncState();
            mHasNotifications = false;
        }
    }

    public void syncState() {
        if (mHasNotifications) {
            mNoficationToggle.syncState();
        } else {
            mNoNotificationToggle.syncState();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (mHasNotifications) {
            mNoficationToggle.onConfigurationChanged(newConfig);
        } else {
            mNoNotificationToggle.onConfigurationChanged(newConfig);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (mHasNotifications) {
            return mNoficationToggle.onOptionsItemSelected(item);
        } else {
            return mNoNotificationToggle.onOptionsItemSelected(item);
        }
    }

}

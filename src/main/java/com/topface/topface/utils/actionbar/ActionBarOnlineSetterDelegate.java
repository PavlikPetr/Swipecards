package com.topface.topface.utils.actionbar;

import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.widget.TextView;

import com.topface.topface.R;

/**
 * Sets action bar title with online indicator
 */
public class ActionBarOnlineSetterDelegate extends ActionBarTitleSetterDelegate {

    private boolean mOnline;
    private TextView mAbTitile;

    public ActionBarOnlineSetterDelegate(Activity activity, ActionBar actionBar) {
        super(actionBar);
        if (activity != null) {
            final int abTitleId = activity.getResources().getIdentifier("action_bar_title", "id", "android");
            mAbTitile = (TextView) activity.findViewById(abTitleId);
            mAbTitile.setCompoundDrawablePadding(10);
        }
    }

    public void setOnline(boolean online) {
        mOnline = online;
        if (mAbTitile != null) {
            mAbTitile.setCompoundDrawablesWithIntrinsicBounds(0, 0, mOnline ? R.drawable.ico_online : 0, 0);
        }
    }

    @Override
    public void setActionBarTitles(String title, String subtitle) {
        super.setActionBarTitles(title, subtitle);
        if (mAbTitile != null) {
            mAbTitile.setCompoundDrawablesWithIntrinsicBounds(0, 0, mOnline ? R.drawable.ico_online : 0, 0);
        }
    }

    @Override
    public void setActionBarTitles(int title, int subtitle) {
        super.setActionBarTitles(title, subtitle);
        if (mAbTitile != null) {
            mAbTitile.setCompoundDrawablesWithIntrinsicBounds(0, 0, mOnline ? R.drawable.ico_online : 0, 0);
        }
    }

    @Override
    public void setActionBarTitles(String title, int subtitle) {
        super.setActionBarTitles(title, subtitle);
        if (mAbTitile != null) {
            mAbTitile.setCompoundDrawablesWithIntrinsicBounds(0, 0, mOnline ? R.drawable.ico_online : 0, 0);
        }
    }

    @Override
    public void setActionBarTitles(int title, String subtitle) {
        super.setActionBarTitles(title, subtitle);
        if (mAbTitile != null) {
            mAbTitile.setCompoundDrawablesWithIntrinsicBounds(0, 0, mOnline ? R.drawable.ico_online : 0, 0);
        }
    }


}

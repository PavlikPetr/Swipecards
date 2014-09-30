package com.topface.topface.utils.actionbar;

import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.R;

/**
 * Sets action bar title with online indicator
 */
public class ActionBarOnlineSetterDelegate extends ActionBarTitleSetterDelegate {

    private boolean mOnline;
    private TextView mAbTitile;

    public ActionBarOnlineSetterDelegate(ActionBar actionBar) {
        super(actionBar);
        if (actionBar != null) {
            View customView = actionBar.getCustomView();
            if (customView != null) {
                final int abTitleId = customView.getResources().getIdentifier("action_bar_title", "id", "android");
                mAbTitile = (TextView) customView.findViewById(abTitleId);
                if (mAbTitile != null) {
                    mAbTitile.setCompoundDrawablePadding(10);
                }
            }
        }
    }

    public ActionBarOnlineSetterDelegate(ActionBar actionBar, int titleId) {
        super(actionBar);
        if (actionBar != null) {
            View customView = actionBar.getCustomView();
            if (customView != null) {
                mAbTitile = (TextView) customView.findViewById(titleId);
                if (mAbTitile != null) {
                    mAbTitile.setCompoundDrawablePadding(10);
                }
            }
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
    }

    @Override
    public void setActionBarTitles(int title, int subtitle) {
        super.setActionBarTitles(title, subtitle);
    }

    @Override
    public void setActionBarTitles(String title, int subtitle) {
        super.setActionBarTitles(title, subtitle);
    }

    @Override
    public void setActionBarTitles(int title, String subtitle) {
        super.setActionBarTitles(title, subtitle);
    }


}

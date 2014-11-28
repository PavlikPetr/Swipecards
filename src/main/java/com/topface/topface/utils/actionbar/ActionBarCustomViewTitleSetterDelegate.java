package com.topface.topface.utils.actionbar;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.ui.NavigationActivity;

/**
 * Title setter delegate for actionbar with custom view.
 */
public class ActionBarCustomViewTitleSetterDelegate extends ActionBarOnlineSetterDelegate {

    private View mClickable;
    private TextView mTitle;
    private TextView mSubTitle;

    public ActionBarCustomViewTitleSetterDelegate(final Activity activity, ActionBar actionBar,
                                                  int clickableTitleId, int titleId, int subtitleId) {
        super(actionBar, activity, titleId);
        if (actionBar != null) {
            View customView = actionBar.getCustomView();
            if (customView != null) {
                mClickable = customView.findViewById(clickableTitleId);
                mTitle = (TextView) customView.findViewById(titleId);
                mSubTitle = (TextView) customView.findViewById(subtitleId);
            }
        }
        if (mClickable != null) {
            mClickable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (activity instanceof NavigationActivity) {
                        Intent intent = new Intent(NavigationActivity.OPEN_MENU);
                        LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
                    } else if (activity.isTaskRoot()) {
                        Intent intent = new Intent(activity, NavigationActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    } else {
                        activity.onBackPressed();
                    }
                }
            });
        }
    }

    @Override
    public void setActionBarTitles(String title, String subtitle) {
        if (mTitle != null) {
            mTitle.setText(title);
        }
        if (mSubTitle != null) {
            if (subtitle != null && !subtitle.isEmpty()) {
                mSubTitle.setVisibility(View.VISIBLE);
                mSubTitle.setText(subtitle);
            } else {
                mSubTitle.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setActionBarTitles(int title, int subtitle) {
        if (mTitle != null) {
            mTitle.setText(title);
        }
        if (mSubTitle != null) {
            if (subtitle > 0) {
                mSubTitle.setVisibility(View.VISIBLE);
                mSubTitle.setText(subtitle);
            } else {
                mSubTitle.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setActionBarTitles(String title, int subtitle) {
        if (mTitle != null) {
            mTitle.setText(title);
        }
        if (mSubTitle != null) {
            if (subtitle > 0) {
                mSubTitle.setVisibility(View.VISIBLE);
                mSubTitle.setText(subtitle);
            } else {
                mSubTitle.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setActionBarTitles(int title, String subtitle) {
        if (mTitle != null) {
            mTitle.setText(title);
        }
        if (mSubTitle != null) {
            if (subtitle != null && !subtitle.isEmpty()) {
                mSubTitle.setVisibility(View.VISIBLE);
                mSubTitle.setText(subtitle);
            } else {
                mSubTitle.setVisibility(View.GONE);
            }
        }
    }
}

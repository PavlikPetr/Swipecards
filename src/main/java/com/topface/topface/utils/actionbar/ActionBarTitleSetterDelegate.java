package com.topface.topface.utils.actionbar;

import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;

/**
 * Created by kirussell on 26.09.13.
 * Class
 */
public class ActionBarTitleSetterDelegate {

    private boolean mNoActionBar;
    private TextView mTitle;
    private TextView mSubtitle;

    public ActionBarTitleSetterDelegate(ActionBar actionBar) {
        mNoActionBar = (actionBar == null);
        if (!mNoActionBar) {
            mTitle = (TextView) actionBar.getCustomView().findViewById(R.id.title);
            mSubtitle = (TextView) actionBar.getCustomView().findViewById(R.id.subtitle);
        }
    }

    public void setActionBarTitles(String title, String subtitle) {
        if (mNoActionBar) return;
        mSubtitle.setVisibility(View.VISIBLE);
        mTitle.setText(title);
        if (TextUtils.isEmpty(subtitle)) {
            mSubtitle.setVisibility(View.GONE);
            return;
        }
        mSubtitle.setText(subtitle);
    }

    public void setActionBarTitles(int title, int subtitle) {
        if (mNoActionBar) return;
        setActionBarTitles(App.getContext().getResources().getString(title),
                App.getContext().getResources().getString(subtitle));
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setActionBarTitles(String title, int subtitle) {
        if (mNoActionBar) return;
        setActionBarTitles(title, App.getContext().getResources().getString(subtitle));

    }

    public void setActionBarTitles(int title, String subtitle) {
        if (mNoActionBar) return;
        setActionBarTitles(App.getContext().getResources().getString(title), subtitle);
    }

    public void setOnline(boolean online) {
        if (mTitle != null) {
            mTitle.setCompoundDrawablePadding((int) App.getContext().getResources().
                    getDimension(R.dimen.padding_left_for_online_icon));
            mTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, online ? R.drawable.ico_online : 0, 0);
        }
    }

}

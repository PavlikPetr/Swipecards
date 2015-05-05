package com.topface.topface.utils.actionbar;

import android.app.Activity;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.BaseFragmentActivity;

/**
 * Created by onikitin on 26.01.15.
 * Класс для создания View для экшен бара
 */
public class ActionBarView implements View.OnClickListener {

    private ActionBar mActionBar;
    private TextView mTitle;
    private TextView mSubtitle;
    private ImageView mIcon;
    private View mActionBarView;
    private Activity mActivity;

    public ActionBarView(ActionBar actionBar, Activity activity) {
        mActionBar = actionBar;
        mActivity = activity;
    }

    private void prepareView() {
        mActionBar.setCustomView(R.layout.actionbar_container_title_view);
        mActionBarView = mActionBar.getCustomView();
        mTitle = (TextView) mActionBarView.findViewById(R.id.title);
        mSubtitle = (TextView) mActionBarView.findViewById(R.id.subtitle);
        mIcon = (ImageView) mActionBarView.findViewById(R.id.up_icon);
        mActionBarView.findViewById(R.id.title_clickable).setOnClickListener(this);
    }

    public void setLeftMenuView() {
        prepareView();
        mIcon.setImageResource(R.drawable.ic_home);
        mSubtitle.setVisibility(View.VISIBLE);
    }

    public void setPurchasesView(String title) {
        setArrowUpView(title);
        LinearLayout resoursesLayout = (LinearLayout) mActionBarView.findViewById(R.id.resources_layout);
        resoursesLayout.setVisibility(View.VISIBLE);
    }

    public void setArrowUpView(String title) {
        prepareView();
        mIcon.setImageResource(R.drawable.ic_up_arrow);
        mTitle.setText(title);
    }

    public void setArrowUpView() {
        setArrowUpView(null);
    }

    public void setSimpleView() {
        prepareView();
        mActionBarView.findViewById(R.id.title_clickable).setClickable(false);
        mIcon.setVisibility(View.GONE);
        mTitle.setText(R.string.app_name);
    }

    @Override
    public void onClick(View v) {
        if (mActionBar != null) {
            View customView = mActionBar.getCustomView();
            if (customView != null) {
                mActionBarView = customView.findViewById(R.id.title_clickable);
            }
        }
        if (mActionBarView != null) {
            if (mActivity instanceof BaseFragmentActivity) {
                ((BaseFragmentActivity) mActivity).onUpClick();
            }
        }
    }

    public void setActionBarTitle(String title) {
        mTitle.setText(title);
    }

    public void setActionBarTitle(@StringRes int titleId) {
        setActionBarTitle(App.getContext().getResources().getString(titleId));
    }
}

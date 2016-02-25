package com.topface.topface.utils.actionbar;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.BaseFragmentActivity;

import java.lang.ref.WeakReference;

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
    private WeakReference<Activity> mWeakReference;

    public ActionBarView(ActionBar actionBar, Activity activity) {
        mActionBar = actionBar;
        mWeakReference = new WeakReference<>(activity);
    }

    private void prepareView() {
        mActionBar.setCustomView(R.layout.actionbar_container_title_view);
        mActionBarView = mActionBar.getCustomView();
        mTitle = (TextView) mActionBarView.findViewById(R.id.title);
        mSubtitle = (TextView) mActionBarView.findViewById(R.id.subtitle);
        mIcon = (ImageView) mActionBarView.findViewById(R.id.up_icon);
        mActionBarView.findViewById(R.id.title_clickable).setOnClickListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedValue tempVal = new TypedValue();
            mWeakReference.get().getResources().getValue(R.dimen.actionbar_elevation, tempVal, true);
            mActionBar.setElevation(tempVal.getFloat());
        }
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
        mIcon.setImageResource(R.drawable.ic_arrow_up);
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
            Activity activity = mWeakReference.get();
            if (activity instanceof BaseFragmentActivity) {
                ((BaseFragmentActivity) activity).onUpClick();
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

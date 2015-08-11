package com.topface.topface.ui.views;

import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.widget.SwipeRefreshLayout;

import com.topface.topface.App;
import com.topface.topface.R;

/**
 * Created by ppetr on 11.08.15.
 * Controller SwipeRefreshLayout with our customzation
 */
public class SwipeRefreshController {
    private SwipeRefreshLayout mSwipeRefresh;

    public SwipeRefreshController(SwipeRefreshLayout swipeRefreshLayout) {
        setSwipeRefreshLayout(swipeRefreshLayout);
        setDefaultShemeColor();
    }

    private void setSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        mSwipeRefresh = swipeRefreshLayout;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefresh;
    }

    private void setDefaultShemeColor() {
        if (mSwipeRefresh != null) {
            mSwipeRefresh.setColorSchemeResources(getColorPrimary(), getColorAccent());
        }
    }

    private int getColorByThemeAttribute(int attr) {
        TypedArray a = App.getContext().getTheme().obtainStyledAttributes(R.style.Theme_Topface, new int[]{attr});
        int color = a.getResourceId(0, 0);
        a.recycle();
        return color;
    }

    private int getColorPrimary() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getColorByThemeAttribute(R.attr.colorPrimary);
        }
        return R.color.light_theme_color_primary;
    }

    private int getColorAccent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getColorByThemeAttribute(R.attr.colorAccent);
        }
        return R.color.light_theme_color_accent;
    }

    public void releaseController() {
        mSwipeRefresh = null;
    }
}

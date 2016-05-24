package com.topface.topface.ui.views;

import android.graphics.Color;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.state.DrawerLayoutState;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by ppavlik on 19.05.16.
 */
public class DrawerLayoutManager<DrawerLayoutType extends DrawerLayout> {

    @Inject
    DrawerLayoutState mDrawerLayoutState;

    private DrawerLayoutType mDrawerLayout;

    public DrawerLayoutManager(@NotNull DrawerLayoutType drawerLayout) {
        App.get().inject(this);
        mDrawerLayout = drawerLayout;
    }

    public void initLeftMneuDrawerLayout() {
        mDrawerLayout.setScrimColor(Color.argb(217, 0, 0, 0));
        mDrawerLayout.setDrawerShadow(R.drawable.shadow_left_menu_right, GravityCompat.START);
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mDrawerLayoutState.onSlide();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mDrawerLayoutState.onOpen();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mDrawerLayoutState.onClose();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                mDrawerLayoutState.onStateChanged();
            }
        });
    }

    public void close(){
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public DrawerLayoutType getDrawer() {
        return mDrawerLayout;
    }
}

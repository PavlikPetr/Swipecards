package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.topface.topface.data.IUniversalUser;
import com.topface.topface.utils.actionbar.OverflowMenu;

/**
 * Created by ppetr on 19.05.15.
 * animated fragment on start
 */
public abstract class AnimatedFragment extends UserAvatarFragment {
    private static final int MAIN_SCREEN_ANIMATION_DURATION = 300;
    private boolean isAnimationNeedToShow;

    private View mView;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (null == mView) {
            mView = view;
        }
        // показ "белого экрана" и анимируем основную view только при первом запуске фрагмента
        isAnimationNeedToShow = savedInstanceState == null;
        if (isAnimationNeedToShow && isAnimationRequire()) {
            startWhiteScreenTimer();
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected IUniversalUser createUniversalUser() {
        return null;
    }

    @Override
    protected OverflowMenu createOverflowMenu(Menu barActions) {
        return null;
    }

    @Override
    protected void initOverflowMenuActions(OverflowMenu overflowMenu) {

    }

    @Override
    protected String getDefaultTitle() {
        return null;
    }


    public void requestExecuted() {
        stopWhiteScreenTimer();
    }

    private void startWhiteScreenTimer() {
        if (null != mView && isAnimationNeedToShow && isAnimationRequire()) {
            mView.setAlpha(0);
            mView.animate().setDuration(MAIN_SCREEN_ANIMATION_DURATION);
        }
    }

    private void stopWhiteScreenTimer() {
        showMainViewWithAnimation();
    }

    private void showMainViewWithAnimation() {
        if (null != mView && isAnimationNeedToShow && isAnimationRequire()) {
            mView.animate().alpha(1);
        }
    }

    public void setAnimatedView(View view) {
        mView = view;
    }

    protected abstract boolean isAnimationRequire();
}

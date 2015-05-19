package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.topface.topface.data.IUniversalUser;
import com.topface.topface.utils.actionbar.OverflowMenu;

/**
 * Created by ppetr on 19.05.15.
 * animated fragment on start
 */
public class AnimatedFragment extends UserAvatarFragment {
    private static final int WHITE_SCREEN_DELAY = 1000;
    private static final int MAIN_SCREEN_ANIMATION_DURATION = 300;

    private View mView;
    private AlphaAnimation mMainScreenAlphaAnimation;

    CountDownTimer mWhiteScreenTimer = new CountDownTimer(WHITE_SCREEN_DELAY, WHITE_SCREEN_DELAY) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            showMainViewWithAnimation();
        }
    };

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mView = null;
        // показ "белого экрана" и анимируем основную view только при первом запуске фрагмента
        if (null == savedInstanceState) {
            mView = view;
            startWhiteScreenTimer();
        }
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    protected IUniversalUser createUniversalUser() {
        return null;
    }

    @Override
    protected OverflowMenu createOverflowMenu(MenuItem barActions) {
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
        if (null != mView) {
            mView.setAlpha(0);
            mWhiteScreenTimer.cancel();
            mWhiteScreenTimer.start();
            mView.animate().setDuration(MAIN_SCREEN_ANIMATION_DURATION);
        }
    }

    private void stopWhiteScreenTimer() {
        mWhiteScreenTimer.cancel();
        showMainViewWithAnimation();
    }

    private void showMainViewWithAnimation() {
        if (null != mView) {
            mView.animate().alpha(1);
        }
    }
}

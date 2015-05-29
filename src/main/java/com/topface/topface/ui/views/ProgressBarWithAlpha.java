package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.topface.topface.R;

/**
 * Created by ppetr on 19.05.15.
 * progress bar contains oneshot alpha animation
 */
public class ProgressBarWithAlpha extends ProgressBar {

    private final static int ALPHA_ANIMATION_DURATION = 3000;

    public ProgressBarWithAlpha(Context context) {
        super(context);
        initView();
    }

    public ProgressBarWithAlpha(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        this.setAlpha(0.0f);
        this.animate().setDuration(ALPHA_ANIMATION_DURATION);
    }

    @Override
    public void setVisibility(int v) {
        if (v == View.VISIBLE) {
            this.animate().alpha(1.0f);
        }
        super.setVisibility(v);
    }

    @Override
    public Drawable getIndeterminateDrawable() {
        return getResources().getDrawable(R.drawable.progress_medium_holo);
    }
}

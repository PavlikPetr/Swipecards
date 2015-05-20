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
        this.animate().setDuration(5000);
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (visibility == View.VISIBLE) {
            this.animate().alpha(1.0f);
        }
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    public Drawable getIndeterminateDrawable() {
        return getResources().getDrawable(R.drawable.progress_medium_holo);
    }
}

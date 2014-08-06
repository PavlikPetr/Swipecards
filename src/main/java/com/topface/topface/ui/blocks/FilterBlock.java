package com.topface.topface.ui.blocks;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.topface.topface.utils.SwapAnimation;

/**
 * Блок инициализации панели инструментов и кнопки для ее открытия и закрытия
 */
@SuppressWarnings("deprecation")
public class FilterBlock {
    private final View mToolsBar;
    private final View mControlGroup;

    public FilterBlock(ViewGroup rootView, int controlGroupId, int toolsBar) {
        mControlGroup = rootView.findViewById(controlGroupId);
        mToolsBar = rootView.findViewById(toolsBar);
        if (mControlGroup != null && mToolsBar != null) {
            initFilter();
        }
    }

    protected void initFilter() {
        mControlGroup.setVisibility(View.INVISIBLE);
        mControlGroup.setVisibility(View.VISIBLE);
        mToolsBar.setVisibility(View.INVISIBLE);

        ViewTreeObserver vto = mToolsBar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int y = mToolsBar.getMeasuredHeight();
                if (y != 0) {
                    mControlGroup.setPadding(mControlGroup.getPaddingLeft(), -y, mControlGroup.getPaddingRight(), mControlGroup.getPaddingBottom());
                    ViewTreeObserver obs = mControlGroup.getViewTreeObserver();
                    removeGlobalLayoutListener(obs);
                }
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            private void removeGlobalLayoutListener(ViewTreeObserver obs) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    public void openControls() {
        mControlGroup.startAnimation(new SwapAnimation(mControlGroup, mToolsBar));
    }
}

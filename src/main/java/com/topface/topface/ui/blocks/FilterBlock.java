package com.topface.topface.ui.blocks;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.topface.topface.Static;
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
        mControlGroup.setVisibility(View.VISIBLE);
        mToolsBar.setVisibility(View.VISIBLE);

        ViewTreeObserver vto = mToolsBar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int y = mToolsBar.getMeasuredHeight();
                if (y != 0) {
                    y += Static.HEADER_SHADOW_SHIFT;
                    mControlGroup.setPadding(mControlGroup.getPaddingLeft(), -y, mControlGroup.getPaddingRight(), mControlGroup.getPaddingBottom());
                    ViewTreeObserver obs = mControlGroup.getViewTreeObserver();
                    if (Build.VERSION.SDK_INT >= 16) {
                        obs.removeOnGlobalLayoutListener(this);
                    } else {
                        obs.removeGlobalOnLayoutListener(this);
                    }
                }
            }
        });
    }

    public void openControls() {
        mControlGroup.startAnimation(new SwapAnimation(mControlGroup, mToolsBar));
    }
}

package com.topface.topface.ui.blocks;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.topface.topface.Static;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.SwapAnimation;

/**
 * Блок инициализации панели инструментов и кнопки для ее открытия и закрытия
 */
@SuppressWarnings("deprecation")
public class FilterBlock {
    private final View mToolsBar;
    private final View mControlGroup;
    private final ActionBar mActionBar;

    public FilterBlock(ViewGroup rootView, int controlGroupId, ActionBar actionBar, int toolsBar) {
        mActionBar = actionBar;
        mControlGroup = rootView.findViewById(controlGroupId);
        mToolsBar = rootView.findViewById(toolsBar);
        if (mControlGroup != null && actionBar != null && mToolsBar != null) {
            initFilter();
        }
    }

    protected void initFilter() {
        mControlGroup.setVisibility(View.VISIBLE);
        mToolsBar.setVisibility(View.VISIBLE);

        mActionBar.showSettingsButton(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mControlGroup.startAnimation(new SwapAnimation(mControlGroup, mToolsBar));
            }
        });

        ViewTreeObserver vto = mToolsBar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int y = mToolsBar.getMeasuredHeight();
                if (y != 0) {
                    y += Static.HEADER_SHADOW_SHIFT;
                    mControlGroup.setPadding(mControlGroup.getPaddingLeft(), -y, mControlGroup.getPaddingRight(), mControlGroup.getPaddingBottom());
                    ViewTreeObserver obs = mControlGroup.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });
    }
}

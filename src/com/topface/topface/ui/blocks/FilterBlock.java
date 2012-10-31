package com.topface.topface.ui.blocks;

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
    private final View mShowToolsBarButton;
    private final View mToolsBar;
    private final View mControlGroup;

    public FilterBlock(ViewGroup rootView, int controlGroupId, int showToolsBarButton, int toolsBar) {
        mControlGroup = rootView.findViewById(controlGroupId);
        mShowToolsBarButton = rootView.findViewById(showToolsBarButton);
        mToolsBar = rootView.findViewById(toolsBar);
        initFilter();
    }

    protected void initFilter() {
        mControlGroup.setVisibility(View.VISIBLE);
        mShowToolsBarButton.setVisibility(View.VISIBLE);
        mToolsBar.setVisibility(View.VISIBLE);

        mShowToolsBarButton.setOnClickListener(new View.OnClickListener() {
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

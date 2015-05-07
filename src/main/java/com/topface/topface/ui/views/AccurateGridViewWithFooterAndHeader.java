package com.topface.topface.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.topface.topface.ui.GridViewWithHeaderAndFooter;

/**
 * GridView with squred items
 */
public class AccurateGridViewWithFooterAndHeader extends GridViewWithHeaderAndFooter {

    public AccurateGridViewWithFooterAndHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccurateGridViewWithFooterAndHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AccurateGridViewWithFooterAndHeader(Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        int size = getGridViewColumnWidth();
        ViewGroup.LayoutParams lp = super.generateDefaultLayoutParams();
        lp.width = size;
        lp.height = size;
        return lp;
    }

    public Class getBaseGridViewClass() {
        return getClass().getSuperclass().getSuperclass();
    }
}

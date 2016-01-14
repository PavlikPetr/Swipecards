package com.topface.topface.ui.adapters;

import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by ppetr on 14.01.16.
 */
public class BindingAdapterMethods {
    @BindingAdapter("android:layout_marginTop")
    public static void setMarginTop(View view, int padding) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        lp.setMargins(lp.leftMargin, padding, lp.rightMargin, lp.bottomMargin);
    }
}

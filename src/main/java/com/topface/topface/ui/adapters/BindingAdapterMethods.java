package com.topface.topface.ui.adapters;

import android.databinding.BindingAdapter;
import android.databinding.ObservableFloat;
import android.view.View;
import android.widget.LinearLayout;

import com.topface.framework.utils.Debug;

/**
 * Created by ppetr on 14.01.16.
 * Collection of bindings adapter methods
 */
public class BindingAdapterMethods {
    @BindingAdapter("android:layout_marginTop")
    public static void setMarginTop(View view, float padding) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        lp.setMargins(lp.leftMargin, (int) padding, lp.rightMargin, lp.bottomMargin);
    }
}

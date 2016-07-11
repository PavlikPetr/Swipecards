package com.topface.topface.utils;

import android.databinding.BindingConversion;
import android.view.View;

/**
 * Created by ppavlik on 11.07.16.
 * Conversion methods for databinding
 */

public class BindingConversions {

    @BindingConversion
    public static int visibility(boolean visible) {
        return visible ? View.VISIBLE : View.GONE;
    }
}

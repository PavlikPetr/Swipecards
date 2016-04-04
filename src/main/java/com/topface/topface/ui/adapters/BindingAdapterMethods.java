package com.topface.topface.ui.adapters;

import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.databinding.BindingAdapter;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.ui.views.ImageViewRemote;

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

    @BindingAdapter("android:background")
    public static void setBackgroundResource(View view, @DrawableRes int bgResource) {
        view.setBackgroundResource(bgResource);
    }

    @BindingAdapter("android:textColor")
    public static void setTextColorSelector(View view, int colorSelector) {
        try {
            XmlResourceParser xrp = App.getContext().getResources().getXml(colorSelector);
            ColorStateList csl = ColorStateList.createFromXml(App.getContext().getResources(), xrp);
            ((TextView) view).setTextColor(csl);
        } catch (Exception e) {
            Debug.error(e.toString());
        }
    }

    @BindingAdapter("app:remoteSrc")
    public static void setremoteSrc(ImageViewRemote view, String res) {
        view.setRemoteSrc(res);
    }
}

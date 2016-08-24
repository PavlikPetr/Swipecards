package com.topface.topface.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.widget.TextView;

import com.topface.topface.ui.views.ImageViewRemote;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BindingsUtils {

    public static final int EMPTY_RESOURCE = 0;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LEFT, TOP, RIGHT, BOTTOM})
    public @interface DrawableSide {
    }

    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;


    public static TextView replaceDrawable(TextView view, @DrawableRes int bgResource, @DrawableSide int replacedPos) {
        Drawable[] editedDrawables = new Drawable[4];
        Drawable[] drawables = view.getCompoundDrawables();
        int drawablesLength = drawables.length;
        for (int i = 0; i < editedDrawables.length; i++) {
            editedDrawables[i] = i < drawablesLength ? drawables[i] : null;
        }
        editedDrawables[replacedPos] = bgResource != 0 ? view.getResources().getDrawable(bgResource) : null;
        view.setCompoundDrawablesWithIntrinsicBounds(editedDrawables[LEFT], editedDrawables[TOP], editedDrawables[RIGHT], editedDrawables[BOTTOM]);
        return view;
    }

    public static void loadLink(ImageViewRemote view, String res, @DrawableRes int defaultRes) {
        if (!TextUtils.isEmpty(res)) {
            view.setRemoteSrc(res);
        } else {
            if (defaultRes == EMPTY_RESOURCE) {
                view.setImageDrawable(null);
            } else {
                view.setImageResource(defaultRes);
            }
        }
    }
}

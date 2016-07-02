package com.topface.topface.utils;

import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.widget.TextView;

import com.topface.topface.App;

public class BindingsUtils {

    public static TextView replaceDrawable(TextView view, @DrawableRes int bgResource, int replacedPos) {
        Drawable[] editedDrawables = new Drawable[4];
        if (replacedPos >= editedDrawables.length) {
            throw new IllegalArgumentException("Wrong replaced position");
        }
        Drawable[] drawables = view.getCompoundDrawables();
        int drawablesLength = drawables.length;
        for (int i = 0; i < editedDrawables.length; i++) {
            editedDrawables[i] = i < drawablesLength ? drawables[i] : null;
        }
        if (replacedPos < editedDrawables.length) {
            editedDrawables[replacedPos] = view.getResources().getDrawable(bgResource);
        }
        view.setCompoundDrawablesWithIntrinsicBounds(editedDrawables[0], editedDrawables[1], editedDrawables[2], editedDrawables[3]);
        return view;
    }
}

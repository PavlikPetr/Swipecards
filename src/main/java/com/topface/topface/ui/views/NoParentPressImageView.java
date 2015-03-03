package com.topface.topface.ui.views;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by onikitin on 17.02.15.
 * Кастомный ImageView для android 2.3. Нужна для исключения подсвечивания childView при тапе по
 * элементу списка
 */
public class NoParentPressImageView extends ImageView {

    public NoParentPressImageView(Context context) {
        this(context, null);
    }

    public NoParentPressImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setPressed(boolean pressed) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
            if (pressed && ((View) getParent()).isPressed()) {
                return;
            }
        }
        super.setPressed(pressed);
    }
}

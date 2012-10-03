package com.topface.topface.ui.edit;

import android.graphics.drawable.Drawable;
import com.topface.topface.R;

public abstract class EditProfileItem {
    public enum Type {TOP, MIDDLE, BOTTOM, NONE}

    ;

    private Type mType = Type.MIDDLE;

    public String getTitle() {
        return null;
    }

    public String getText() {
        return null;
    }

    public Drawable getIcon() {
        return null;
    }

    public Type getType() {
        return mType;
    }

    public int getLayoutResId() {
        return R.layout.item_edit_form_select;
    }

    public EditProfileItem setType(Type type) {
        mType = type;
        return this;
    }

    abstract void onClick();
}

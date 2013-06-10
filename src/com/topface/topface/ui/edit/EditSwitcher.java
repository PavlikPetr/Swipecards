package com.topface.topface.ui.edit;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class EditSwitcher {
    private CheckBox mCheckbox;
    private TextView mTextOn;
    private TextView mTextOff;
    private ViewGroup mRoot;

    public EditSwitcher(ViewGroup root) {
        mRoot = root;
        mCheckbox = (CheckBox) root.findViewWithTag("cbSwitch");
        mTextOn = (TextView) root.findViewWithTag("tvSwitchOn");
        mTextOff = (TextView) root.findViewWithTag("tvSwitchOff");
    }

    public void setChecked(boolean checked) {
        mCheckbox.setChecked(checked);
        if (checked) {
            mTextOn.setVisibility(View.VISIBLE);
            mTextOff.setVisibility(View.INVISIBLE);
        } else {
            mTextOn.setVisibility(View.INVISIBLE);
            mTextOff.setVisibility(View.VISIBLE);
        }
    }

    public void setEnabled(boolean enabled) {
        mRoot.setEnabled(enabled);
        mCheckbox.setEnabled(enabled);
        mTextOn.setEnabled(enabled);
        mTextOff.setEnabled(enabled);
    }

    public void doSwitch() {
        setChecked(!mCheckbox.isChecked());
    }

    public boolean isChecked() {
        return mCheckbox.isChecked();
    }

    public void setVisibility(int visibility) {
        mCheckbox.setVisibility(visibility);
        if (visibility == View.GONE) {
            mTextOff.setVisibility(visibility);
            mTextOn.setVisibility(visibility);
        } else {
            setChecked(isChecked());
        }
    }
}

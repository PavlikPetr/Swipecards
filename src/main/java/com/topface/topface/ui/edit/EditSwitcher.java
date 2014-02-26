package com.topface.topface.ui.edit;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

public class EditSwitcher {
    private TextView mTitle;
    private CheckBox mCheckbox;
    private TextView mTextOn;
    private TextView mTextOff;
    private ViewGroup mRoot;
    private ProgressBar mPrgrsBar;

    public EditSwitcher(ViewGroup root, String title) {
        mRoot = root;
        mTitle = (TextView) root.findViewWithTag("tvTitle");
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
        mCheckbox = (CheckBox) root.findViewWithTag("cbSwitch");
        mTextOn = (TextView) root.findViewWithTag("tvSwitchOn");
        mTextOff = (TextView) root.findViewWithTag("tvSwitchOff");
        mPrgrsBar = (ProgressBar) root.findViewWithTag("vsiLoadBar");
    }

    public EditSwitcher(ViewGroup root) {
        this(root, null);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public boolean setChecked(boolean checked) {
        mCheckbox.setChecked(checked);
        if (checked) {
            mTextOn.setVisibility(View.VISIBLE);
            mTextOff.setVisibility(View.INVISIBLE);
        } else {
            mTextOn.setVisibility(View.INVISIBLE);
            mTextOff.setVisibility(View.VISIBLE);
        }
        return checked;
    }

    public void setEnabled(boolean enabled) {
        mRoot.setEnabled(enabled);
        mCheckbox.setEnabled(enabled);
        mTextOn.setEnabled(enabled);
        mTextOff.setEnabled(enabled);
    }

    public boolean doSwitch() {
        return setChecked(!mCheckbox.isChecked());
    }

    public boolean isChecked() {
        return mCheckbox.isChecked();
    }

    public void setVisibility(boolean visible) {
        setVisibility(visible, isChecked());
    }

    public void setVisibility(boolean visible, boolean checked) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        mCheckbox.setVisibility(visibility);
        if (!visible) {
            mTextOn.setVisibility(visibility);
            mTextOff.setVisibility(visibility);
        } else {
            setChecked(checked);
        }
    }

    public void setProgressState(boolean waiting, boolean checked) {
        mPrgrsBar.setVisibility(waiting ? View.VISIBLE : View.GONE);
        setVisibility(!waiting, checked);
    }

    public void setProgressState(boolean waiting) {
        mPrgrsBar.setVisibility(waiting ? View.VISIBLE : View.GONE);
        setVisibility(!waiting);
    }
}

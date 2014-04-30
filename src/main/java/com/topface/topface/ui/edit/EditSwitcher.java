package com.topface.topface.ui.edit;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.topface.ui.views.Switcher;

public class EditSwitcher {
    private TextView mTitle;
    private Switcher mSwitcher;
    private ViewGroup mRoot;
    private ProgressBar mPrgrsBar;

    public EditSwitcher(ViewGroup root, String title) {
        mRoot = root;
        mTitle = (TextView) root.findViewWithTag("tvTitle");
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
        }
        mSwitcher = (Switcher) root.findViewWithTag("cbSwitch");
        mPrgrsBar = (ProgressBar) root.findViewWithTag("vsiLoadBar");
    }

    public EditSwitcher(ViewGroup root) {
        this(root, null);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public boolean setChecked(boolean checked) {
        mSwitcher.setChecked(checked);
        return checked;
    }

    public void setEnabled(boolean enabled) {
        mRoot.setEnabled(enabled);
        mSwitcher.setEnabled(enabled);
    }

    public boolean doSwitch() {
        return setChecked(!mSwitcher.isChecked());
    }

    public boolean isChecked() {
        return mSwitcher.isChecked();
    }

    public void setVisibility(boolean visible) {
        setVisibility(visible, isChecked());
    }

    public void setVisibility(boolean visible, boolean checked) {
        int visibility = visible ? View.VISIBLE : View.INVISIBLE;
        mSwitcher.setVisibility(visibility);
        if (visible) {
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

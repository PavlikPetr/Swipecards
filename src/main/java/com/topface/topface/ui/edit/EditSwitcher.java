package com.topface.topface.ui.edit;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.topface.R;

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

    public static class Switcher extends Button {

        private String mTextOn;
        private String mTextOff;

        private int mBackgroundOn;
        private int mBackgrounOff;

        private boolean mChecked;

        public Switcher(Context context, AttributeSet attrs) {
            super(context, attrs);
            mTextOn = context.getString(R.string.settings_switch_on);
            mTextOff = context.getString(R.string.settings_switch_off);
            mBackgroundOn = R.drawable.edit_switch_on;
            mBackgrounOff = R.drawable.edit_switch_off;
            syncState();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            CharSequence defaultText = getText();
            Paint meter = new Paint();
            if (meter.measureText(mTextOn, 0, mTextOn.length()) > meter.measureText(mTextOff, 0, mTextOff.length())) {
                setText(mTextOn);
            } else {
                setText(mTextOff);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setText(defaultText);
        }

        public void setChecked(boolean checked) {
            mChecked = checked;
            syncState();
        }

        private void syncState() {
            if (mChecked) {
                setBackgroundResource(mBackgroundOn);
                setTextColor(Color.parseColor("#FFFFFF"));
                setText(mTextOn);
            } else {
                setBackgroundResource(mBackgrounOff);
                setTextColor(Color.parseColor("#2F2F2F"));
                setText(mTextOff);
            }
        }

        public boolean isChecked() {
            return mChecked;
        }
    }
}

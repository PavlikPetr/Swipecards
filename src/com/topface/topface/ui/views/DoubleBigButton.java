package com.topface.topface.ui.views;

import com.topface.topface.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;

public class DoubleBigButton extends LinearLayout implements CompoundButton.OnCheckedChangeListener {
    // Data
    private RadioButton mLeftButton;
    private RadioButton mRightButoon;
    // Constants
    public static final int LEFT_BUTTON = 0;
    public static final int RIGHT_BUTTON = 1;
    //---------------------------------------------------------------------------
    public DoubleBigButton(Context context) {
        super(context, null);
    }
    //---------------------------------------------------------------------------
    public DoubleBigButton(Context context,AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.btn_double_big, this, true);

        mLeftButton = (RadioButton)findViewById(R.id.dblLeft);
        mRightButoon = (RadioButton)findViewById(R.id.dblRight);
    }
    //---------------------------------------------------------------------------
    public void setLeftText(String text) {
        mLeftButton.setText(text);
    }
    //---------------------------------------------------------------------------
    public void setRightText(String text) {
        mRightButoon.setText(text);
    }
    //---------------------------------------------------------------------------
    public void setChecked(int n) {
        switch (n) {
            case LEFT_BUTTON:
                mLeftButton.setChecked(true);
                break;
            case RIGHT_BUTTON:
                mRightButoon.setChecked(true);
                break;
        }
    }
    //---------------------------------------------------------------------------
    public void setLeftListener(OnClickListener onClickListener) {
        mLeftButton.setOnClickListener(onClickListener);
        mLeftButton.setOnCheckedChangeListener(this);
    }
    //---------------------------------------------------------------------------
    public void setRightListener(OnClickListener onClickListener) {
        mRightButoon.setOnClickListener(onClickListener);
        mRightButoon.setOnCheckedChangeListener((OnCheckedChangeListener)this);
    }
    //---------------------------------------------------------------------------
    @Override
    public void onCheckedChanged(CompoundButton button,boolean value) {
        switch (button.getId()) {
            case R.id.dblLeft:
                //mLeftButton.setChecked(true);
                break;
            case R.id.dblRight:
                //mRightButoon.setChecked(true);
                break;
            default:
                break;
        }
    }
    //---------------------------------------------------------------------------
    public boolean isRightButtonChecked() {
        return mRightButoon.isChecked();
    }
    //---------------------------------------------------------------------------
}

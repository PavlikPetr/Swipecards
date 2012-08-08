package com.topface.topface.ui.views;

import com.topface.topface.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class DoubleButton extends LinearLayout implements CompoundButton.OnCheckedChangeListener {
    // Data
	private RadioGroup mGroup;
    private RadioButton mLeftButton;
    private RadioButton mRightButton;
    private boolean mLeftClickableState;
    private boolean mRightClickableState;
    // Constants
    public static int LEFT_BUTTON = 0;
    public static int RIGHT_BUTTON = 1;
    //---------------------------------------------------------------------------
    public DoubleButton(Context context) {
        super(context, null);
    }
    //---------------------------------------------------------------------------
    public DoubleButton(Context context,AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.btn_double, this, true);

        mGroup = (RadioGroup)findViewById(R.id.dblGroup);
        mLeftButton = (RadioButton)findViewById(R.id.dblLeft);        
        mRightButton = (RadioButton)findViewById(R.id.dblRight);
        for (int i = 0; i < mGroup.getChildCount(); i++) {
        	if (mLeftButton == mGroup.getChildAt(i)) {
        		LEFT_BUTTON = i;
        	} else if (mRightButton == mGroup.getChildAt(i)) {
        		RIGHT_BUTTON = i;
        	}
		}
        mLeftClickableState = mGroup.getChildAt(LEFT_BUTTON).isClickable();
		mRightClickableState = mGroup.getChildAt(RIGHT_BUTTON).isClickable();
    }
    //---------------------------------------------------------------------------
    public void setLeftText(String text) {
        mLeftButton.setText(text);
    }
    //---------------------------------------------------------------------------
    public void setRightText(String text) {
        mRightButton.setText(text);
    }
    //---------------------------------------------------------------------------
    public void setChecked(int n) {
    	if (n == LEFT_BUTTON) {
    		mLeftButton.setChecked(true);    
    		mLeftClickableState = false;
    		mRightClickableState = true;
    	} else if(n == RIGHT_BUTTON) {
    		mRightButton.setChecked(true);
    		mLeftClickableState = true;
    		mRightClickableState = false;
    	}
    }
    //---------------------------------------------------------------------------
    public void setLeftListener(OnClickListener onClickListener) {
        mLeftButton.setOnClickListener(onClickListener);
        mLeftButton.setOnCheckedChangeListener(this);
    }
    //---------------------------------------------------------------------------
    public void setRightListener(OnClickListener onClickListener) {
        mRightButton.setOnClickListener(onClickListener);
        mRightButton.setOnCheckedChangeListener((OnCheckedChangeListener)this);
    }
    //---------------------------------------------------------------------------
    @Override
    public void onCheckedChanged(CompoundButton button,boolean value) {
    	switch (button.getId()) {
        case R.id.dblLeft:
        	mGroup.getChildAt(LEFT_BUTTON).setClickable(!value);            	        		
    		mLeftClickableState = !value;        		
            break;
        case R.id.dblRight:            	
    		mGroup.getChildAt(RIGHT_BUTTON).setClickable(!value);
    		mRightClickableState =!value;        		
            break;
        default:
            break;
    }
    }
    //---------------------------------------------------------------------------
    public void setClickable(boolean clickable) {
    	if (!clickable) {    		
    		mGroup.getChildAt(LEFT_BUTTON).setClickable(clickable);
    		mGroup.getChildAt(RIGHT_BUTTON).setClickable(clickable);
    	} else {    		
    		mGroup.getChildAt(LEFT_BUTTON).setClickable(mLeftClickableState);
    		mGroup.getChildAt(RIGHT_BUTTON).setClickable(mRightClickableState);
    	}
    }
}

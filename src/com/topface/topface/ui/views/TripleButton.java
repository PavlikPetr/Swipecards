package com.topface.topface.ui.views;

import com.topface.topface.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TripleButton extends LinearLayout implements OnCheckedChangeListener{

	// Data
		private RadioGroup mGroup;
	    private RadioButton mLeftButton;
	    private RadioButton mMiddleButton;
	    private RadioButton mRightButton;
	    private boolean mLeftClickableState;
	    private boolean mMiddleClickableState;
	    private boolean mRightClickableState;
	    // Constants
	    public static int LEFT_BUTTON = 0;
	    public static int MIDDLE_BUTTON = 1;
	    public static int RIGHT_BUTTON = 2;
	    //---------------------------------------------------------------------------
	    public TripleButton(Context context) {
	        super(context, null);
	    }
	    //---------------------------------------------------------------------------
	    public TripleButton(Context context,AttributeSet attrs) {
	        super(context, attrs);

	        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	        inflater.inflate(R.layout.btn_triple, this, true);

	        mGroup = (RadioGroup)findViewById(R.id.trplGroup);
	        mLeftButton = (RadioButton)mGroup.findViewById(R.id.trplLeft);
	        mMiddleButton = (RadioButton)mGroup.findViewById(R.id.trplMiddle);
	        mRightButton = (RadioButton)mGroup.findViewById(R.id.trplRight);
	        for (int i = 0; i < mGroup.getChildCount(); i++) {
	        	if (mLeftButton == mGroup.getChildAt(i)) {
	        		LEFT_BUTTON = i;
	        	} else if (mMiddleButton == mGroup.getChildAt(i)) {
	        		MIDDLE_BUTTON = i;
	        	} else if (mRightButton == mGroup.getChildAt(i)) {
	        		RIGHT_BUTTON = i;
	        	}
			}
	        mLeftClickableState = true;
	        mMiddleClickableState = true;
			mRightClickableState = true;
	    }
	    //---------------------------------------------------------------------------
	    public void setLeftText(int res) {
	        mLeftButton.setText(res);
	    }
	    //---------------------------------------------------------------------------
	    public void setMiddleText(int res) {
	        mMiddleButton.setText(res);
	    }
	    //---------------------------------------------------------------------------
	    public void setRightText(int res) {
	        mRightButton.setText(res);
	    }
	    //---------------------------------------------------------------------------
	    //---------------------------------------------------------------------------
	    public void setLeftText(String text) {
	        mLeftButton.setText(text);
	    }
	    //---------------------------------------------------------------------------
	    public void setMiddleText(String text) {
	        mMiddleButton.setText(text);
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
	    		mMiddleClickableState = true;
	    		mRightClickableState = true;
	    	} else if(n == RIGHT_BUTTON) {
	    		mMiddleButton.setChecked(true);
	    		mLeftClickableState = true;
	    		mMiddleClickableState = false;
	    		mRightClickableState = true;
	    	} else if(n == RIGHT_BUTTON) {
	    		mRightButton.setChecked(true);
	    		mLeftClickableState = true;
	    		mMiddleClickableState = true;
	    		mRightClickableState = false;
	    	}
	    }
	    //---------------------------------------------------------------------------
	    public void setLeftListener(OnClickListener onClickListener) {
	        mLeftButton.setOnClickListener(onClickListener);
	        mLeftButton.setOnCheckedChangeListener(this);
	    }
	    //---------------------------------------------------------------------------
	    public void setMiddleListener(OnClickListener onClickListener) {
	        mMiddleButton.setOnClickListener(onClickListener);
	        mMiddleButton.setOnCheckedChangeListener(this);
	    }
	    //---------------------------------------------------------------------------
	    public void setRightListener(OnClickListener onClickListener) {
	        mRightButton.setOnClickListener(onClickListener);
	        mRightButton.setOnCheckedChangeListener(this);
	    }
	    //---------------------------------------------------------------------------
	    @Override
	    public void onCheckedChanged(CompoundButton button,boolean value) {
	        switch (button.getId()) {
	            case R.id.trplLeft:
	            	mGroup.getChildAt(LEFT_BUTTON).setClickable(!value);            	        		
	        		mLeftClickableState = !value;        		
	                break;
	            case R.id.trplMiddle:
	            	mGroup.getChildAt(MIDDLE_BUTTON).setClickable(!value);            	        		
	        		mMiddleClickableState = !value;        		
	                break;
	            case R.id.trplRight:            	
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
	    		mGroup.getChildAt(MIDDLE_BUTTON).setClickable(clickable);
	    		mGroup.getChildAt(RIGHT_BUTTON).setClickable(clickable);
	    	} else {    		
	    		mGroup.getChildAt(LEFT_BUTTON).setClickable(mLeftClickableState);
	    		mGroup.getChildAt(MIDDLE_BUTTON).setClickable(mMiddleClickableState);
	    		mGroup.getChildAt(RIGHT_BUTTON).setClickable(mRightClickableState);
	    	}
	    }	    
}

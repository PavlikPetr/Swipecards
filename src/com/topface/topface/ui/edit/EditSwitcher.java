package com.topface.topface.ui.edit;

import com.topface.topface.R;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class EditSwitcher {
	private CheckBox mCheckbox;
	private TextView mTextOn;
	private TextView mTextOff;
	
	public EditSwitcher(ViewGroup root) {
		mCheckbox = (CheckBox)root.findViewById(R.id.cbSwitch);
		mTextOn = (TextView)root.findViewById(R.id.tvSwitchOn);
		mTextOff = (TextView)root.findViewById(R.id.tvSwitchOff);
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
	
	public void doSwitch() {
		setChecked(!mCheckbox.isChecked());		
	}
	
	public boolean isChecked() {
		return mCheckbox.isChecked();
	}
}

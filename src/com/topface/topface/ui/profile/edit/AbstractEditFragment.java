package com.topface.topface.ui.profile.edit;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public abstract class AbstractEditFragment extends Fragment {
	
	protected Button mSaveButton;
	protected ProgressBar mRightPrsBar;	
	
	protected void prepareRequestSend() {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (mRightPrsBar != null && mSaveButton != null) {
					mRightPrsBar.setVisibility(View.VISIBLE);				
					mSaveButton.setVisibility(View.INVISIBLE);
				}
			}
		});
	}
	
	protected void finishRequestSend() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mRightPrsBar != null && mSaveButton != null) {
					mRightPrsBar.setVisibility(View.GONE);
					mSaveButton.setVisibility(View.VISIBLE);
				}
			}
		});
	}	
	
	abstract boolean hasChanges();
	abstract void saveChanges();	
}

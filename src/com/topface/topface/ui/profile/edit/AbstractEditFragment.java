package com.topface.topface.ui.profile.edit;

import com.topface.topface.ui.fragments.BaseFragment;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public abstract class AbstractEditFragment extends BaseFragment {
	
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
					if (hasChanges()) {
						mSaveButton.setVisibility(View.VISIBLE);
					} else {
						mRightPrsBar.setVisibility(View.INVISIBLE);		
					}
				}
			}
		});
	}	
	
	protected void refreshSaveState() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
					if(mSaveButton != null) {
						if (hasChanges()) {
							mSaveButton.setVisibility(View.VISIBLE);
						} else {
							mSaveButton.setVisibility(View.INVISIBLE);
						}
					}
			}
		});
	}
	
	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		intent.putExtra(EditContainerActivity.INTENT_REQUEST_KEY, requestCode);
		super.startActivityForResult(intent, requestCode);
	}
	
	abstract boolean hasChanges();
	abstract void saveChanges();	
}

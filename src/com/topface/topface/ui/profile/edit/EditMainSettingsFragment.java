package com.topface.topface.ui.profile.edit;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.utils.CacheProfile;

public class EditMainSettingsFragment extends AbstractEditFragment {

	public enum EditType {NAME, AGE, STATUS}; 
	
	private EditType[] mTypes;
	private HashMap<EditType, String> hashChangedData = new HashMap<EditMainSettingsFragment.EditType, String>();
	
	public EditMainSettingsFragment(EditType[] type) {
		mTypes = type;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.ac_edit_with_edittext, null, false);

		// Navigation bar
		((TextView) getActivity().findViewById(R.id.tvNavigationTitle))
				.setText(R.string.edit_title);
		TextView subTitle = (TextView) getActivity().findViewById(R.id.tvNavigationSubtitle);
		subTitle.setVisibility(View.VISIBLE);
		subTitle.setText(R.string.edit_bg_photo);

		((Button) getActivity().findViewById(R.id.btnNavigationHome)).setVisibility(View.GONE);
		Button btnBack = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
		btnBack.setVisibility(View.VISIBLE);
		btnBack.setText(R.string.navigation_edit);
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});

		mSaveButton = (Button) getActivity().findViewById(R.id.btnNavigationRightWithText);		
		mSaveButton.setText(getResources().getString(R.string.navigation_save));
		mSaveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveChanges();				
			}
		});

		mRightPrsBar = (ProgressBar) getActivity().findViewById(R.id.prsNavigationRight);

		for (final EditType type : mTypes) {
			String data = getDataByEditType(type);			
			switch (type) {
			case NAME:
				ViewGroup loName = (ViewGroup) root.findViewById(R.id.loName);
				loName.setVisibility(View.VISIBLE);
				EditText edName = (EditText) loName.findViewById(R.id.edNameText);
				edName.setText(data);
				edName.addTextChangedListener(new TextWatcher() {
					String before = Static.EMPTY;
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) { }
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
						before = s.toString();
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						String after = s.toString();
						if(!before.equals(after)) {
							hashChangedData.put(type, after);
							refreshSaveState();
						}
					}
				});
				
				break;
			case AGE:
				ViewGroup loAge = (ViewGroup) root.findViewById(R.id.loAge);
				loAge.setVisibility(View.VISIBLE);
				EditText edAge = (EditText) loAge.findViewById(R.id.edAgeText);
				edAge.setText(data);
				edAge.addTextChangedListener(new TextWatcher() {
					String before = Static.EMPTY;
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) { }
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
						before = s.toString();
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						String after = s.toString();
						if(!before.equals(after)) {
							hashChangedData.put(type, after);
							refreshSaveState();
						}
					}
				});
				break;
			case STATUS:
				ViewGroup loStatus = (ViewGroup) root.findViewById(R.id.loStatus);			
				loStatus.setVisibility(View.VISIBLE);
				EditText edStatus = (EditText) loStatus.findViewById(R.id.edStatusText);
				edStatus.setText(data);			
				edStatus.addTextChangedListener(new TextWatcher() {
					String before = Static.EMPTY;
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) { }
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count, int after) { 
						before = s.toString();
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						String after = s.toString();
						if(!before.equals(after)) {
							hashChangedData.put(type, after);
							refreshSaveState();
						}
					}
				});
				break;
			}
			
			hashChangedData.put(type, data);
		}
		return root;
	}

	@Override
	protected boolean hasChanges() {
		for (EditType type : hashChangedData.keySet()) {
			if(!getDataByEditType(type).equals(hashChangedData.get(type)))
				return true;
		}
		return false;
	}

	@Override
	protected void saveChanges() {
		if (hasChanges()) {
			SettingsRequest request = getSettigsRequest();			
			
			prepareRequestSend();
			registerRequest(request);
			request.callback(new ApiHandler() {

				@Override
				public void success(ApiResponse response) throws NullPointerException {
					for (EditType type : hashChangedData.keySet()) {
						setDataByEditType(type, hashChangedData.get(type));
					}
					getActivity().setResult(Activity.RESULT_OK);
					finishRequestSend();
				}

				@Override
				public void fail(int codeError, ApiResponse response)
						throws NullPointerException {
					getActivity().setResult(Activity.RESULT_CANCELED);
					finishRequestSend();
				}
			}).exec();			
		}
	}

	@Override
	public void fillLayout() {
	}

	@Override
	public void clearLayout() {
	}
	
	private String getDataByEditType(EditType type) {		
		switch(type) {
		case NAME:
			return CacheProfile.first_name;
		case AGE:
			return Integer.toString(CacheProfile.age);
		case STATUS:
			return CacheProfile.status;
		}
		return Static.EMPTY;
	}
	
	private void setDataByEditType(EditType type, String data) {		
		switch(type) {
		case NAME:
			CacheProfile.first_name = data;
			break;
		case AGE:
			CacheProfile.age = Integer.parseInt(data);
			break;
		case STATUS:
			CacheProfile.status = data;
			break;
		}		
	}	
	
	private SettingsRequest getSettigsRequest() {
		SettingsRequest request = new SettingsRequest(getActivity().getApplicationContext());
		for (EditType type : hashChangedData.keySet()) {
			String changedValue = hashChangedData.get(type);
			if (!changedValue.equals(getDataByEditType(type))) {
				switch (type) {
				case NAME:					
					request.name = changedValue;
					break;
				case AGE:					
					request.age = Integer.parseInt(changedValue);
					break;
				case STATUS:					
					request.status = changedValue;
					break;				
				}
			}
		}
		return request;
	}
}

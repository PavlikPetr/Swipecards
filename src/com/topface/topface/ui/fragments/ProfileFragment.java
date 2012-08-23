package com.topface.topface.ui.fragments;

import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.utils.AuthToken;
import com.topface.topface.utils.Debug;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ProfileFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
		super.onCreateView(inflater, container, saved);
		View view = inflater.inflate(R.layout.ac_profile, null);

//		((Button) view.findViewById(R.id.DELETESSID))
//				.setOnClickListener(new View.OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						Data.SSID = "asd";
//					}
//				});
//
//		((Button) view.findViewById(R.id.DELETETOKEN))
//				.setOnClickListener(new View.OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						AuthToken token = new AuthToken(getActivity().getApplicationContext()); 						
//						token.saveToken(token.getSocialNet(), token.getUserId(), "aSD", token.getExpires());
//					}
//				});

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void clearLayout() {
		Debug.log(this, "SettingsActivity::clearLayout");
	}

	@Override
	public void fillLayout() {
		Debug.log(this, "SettingsActivity::fillLayout");
	}

	@Override
	protected void onUpdateStart(boolean isFlyUpdating) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onUpdateSuccess(boolean isFlyUpdating) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onUpdateFail(boolean isFlyUpdating) {
		// TODO Auto-generated method stub

	}

}

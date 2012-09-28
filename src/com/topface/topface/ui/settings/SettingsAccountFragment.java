package com.topface.topface.ui.settings;

import com.topface.topface.R;
import com.topface.topface.utils.Settings;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SettingsAccountFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_account, container, false);
		
		// Navigation bar
		getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
		Button btnBack = (Button)getActivity().findViewById(R.id.btnNavigationBackWithText);
		btnBack.setVisibility(View.VISIBLE);
		btnBack.setText(R.string.navigation_back_settings);
		btnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
		((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(R.string.settings_account);
		
		((TextView)root.findViewById(R.id.tvName)).setText(Settings.getInstance().getSocialAccountName());
		
		return root;
	}

}

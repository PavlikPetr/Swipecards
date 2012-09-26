package com.topface.topface.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.BaseFragmentActivity;

public class SettingsContainerActivity extends BaseFragmentActivity{	
	
	Fragment mFragment; 
	
	public static final int INTENT_ACCOUNT = 201;
	public static final int INTENT_FEEDBACK = 202;
	public static final int INTENT_ABOUT = 203;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.fragment_frame);
		
		overridePendingTransition(R.anim.slide_in_from_right,R.anim.slide_out_left);
		
		Intent intent = getIntent();
		
		switch (intent.getIntExtra(Static.INTENT_REQUEST_KEY,0)) {
		case INTENT_ACCOUNT:
			mFragment = new SettingsAccountFragment();
			break;
		case INTENT_FEEDBACK:
			mFragment = new SettingsFeedbackFragment();
			break;
		case INTENT_ABOUT:
			mFragment = new SettingsAboutFragment();
			break;
		default:
			break;
		}
		
		if(mFragment != null) {
			getSupportFragmentManager().beginTransaction().replace(R.id.loFrame, mFragment).commit();
		}
	}
	
	@Override
	public void finish() {		
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_right);
	}
}

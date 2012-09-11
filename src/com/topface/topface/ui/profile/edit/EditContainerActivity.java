package com.topface.topface.ui.profile.edit;

import com.topface.topface.R;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

public class EditContainerActivity extends FragmentActivity {

	public static final String INTENT_REQUEST_KEY = "requestCode";
	
	public static final String INTENT_FORM_TITLE_ID = "title_id";
	public static final String INTENT_FORM_DATA_ID = "data_id";
	
	public static final int INTENT_EDIT_BACKGROUND = 222;
	public static final int INTENT_EDIT_ALBUM = 333;
	public static final int INTENT_EDIT_FORM_ITEM = 444;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.fragment_frame);		
		
		Fragment fragment = null;
		
		Intent intent = getIntent();		
		switch (intent.getIntExtra(INTENT_REQUEST_KEY,0)) {
		case INTENT_EDIT_BACKGROUND:
			fragment = new EditBackgroundFragment();
			break;
		case INTENT_EDIT_FORM_ITEM:
			int titleId = intent.getIntExtra(INTENT_FORM_TITLE_ID, -1);
			int dataId = intent.getIntExtra(INTENT_FORM_TITLE_ID, -1);
			fragment = new EditFormItemsFragment(titleId, dataId);
			break;
		default:
			break;
		}
		
		if(fragment != null) {
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_frame, fragment).commit();
		}
	}
	
}

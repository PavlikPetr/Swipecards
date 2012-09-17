package com.topface.topface.ui.profile.edit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.topface.topface.R;

public class EditContainerActivity extends FragmentActivity {

	public static final String INTENT_REQUEST_KEY = "requestCode";
	
	public static final String INTENT_FORM_TITLE_ID = "title_id";
	public static final String INTENT_FORM_DATA_ID = "data_id";
	public static final String INTENT_FORM_DATA = "data";
	
	public static final int INTENT_EDIT_BACKGROUND = 222;
	public static final int INTENT_EDIT_ALBUM = 333;
	public static final int INTENT_EDIT_FORM_ITEM = 444;
	public static final int INTENT_EDIT_INPUT_FORM_ITEM = 555;
	
	private Fragment mFragment;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.fragment_frame);		
		
		Intent intent = getIntent();
		switch (intent.getIntExtra(INTENT_REQUEST_KEY,0)) {
		case INTENT_EDIT_BACKGROUND:
			mFragment = new EditBackgroundFragment();
			break;
		case INTENT_EDIT_FORM_ITEM:
			int titleId = intent.getIntExtra(INTENT_FORM_TITLE_ID, -1);
			int dataId = intent.getIntExtra(INTENT_FORM_DATA_ID, -1);
			String data = intent.getStringExtra(INTENT_FORM_DATA);
			mFragment = new EditFormItemsFragment(titleId, dataId, data);
			break;
		default:
			break;
		}
		
		if(mFragment != null) {
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragment_frame, mFragment).commit();
		}
	}		
	
}

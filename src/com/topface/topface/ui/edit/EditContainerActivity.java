package com.topface.topface.ui.edit;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.edit.EditMainSettingsFragment.EditType;
import com.topface.topface.ui.profile.ProfilePhotoFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class EditContainerActivity extends BaseFragmentActivity {	
	
	public static final String INTENT_FORM_TITLE_ID = "title_id";
	public static final String INTENT_FORM_DATA_ID = "data_id";
	public static final String INTENT_FORM_DATA = "data";
	
	public static final int INTENT_EDIT_NAME_AGE = 111;
	public static final int INTENT_EDIT_STATUS = 222;
	public static final int INTENT_EDIT_BACKGROUND = 333;
	public static final int INTENT_EDIT_ALBUM = 444;
	public static final int INTENT_EDIT_FORM_ITEM = 555;
	public static final int INTENT_EDIT_INPUT_FORM_ITEM = 666;
	
	public static final int INTENT_EDIT_FILTER = 777;
	public static final int INTENT_EDIT_FILTER_FORM_CHOOSE_ITEM = 888;
	
	private Fragment mFragment;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.fragment_frame);
		
		overridePendingTransition(R.anim.slide_in_from_right,R.anim.slide_out_left);
		
		int titleId;
		int dataId;
		String data;
		Intent intent = getIntent();
		switch (intent.getIntExtra(Static.INTENT_REQUEST_KEY,0)) {
		case INTENT_EDIT_NAME_AGE:
			mFragment = new EditMainSettingsFragment(new EditType[]{EditType.NAME,EditType.AGE});
			break;
		case INTENT_EDIT_STATUS:
			mFragment = new EditMainSettingsFragment(new EditType[]{EditType.STATUS});
			break;
		case INTENT_EDIT_BACKGROUND:
			mFragment = new EditBackgroundFragment();
			break;
		case INTENT_EDIT_FORM_ITEM:
			titleId = intent.getIntExtra(INTENT_FORM_TITLE_ID, -1);
			dataId = intent.getIntExtra(INTENT_FORM_DATA_ID, -1);
			data = intent.getStringExtra(INTENT_FORM_DATA);
			mFragment = new EditFormItemsFragment(titleId, dataId, data);			
			break;
		case INTENT_EDIT_INPUT_FORM_ITEM:
			titleId = intent.getIntExtra(INTENT_FORM_TITLE_ID, -1);			
			data = intent.getStringExtra(INTENT_FORM_DATA);
			mFragment = new EditFormItemInputFragment(titleId, data);
			break;
		case INTENT_EDIT_ALBUM:
			mFragment = new ProfilePhotoFragment();
			break;
		case INTENT_EDIT_FILTER:
			mFragment = new FilterFragment();
			break;
		case INTENT_EDIT_FILTER_FORM_CHOOSE_ITEM:
			titleId = intent.getIntExtra(INTENT_FORM_TITLE_ID, -1);
			dataId = intent.getIntExtra(INTENT_FORM_DATA_ID, -1);
			data = intent.getStringExtra(INTENT_FORM_DATA);
			mFragment = new FilterChooseFormItemFragment(titleId, dataId, data, FilterFragment.mTargetUser);		
		default:
			break;
		}
		
		if(mFragment != null) {
			getSupportFragmentManager().beginTransaction()
				.replace(R.id.loFrame, mFragment).commit();
		}
	}	
	
	@Override
	public void finish() {		
		super.finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_right);
	}
}

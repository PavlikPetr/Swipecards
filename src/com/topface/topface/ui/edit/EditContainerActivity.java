package com.topface.topface.ui.edit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.edit.EditMainFormItemsFragment.EditType;
import com.topface.topface.ui.views.LockerView;

public class EditContainerActivity extends BaseFragmentActivity {

    public static final String INTENT_FORM_TITLE_ID = "title_id";
    public static final String INTENT_FORM_DATA_ID = "data_id";
    public static final String INTENT_FORM_DATA = "data";

    public static final String INTENT_AGE_START = "age_start";
    public static final String INTENT_AGE_END = "age_end";
    public static final String FILTER_SEX = "filter_sex";

    public static final int INTENT_EDIT_NAME_AGE = 101;
    public static final int INTENT_EDIT_STATUS = 102;
    public static final int INTENT_EDIT_BACKGROUND = 103;
    public static final int INTENT_EDIT_ALBUM = 104;
    public static final int INTENT_EDIT_FORM_ITEM = 105;
    public static final int INTENT_EDIT_INPUT_FORM_ITEM = 106;
    public static final int INTENT_EDIT_PROFILE_PHOTO = 107;
    public static final int INTENT_EDIT_AGE = 108;

    public static final int INTENT_EDIT_FILTER = 201;
    public static final int INTENT_EDIT_FILTER_FORM_CHOOSE_ITEM = 202;

    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.fragment_frame);

        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left);

        LockerView lv = (LockerView) findViewById(R.id.loProfileLoading);

        int titleId;
        int dataId;
        String data;
        Intent intent = getIntent();
        switch (intent.getIntExtra(Static.INTENT_REQUEST_KEY, 0)) {
            case INTENT_EDIT_NAME_AGE:
                mFragment = new EditMainFormItemsFragment(new EditType[]{EditType.NAME, EditType.AGE});
                break;
            case INTENT_EDIT_STATUS:
                mFragment = new EditMainFormItemsFragment(new EditType[]{EditType.STATUS});
                break;
            case INTENT_EDIT_BACKGROUND:
                mFragment = new EditBackgroundFragment();
                break;
            case INTENT_EDIT_FORM_ITEM:
                titleId = intent.getIntExtra(INTENT_FORM_TITLE_ID, -1);
                dataId = intent.getIntExtra(INTENT_FORM_DATA_ID, -1);
                data = intent.getStringExtra(INTENT_FORM_DATA);
                mFragment = EditFormItemsFragment.newInstance(titleId, dataId, data);
                break;
            case INTENT_EDIT_INPUT_FORM_ITEM:
                titleId = intent.getIntExtra(INTENT_FORM_TITLE_ID, -1);
                data = intent.getStringExtra(INTENT_FORM_DATA);
                mFragment = new EditFormItemInputFragment(titleId, data);
                break;
            case INTENT_EDIT_ALBUM:
                mFragment = new EditProfilePhotoFragment(lv);
                break;
            case INTENT_EDIT_FILTER:
                mFragment = new FilterFragment();
                break;
            case INTENT_EDIT_FILTER_FORM_CHOOSE_ITEM:
                titleId = intent.getIntExtra(INTENT_FORM_TITLE_ID, -1);
                dataId = intent.getIntExtra(INTENT_FORM_DATA_ID, -1);
                data = intent.getStringExtra(INTENT_FORM_DATA);
                mFragment = new FilterChooseFormItemFragment(titleId, dataId, data, FilterFragment.mTargetUser);
                break;
            case INTENT_EDIT_PROFILE_PHOTO:
                mFragment = new EditProfilePhotoFragment(lv);
                break;
            case INTENT_EDIT_AGE:
                int age_start = intent.getIntExtra(INTENT_AGE_START, 16);
                int age_end = intent.getIntExtra(INTENT_AGE_END, 32);
                int sex = intent.getIntExtra(FILTER_SEX, 1);
                mFragment = new EditAgeFragment(age_start, age_end, sex);
                break;
            default:
                break;
        }

        if (mFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.loFrame, mFragment).commit();
        }
    }

    @Override
    public void finish() {

        if (mFragment instanceof AbstractEditFragment) {
            AbstractEditFragment editFragment = (AbstractEditFragment) mFragment;
            editFragment.saveChanges(mFinishHandler);

        } else {
            super.finish();
        }
    }

    Handler mFinishHandler = new Handler() {
        public void handleMessage(Message msg) {
            EditContainerActivity.super.finish();
        }
    };

    @Override
    public boolean isTrackable() {
        return false;
    }
}

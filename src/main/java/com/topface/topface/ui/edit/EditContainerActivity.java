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

public class EditContainerActivity extends BaseFragmentActivity {

    public static final String INTENT_FORM_TITLE_ID = "titleId";
    public static final String INTENT_FORM_DATA_ID = "dataId";
    public static final String INTENT_FORM_DATA = "data";
    public static final String INTENT_FORM_LIMIT_VALUE = "limit_value";

    public static final String INTENT_AGE_START = "ageStart";
    public static final String INTENT_AGE_END = "ageEnd";
    public static final String FILTER_SEX = "filterSex";

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

    Handler mFinishHandler = new Handler() {
        public void handleMessage(Message msg) {
            EditContainerActivity.super.finish();
        }
    };
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.ac_fragment_frame);
        int titleId;
        int dataId;
        String data;
        int limitValue;
        Intent intent = getIntent();
        switch (intent.getIntExtra(Static.INTENT_REQUEST_KEY, 0)) {
            case INTENT_EDIT_NAME_AGE:
                mFragment = EditMainFormItemsFragment.newInstance(new EditType[]{EditType.NAME, EditType.AGE});
                break;
            case INTENT_EDIT_STATUS:
                mFragment = EditMainFormItemsFragment.newInstance(new EditType[]{EditType.STATUS});
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
                if (intent.hasExtra(INTENT_FORM_LIMIT_VALUE)) {
                    limitValue = intent.getIntExtra(INTENT_FORM_LIMIT_VALUE, EditFormItemInputFragment.UNUSED_LIMIT_VALUE);
                    mFragment = EditFormItemInputFragment.newInstance(titleId, data, limitValue);
                    break;
                }
                mFragment = EditFormItemInputFragment.newInstance(titleId, data);
                break;
            case INTENT_EDIT_ALBUM:
                mFragment = new EditProfilePhotoFragment();
                break;
            case INTENT_EDIT_FILTER:
                mFragment = new FilterFragment();
                break;
            case INTENT_EDIT_FILTER_FORM_CHOOSE_ITEM:
                titleId = intent.getIntExtra(INTENT_FORM_TITLE_ID, -1);
                dataId = intent.getIntExtra(INTENT_FORM_DATA_ID, -1);
                data = intent.getStringExtra(INTENT_FORM_DATA);
                mFragment = FilterChooseFormItemFragment.newInstance(titleId, dataId, data,
                        FilterFragment.mTargetUser.sex, FilterFragment.mTargetUser.getType());
                break;
            case INTENT_EDIT_PROFILE_PHOTO:
                mFragment = new EditProfilePhotoFragment();
                break;
            case INTENT_EDIT_AGE:
                mFragment = EditAgeFragment.newInstance(
                        intent.getIntExtra(INTENT_AGE_START, 16),
                        intent.getIntExtra(INTENT_AGE_END, 32),
                        intent.getIntExtra(FILTER_SEX, Static.BOY)
                );
                break;
            default:
                break;
        }

        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(mFragment.getClass().getCanonicalName());
        if (fragmentByTag != null) {
            mFragment = fragmentByTag;
        }
        if (mFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.loFrame, mFragment, mFragment.getClass().getCanonicalName()).commit();
        }
    }

    @Override
    public void finish() {
        if (mFragment instanceof AbstractEditFragment) {
            ((AbstractEditFragment) mFragment).saveChanges(mFinishHandler);
        } else {
            super.finish();
        }
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

}

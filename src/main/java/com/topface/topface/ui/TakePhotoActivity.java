package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.profile.TakePhotoFragment;

/**
 * Container for take-photo-fragment
 * Created by mbautin on 08.12.15.
 */
public class TakePhotoActivity extends BaseFragmentActivity implements TakePhotoFragment.ITakePhotoUserActionListener {
    public static final int REQUEST_CODE_TAKE_PHOTO = 1998;
    public static final String EXTRA_TAKE_PHOTO_USER_ACTION = "TakePhotoActivity.Extra.TakePhotoUserAction";
    public static final String EXTRA_PLC = "TakePhotoActivity.Extra.Plc";
    private Intent mResultIntent;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Fragment fragment = TakePhotoFragment.newInstance();
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.loFrame, fragment).commit();
        }
        mResultIntent = new Intent();
        Intent intent = getIntent();
        mResultIntent.putExtra(EXTRA_PLC, intent.getStringExtra(EXTRA_PLC));
        mResultIntent.putExtra(EXTRA_TAKE_PHOTO_USER_ACTION, TakePhotoFragment.ACTION_CANCEL);
        setResult(RESULT_CANCELED, mResultIntent);
    }


    @Override
    protected int getContentLayout() {
        return R.layout.ac_fragment_frame;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    public static Intent createIntent(Context context, String plc) {
        Intent intent = new Intent(context, TakePhotoActivity.class);
        intent.putExtra(EXTRA_PLC, plc);
        return intent;
    }

    @Override
    public void onTakePhotoUserAction(@TakePhotoFragment.TakePhotoUserAction int userAction) {
        mResultIntent.putExtra(EXTRA_TAKE_PHOTO_USER_ACTION, userAction);
        setResult(RESULT_OK, mResultIntent);
        finish();
    }
}

package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.IUniversalUser;
import com.topface.topface.data.UniversalUserFactory;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.fragments.OwnAvatarFragment;
import com.topface.topface.ui.fragments.SettingsFragment;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.IPhotoTakerWithDialog;
import com.topface.topface.utils.PhotoTaker;
import com.topface.topface.utils.actionbar.OverflowMenu;

/**
 * Created by kirussell on 18.03.14.
 * Profile fragment for current authorized client with ui for customization of user settings
 */
public class OwnProfileFragment extends OwnAvatarFragment {
    private AddPhotoHelper mAddPhotoHelper;
    private BroadcastReceiver mAddPhotoReceiver;
    private BroadcastReceiver mUpdateProfileReceiver;
    private IPhotoTakerWithDialog mPhotoTaker;
    private MenuItem mBarAvatar;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AddPhotoHelper.handlePhotoMessage(msg);
        }
    };

    public static OwnProfileFragment newInstance() {
        return new OwnProfileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        initAddPhotoHelper();
        mPhotoTaker = new PhotoTaker(mAddPhotoHelper, getActivity());
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mUpdateProfileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onProfileUpdated();
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateProfileReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
        TakePhotoDialog takePhotoDialog = (TakePhotoDialog) mPhotoTaker.getActivityFragmentManager().findFragmentByTag(TakePhotoDialog.TAG);
        if (CacheProfile.photo == null && mAddPhotoHelper != null && takePhotoDialog == null && !App.getConfig().getUserConfig().isUserAvatarAvailable()) {
            mAddPhotoHelper.showTakePhotoDialog(mPhotoTaker, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateProfileReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mAddPhotoReceiver);
    }

    @Override
    protected void onProfileUpdated() {
        super.onProfileUpdated();
        setProfile(CacheProfile.getProfile());
    }

    @Override
    protected void initBody() {
        super.initBody();
        addBodyPage(ProfilePhotoFragment.class.getName(), getResources().getString(R.string.profile_photo));
        addBodyPage(ProfileFormFragment.class.getName(), getResources().getString(R.string.profile_form));
        addBodyPage(VipBuyFragment.class.getName(), getResources().getString(R.string.vip_status));
        addBodyPage(SettingsFragment.class.getName(), getResources().getString(R.string.settings_header_title));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_profile);
        if (item != null && mBarAvatar != null) {
            item.setChecked(mBarAvatar.isChecked());
        }
        mBarAvatar = item;
        MenuItemCompat.getActionView(mBarAvatar).findViewById(R.id.ivBarAvatarContainer).setOnClickListener(this);

        setActionBarAvatar(getUniversalUser());
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_avatar;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.profile_header_title);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case AddPhotoHelper.GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY:
                case AddPhotoHelper.GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA:
                    if (mAddPhotoHelper != null) {
                        mAddPhotoHelper.processActivityResult(requestCode, resultCode, data);
                    }
                    break;
                case AddPhotoHelper.GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY_WITH_DIALOG:
                case AddPhotoHelper.GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA_WITH_DIALOG:
                    if (mAddPhotoHelper != null) {
                        mAddPhotoHelper.showTakePhotoDialog(mPhotoTaker, mAddPhotoHelper.processActivityResult(requestCode, resultCode, data, false));
                    }
                    break;
            }
        }
    }

    private void initAddPhotoHelper() {
        mAddPhotoHelper = new AddPhotoHelper(this, null);
        mAddPhotoHelper.setOnResultHandler(mHandler);
        mAddPhotoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                FragmentActivity activity = getActivity();
                if (activity != null && mAddPhotoHelper != null) {
                    int id = intent.getIntExtra("btn_id", 0);
                    View view = new View(activity);
                    view.setId(id);
                    mAddPhotoHelper.getAddPhotoClickListener().onClick(view);
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mAddPhotoReceiver, new IntentFilter(ADD_PHOTO_INTENT));
    }

    @Override
    protected IUniversalUser createUniversalUser() {
        return UniversalUserFactory.create(getProfile());
    }

    @Override
    protected OverflowMenu createOverflowMenu(Menu barActions) {
        return new OverflowMenu(getActivity(), barActions);
    }

    @Override
    protected void initOverflowMenuActions(OverflowMenu overflowMenu) {
        overflowMenu.initOverfowMenu();
    }
}

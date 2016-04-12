package com.topface.topface.ui.fragments.profile;

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
import com.topface.topface.data.Profile;
import com.topface.topface.data.UniversalUserFactory;
import com.topface.topface.statistics.TakePhotoStatistics;
import com.topface.topface.ui.dialogs.TakePhotoPopup;
import com.topface.topface.ui.fragments.OwnAvatarFragment;
import com.topface.topface.ui.fragments.SettingsFragment;
import com.topface.topface.ui.fragments.VkProfileFragment;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.BuyVipFragmentManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.utils.actionbar.OverflowMenu;
import com.topface.topface.utils.social.AuthToken;

/**
 * Created by kirussell on 18.03.14.
 * Profile fragment for current authorized client with ui for customization of user settings
 */
public class OwnProfileFragment extends OwnAvatarFragment {
    private AddPhotoHelper mAddPhotoHelper;
    private BroadcastReceiver mAddPhotoReceiver;
    private BroadcastReceiver mUpdateProfileReceiver;
    private boolean mIsPhotoAsked;
    private static final String TAKE_PHOTO_DIALOG_SHOWN = "dialog_shown";
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
        if (savedInstanceState != null) {
            mIsPhotoAsked = savedInstanceState.getBoolean(TAKE_PHOTO_DIALOG_SHOWN);
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(TAKE_PHOTO_DIALOG_SHOWN, mIsPhotoAsked);
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
        showTakePhotoDialog(TakePhotoStatistics.PLC_OWN_PROFILE_ON_RESUME);
    }

    private void showTakePhotoDialog(String plc) {
        showTakePhotoDialog(plc, false);
    }

    private void showTakePhotoDialog(String plc, boolean forceShow) {
        if (!CacheProfile.isEmpty(getContext()) && mAddPhotoHelper != null
                && (!mIsPhotoAsked || forceShow) && (!App.getConfig().getUserConfig().isUserAvatarAvailable() && App.get().getProfile().photo == null)) {
            TakePhotoPopup.newInstance(plc).show(getActivity().getSupportFragmentManager(), TakePhotoPopup.TAG);
            mIsPhotoAsked = true;
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
        if (mAddPhotoHelper != null) {
            mAddPhotoHelper.releaseHelper();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mAddPhotoReceiver);
    }

    @Override
    protected void onProfileUpdated() {
        super.onProfileUpdated();
        if (isAdded()) {
            setProfile(App.from(getActivity()).getProfile());
        }
    }

    @Override
    protected void initBody() {
        super.initBody();
        addBodyPage(ProfilePhotoFragment.class.getName(), getResources().getString(R.string.profile_photo));
        addBodyPage(ProfileFormFragment.class.getName(), getResources().getString(R.string.profile_form));
        addBodyPage(BuyVipFragmentManager.getClassName(), getResources().getString(R.string.vip_status));
        addBodyPage(SettingsFragment.class.getName(), getResources().getString(R.string.settings_header_title));
        if (AuthToken.getInstance().getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            addBodyPage(VkProfileFragment.class.getName(), getResources().getString(R.string.general_vk_profile));
        }
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
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected boolean isNeedShowOverflowMenu() {
        return false;
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
        if (mAddPhotoHelper != null) {
            mAddPhotoHelper.processActivityResult(requestCode, resultCode, data);
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
                    int id = intent.getIntExtra(AddPhotoHelper.EXTRA_BUTTON_ID, 0);
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
        return UniversalUserFactory.create(App.from(getActivity()).getProfile());
    }

    @Override
    protected OverflowMenu createOverflowMenu(Menu barActions) {
        return new OverflowMenu((IActivityDelegate) getActivity(), barActions);
    }

    @Override
    protected void initOverflowMenuActions(OverflowMenu overflowMenu) {
        overflowMenu.initOverfowMenu();
    }

    @Override
    public void onAvatarClick() {
        Profile profile = App.from(getActivity()).getProfile();
        if (profile.photo != null) {
            startActivity(PhotoSwitcherActivity.
                    getPhotoSwitcherIntent(profile.gifts.getGifts(), profile.photo.position,
                            profile.uid, profile.photosCount,
                            profile.photos));
        } else {
            showTakePhotoDialog(TakePhotoStatistics.PLC_OWN_PROFILE_AVATAR_CLICK, true);
        }
    }
}

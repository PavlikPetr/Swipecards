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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.IUniversalUser;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Profile;
import com.topface.topface.data.UniversalUserFactory;
import com.topface.topface.ui.SettingsActivity;
import com.topface.topface.ui.dialogs.TakePhotoDialog;
import com.topface.topface.ui.fragments.OwnAvatarFragment;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.ui.fragments.gift.OwnGiftsFragment;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.IPhotoTakerWithDialog;
import com.topface.topface.utils.PhotoTaker;

import java.util.ArrayList;

/**
 * Created by kirussell on 18.03.14.
 * Profile fragment for current authorized client with ui for customization of user settings
 */
public class OwnProfileFragment extends OwnAvatarFragment {
    private AddPhotoHelper mAddPhotoHelper;
    private BroadcastReceiver mAddPhotoReceiver;
    private BroadcastReceiver mUpdateProfileReceiver;
    private IPhotoTakerWithDialog mPhotoTaker;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
                Photo photo = (Photo) msg.obj;
                // ставим фото на аватарку только если она едиснтвенная
                if (CacheProfile.photos.size() == 0) {
                    CacheProfile.photo = photo;
                }
                // добавляется фото в начало списка
                CacheProfile.photos.addFirst(photo);
                //Увеличиваем общее количество фотографий юзера
                CacheProfile.totalPhotos += 1;
                ArrayList<Photo> photosForAdd = new ArrayList<>();
                photosForAdd.add(photo);
                Intent intent = new Intent(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
                intent.putExtra(PhotoSwitcherActivity.INTENT_PHOTOS, photosForAdd);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                // оповещаем всех об изменениях
                CacheProfile.sendUpdateProfileBroadcast();
                Toast.makeText(App.getContext(), R.string.photo_add_or, Toast.LENGTH_SHORT).show();
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(App.getContext(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public static OwnProfileFragment newInstance() {
        OwnProfileFragment fragment = new OwnProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static OwnProfileFragment newInstance(String startBodyPageClassName) {
        OwnProfileFragment fragment = new OwnProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG_INIT_BODY_PAGE, startBodyPageClassName);
        fragment.setArguments(args);
        return fragment;
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
        setProfile(CacheProfile.getProfile());
        TakePhotoDialog takePhotoDialog = (TakePhotoDialog) mPhotoTaker.getActivityFragmentManager().findFragmentByTag(TakePhotoDialog.TAG);
        if (CacheProfile.photo == null && mAddPhotoHelper != null && takePhotoDialog == null) {
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
        addBodyPage(OwnGiftsFragment.class.getName(), getResources().getString(R.string.profile_gifts));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startSettingsActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSettingsActivity() {
        startActivity(SettingsActivity.createIntent());
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

    @Override
    protected int getProfileType() {
        return Profile.TYPE_OWN_PROFILE;
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
    protected IUniversalUser getUniversalUser() {
        return UniversalUserFactory.create(getProfile());
    }
}

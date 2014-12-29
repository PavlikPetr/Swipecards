package com.topface.topface.ui.fragments.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.BasePendingInit;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.ui.IUserOnlineListener;
import com.topface.topface.ui.views.ImageViewRemote;

/**
 * Фрагмент с аватркой и именем пользователя в профиле
 */
public class HeaderMainFragment extends ProfileInnerFragment implements IUserOnlineListener {
    private static final String ARG_TAG_AVATAR = "avatar";
    private static final String ARG_TAG_NAME = "name";
    private static final String ARG_TAG_CITY = "city";
    private static final String ARG_TAG_BACKGROUND = "background";
    public static final String UPDATE_AVATAR_POSITION = "com.topface.topface.updateAvatarPosition";
    public static final String INCREMENT_AVATAR_POSITION = "incrementAvatarPosition";
    public static final String DECREMENT_AVATAR_POSITION = "decrementAvatarPosition";
    public static final String POSITION = "position";

    private ImageViewRemote mAvatarView;
    private Photo mAvatarVal;
    private TextView mNameView;
    private String mNameVal;
    private TextView mCityView;
    private String mCityVal;
    private BasePendingInit<Profile> mPendingUserInit = new BasePendingInit<>();

    private BroadcastReceiver mAvatarPositionReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAvatarVal != null) {
                boolean increment = intent.getBooleanExtra(INCREMENT_AVATAR_POSITION, false);
                boolean decrement = intent.getBooleanExtra(DECREMENT_AVATAR_POSITION, false);
                if (increment) {
                    mAvatarVal.position += 1;
                    return;
                }
                Profile profile = mPendingUserInit.getData();
                if (decrement && profile != null) {
                    if (intent.getIntExtra(POSITION, -1) < mAvatarVal.position) {
                        mAvatarVal.position -= 1;
                    }
                    profile.photosCount -= 1;
                }
            }
        }
    };

    private static void saveState(Fragment fragment, Profile profile) {
        if (!fragment.isVisible()) {
            if (fragment.getArguments() == null && !fragment.isAdded()) {
                Bundle args = new Bundle();
                fragment.setArguments(args);
            }

            if (fragment.getArguments() != null) {
                fragment.getArguments().putParcelable(ARG_TAG_AVATAR, profile.photo);
                fragment.getArguments().putString(ARG_TAG_NAME, profile.getNameAndAge());
                if (profile.city != null) {
                    fragment.getArguments().putString(ARG_TAG_CITY, profile.city.name);
                }
                fragment.getArguments().putInt(ARG_TAG_BACKGROUND, profile.background);
            }
        }
    }

    public static Fragment newInstance(Profile profile) {
        HeaderMainFragment fragment = new HeaderMainFragment();
        if (profile == null) return fragment;
        saveState(fragment, profile);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_profile_header_main, null);
        mAvatarView = (ImageViewRemote) root.findViewById(R.id.ivUserAvatar);
        mAvatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile userProfile = mPendingUserInit.getData();
                Photos photos = userProfile.photos;
                if (mAvatarVal == null || photos == null) {
                    return;
                }
                int pos;
                if (photos.size() < mAvatarVal.position) {
                    //ава за пределами загруженной пачки
                    pos = mAvatarVal.position;
                } else {
                    if (photos.get(mAvatarVal.position) == null) {
                        return;
                    }
                    if (photos.get(mAvatarVal.position).getId() != mAvatarVal.getId()) {
                        //ид не равны, юзер загрузил новые фотки
                        int id = mAvatarVal.getId();
                        pos = photos.getPhotoIndexById(id);
                    } else {
                        pos = mAvatarVal.position;
                    }
                }
                startActivity(PhotoSwitcherActivity.
                        getPhotoSwitcherIntent(pos, userProfile.uid, userProfile.photosCount, userProfile.photos));
            }
        });
        mNameView = (TextView) root.findViewById(R.id.tvName);
        mCityView = (TextView) root.findViewById(R.id.tvCity);
        return root;
    }

    @Override
    public void setOnline(boolean online) {
        // установка иконки онлайн
        mNameView.setCompoundDrawablesWithIntrinsicBounds(
                online ? R.drawable.ico_online : 0,
                0, 0, 0
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshViews();
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mAvatarPositionReciver, new IntentFilter(UPDATE_AVATAR_POSITION));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPendingUserInit.setCanSet(true);
        if (mPendingUserInit.getCanSet()) {
            setProfilePending(mPendingUserInit.getData());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPendingUserInit.setCanSet(false);
    }

    public void setProfile(Profile profile) {
        mPendingUserInit.setData(profile);
        if (mPendingUserInit.getCanSet()) {
            setProfilePending(mPendingUserInit.getData());
            if (profile instanceof User) {
                setOnline(((User) profile).online);
            }
        }
    }

    private void setProfilePending(Profile profile) {
        if (profile != null) {
            initState(profile);
            saveState(this, profile);
        }
        refreshViews();
    }

    private void refreshViews() {
        updateUI(new Runnable() {
            @Override
            public void run() {
                mAvatarView.setPhoto(mAvatarVal);
                mNameView.setText(mNameVal);
                setCity(mCityVal);
            }
        });
    }

    private void setCity(String city) {
        mCityView.setText(city);
        mCityView.setVisibility(
                TextUtils.isEmpty(city) ?
                        View.GONE :
                        View.VISIBLE
        );
    }

    @Override
    protected void restoreState() {
        if (getArguments() != null) {
            mAvatarVal = getArguments().getParcelable(ARG_TAG_AVATAR);
            mNameVal = getArguments().getString(ARG_TAG_NAME);
            mCityVal = getArguments().getString(ARG_TAG_CITY);
        }
    }

    private void initState(Profile profile) {
        if (profile != null) {
            mAvatarVal = profile.photo;
            mNameVal = profile.getNameAndAge();
            if (profile.city != null) {
                mCityVal = profile.city.name;
            }
        }
    }

    @Override
    public void clearContent() {
        mAvatarView.setPhoto(null);
        mNameView.setText(Static.EMPTY);
        mNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        setCity(Static.EMPTY);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mAvatarPositionReciver);
    }
}

package com.topface.topface.ui.fragments.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.views.ImageViewRemote;

/**
 * Фрагмент с аватркой и именем пользователя в профиле
 */
public class HeaderMainFragment extends ProfileInnerFragment {
    private static final String ARG_TAG_AVATAR = "avatar";
    private static final String ARG_TAG_NAME = "name";
    private static final String ARG_TAG_CITY = "city";
    private static final String ARG_TAG_BACKGROUND = "background";

    private ImageViewRemote mAvatarView;
    private Photo mAvatarVal;
    private TextView mNameView;
    private String mNameVal;
    private TextView mCityView;
    private String mCityVal;

    private static void saveState(Fragment fragment, Profile profile) {
        if (!fragment.isVisible()) {
            if (fragment.getArguments() == null && !fragment.isAdded()) {
                Bundle args = new Bundle();
                fragment.setArguments(args);
            }

            fragment.getArguments().putParcelable(ARG_TAG_AVATAR, profile.photo);
            fragment.getArguments().putString(ARG_TAG_NAME, profile.getNameAndAge());
            if (profile.city != null) {
                fragment.getArguments().putString(ARG_TAG_CITY, profile.city.name);
            }
            fragment.getArguments().putInt(ARG_TAG_BACKGROUND, profile.background);
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
        mNameView = (TextView) root.findViewById(R.id.tvName);
        mCityView = (TextView) root.findViewById(R.id.tvCity);
        return root;
    }

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
    }

    public void setProfile(Profile profile) {
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
    }
}

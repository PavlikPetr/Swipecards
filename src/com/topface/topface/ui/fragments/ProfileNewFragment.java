package com.topface.topface.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditProfileActivity;
import com.topface.topface.ui.profile.ProfileFormFragment;
import com.topface.topface.ui.profile.ProfilePhotoFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.NavigationBarController;
import com.topface.topface.utils.http.ProfileBackgrounds;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 05.12.12
 * Time: 11:39
 * To change this template use File | Settings | File Templates.
 */
public class ProfileNewFragment extends BaseFragment implements View.OnClickListener {
    public final static int TYPE_MY_PROFILE = 1;
    public final static int TYPE_USER_PROFILE = 2;

    private static final String ARG_TAG_PROFILE_TYPE = "profile_type";
    private static final String ARG_TAG_AVATAR = "avatar";
    private static final String ARG_TAG_NAME = "name";
    private static final String ARG_TAG_CITY = "city";
    private static final String ARG_TAG_BACKGROUND = "background";
    private static final String ARG_TAG_STATUS = "status";

    ArrayList<String> BODY_PAGES_TITLES = new ArrayList<String>();
    ArrayList<String> BODY_PAGES_CLASS_NAMES = new ArrayList<String>();

    ArrayList<String> HEADER_PAGES_CLASS_NAMES = new ArrayList<String>();

    protected NavigationBarController mNavBarController;

    private int mProfileType;
    private Photo mAvatar;
    private String mName;
    private String mCity;
    private int mBackground;
    private String mStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //init args
        mProfileType = getArguments().getInt(ARG_TAG_PROFILE_TYPE);
        mAvatar = getArguments().getParcelable(ARG_TAG_AVATAR);
        mName = getArguments().getString(ARG_TAG_NAME);
        mCity = getArguments().getString(ARG_TAG_CITY);
        mBackground = getArguments().getInt(ARG_TAG_BACKGROUND);
        mStatus = getArguments().getString(ARG_TAG_STATUS);

        //init views
        View root = inflater.inflate(R.layout.ac_profile_new, null);

        //Navigation bar
        mNavBarController = new NavigationBarController((ViewGroup) root.findViewById(R.id.loNavigationBar));
        root.findViewById(R.id.btnNavigationHome).setOnClickListener((NavigationActivity) getActivity());

        if (mProfileType == TYPE_MY_PROFILE) {
            ((TextView) root.findViewById(R.id.tvNavigationTitle)).setText(R.string.profile_header_title);

            Button editButton = (Button) root.findViewById(R.id.btnNavigationRightWithText);
            editButton.setVisibility(View.VISIBLE);
            editButton.setText(getResources().getString(R.string.general_edit_button));
            editButton.setOnClickListener(this);
        } else  if (mProfileType == TYPE_USER_PROFILE){
            ((TextView) root.findViewById(R.id.tvNavigationTitle)).setText(mName);
        }

        //Header pages
        initHeaderPages();
        ViewPager headerPager = (ViewPager)root.findViewById(R.id.vpHeaderFragments);
        headerPager.setAdapter(new ProfilePageAdapter(getActivity().getSupportFragmentManager(),
                HEADER_PAGES_CLASS_NAMES));

        //Body pages
        initBodyPages();
        ViewPager bodyPager = (ViewPager)root.findViewById(R.id.vpFragments);
        bodyPager.setAdapter(new ProfilePageAdapter(getActivity().getSupportFragmentManager(),BODY_PAGES_CLASS_NAMES,
                BODY_PAGES_TITLES));

        //Tabs for header
        CirclePageIndicator cirleIndicator = (CirclePageIndicator) root.findViewById(R.id.cpiHeaderTabs);
        cirleIndicator.setViewPager(headerPager);
        cirleIndicator.setSnap(true);

        //Tabs for Body
        TabPageIndicator tabIndicator = (TabPageIndicator)root.findViewById(R.id.tpiTabs);
        tabIndicator.setViewPager(bodyPager);

        return root;
    }

    private void initHeaderPages() {
        addHeaderPage(HeaderMainFragment.class.getName());
        addHeaderPage(HeaderStatusFragment.class.getName());
    }

    private void initBodyPages() {
        addBodyPage(ProfilePhotoFragment.class.getName(), getResources().getString(R.string.profile_photo));
        addBodyPage(ProfileFormFragment.class.getName(), getResources().getString(R.string.profile_form));
        addBodyPage(ProfileFormFragment.class.getName(), getResources().getString(R.string.profile_vip_status));
        addBodyPage(GiftsFragment.class.getName(), getResources().getString(R.string.profile_gifts));
        addBodyPage(ProfileFormFragment.class.getName(), getResources().getString(R.string.profile_services));
    }

    private void addHeaderPage(String className) {
        HEADER_PAGES_CLASS_NAMES.add(className);
    }

    private void addBodyPage(String className, String pageTitle) {
        BODY_PAGES_TITLES.add(pageTitle);
        BODY_PAGES_CLASS_NAMES.add(className);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNavigationRightWithText:
                startActivity(new Intent(getActivity().getApplicationContext(), EditProfileActivity.class));
                break;
        }
    }

    public static ProfileNewFragment newInstance(int type, Photo avatar, String nameAgeStr, String cityName, int backgroundId,String status){
        ProfileNewFragment  fragment = new ProfileNewFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TAG_PROFILE_TYPE, type);
        args.putParcelable(ARG_TAG_AVATAR, avatar);
        args.putString(ARG_TAG_NAME, nameAgeStr);
        args.putString(ARG_TAG_CITY, cityName);
        args.putInt(ARG_TAG_BACKGROUND, backgroundId);
        args.putString(ARG_TAG_STATUS, status);
        fragment.setArguments(args);

        return fragment;
    }

    public class ProfilePageAdapter extends FragmentStatePagerAdapter {

        private ArrayList<String> mFragmentsClasses = new ArrayList<String>();
        private ArrayList<String> mFragmentsTitles = new ArrayList<String>();

        public ProfilePageAdapter(FragmentManager fm, ArrayList<String> fragmentsClasses) {
            super(fm);
            mFragmentsClasses = fragmentsClasses;
        }

        public ProfilePageAdapter(FragmentManager fm, ArrayList<String> fragmentsClasses, ArrayList<String> fragmentTitles) {
            super(fm);
            mFragmentsClasses = fragmentsClasses;
            mFragmentsTitles = fragmentTitles;
        }

        @Override
        public int getCount() {
            return mFragmentsClasses.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (!mFragmentsTitles.isEmpty())
                return mFragmentsTitles.get(position);

            return super.getPageTitle(position);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            try {
                if(mFragmentsClasses.get(position).equals(HeaderMainFragment.class.getName())) {
                    fragment = HeaderMainFragment.newInstace(mAvatar,mName,mCity,
                        ProfileBackgrounds.getBackgroundResource(getActivity().getApplicationContext(),mBackground));
                } else if(mFragmentsClasses.get(position).equals(HeaderStatusFragment.class.getName())) {
                    fragment = HeaderStatusFragment.newInstace(mStatus);
                } else {
                    Class fragmentClass = Class.forName(mFragmentsClasses.get(position));
                    fragment = (Fragment) fragmentClass.newInstance();
                }


            } catch (Exception ex) {
                Debug.error(ex);
            }
            return fragment;
        }
    }

    public static class HeaderMainFragment extends BaseFragment {
        private ImageViewRemote mAvatarView;
        private Photo mAvatarVal;
        private TextView mNameView;
        private String mNameVal;
        private TextView mCityView;
        private String mCityVal;
        private ImageView mBackgroundView;
        private int mBackgroundVal;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            //init arguments
            mAvatarVal = getArguments().getParcelable(ARG_TAG_AVATAR);
            mNameVal = getArguments().getString(ARG_TAG_NAME);
            mCityVal = getArguments().getString(ARG_TAG_CITY);
            mBackgroundVal = getArguments().getInt(ARG_TAG_BACKGROUND);

            //init views
            View root = inflater.inflate(R.layout.fragment_profile_header_main, null);
            mAvatarView = (ImageViewRemote)root.findViewById(R.id.ivUserAvatar);
            mNameView = (TextView)root.findViewById(R.id.tvName);
            mCityView = (TextView)root.findViewById(R.id.tvCity);
            mBackgroundView = (ImageView) root.findViewById(R.id.ivProfileBackground);

            mAvatarView.setPhoto(mAvatarVal);
            mNameView.setText(mNameVal);
            mCityView.setText(mCityVal);
            mBackgroundView.setBackgroundResource(mBackgroundVal);
            return root;
        }

        @Override
        public void onResume() {
            super.onResume();
            mAvatarView.setPhoto(CacheProfile.photo);
            mNameView.setText(CacheProfile.getUserNameAgeString());
            mCityView.setText(CacheProfile.city_name);
            mBackgroundView.setBackgroundResource(mBackgroundVal);
        }

        public static Fragment newInstace(Photo avatar, String nameAgeStr, String cityName, int backgroundRes) {
            HeaderMainFragment  fragment = new HeaderMainFragment();

            Bundle args = new Bundle();
            args.putParcelable(ARG_TAG_AVATAR, avatar);
            args.putString(ARG_TAG_NAME, nameAgeStr);
            args.putString(ARG_TAG_CITY, cityName);
            args.putInt(ARG_TAG_BACKGROUND, backgroundRes);
            fragment.setArguments(args);

            return fragment;
        }
    }

    public static class HeaderStatusFragment extends BaseFragment {
        private TextView mStatusView;
        private String mStatusVal;

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            //init args
            mStatusVal = getArguments().getString(ARG_TAG_STATUS);

            //init views
            View root = inflater.inflate(R.layout.fragment_profile_header_status, null);
            mStatusView = (TextView)root.findViewById(R.id.tvStatus);
            mStatusView.setText(mStatusVal);
            return root;
        }

        @Override
        public void onResume() {
            super.onResume();
            mStatusView.setText(CacheProfile.status);
        }

        public static Fragment newInstace(String status) {
            HeaderStatusFragment  fragment = new HeaderStatusFragment();

            Bundle args = new Bundle();
            args.putString(ARG_TAG_STATUS, status);
            fragment.setArguments(args);

            return fragment;
        }
    }
}

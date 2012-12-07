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
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditProfileActivity;
import com.topface.topface.ui.profile.ProfileFormFragment;
import com.topface.topface.ui.profile.ProfilePhotoFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.NavigationBarController;
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

    ArrayList<String> BODY_PAGES_TITLES = new ArrayList<String>();
    ArrayList<String> BODY_PAGES_CLASS_NAMES = new ArrayList<String>();

    ArrayList<String> HEADER_PAGES_CLASS_NAMES = new ArrayList<String>();

    private ViewGroup mProfileHeader;

    protected NavigationBarController mNavBarController;
    private ImageViewRemote mUserAvatar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.ac_profile_new, null);

        //Navigation bar
        mNavBarController = new NavigationBarController((ViewGroup) root.findViewById(R.id.loNavigationBar));
        root.findViewById(R.id.btnNavigationHome).setOnClickListener((NavigationActivity) getActivity());
        ((TextView) root.findViewById(R.id.tvNavigationTitle)).setText(R.string.profile_header_title);

        Button editButton = (Button) root.findViewById(R.id.btnNavigationRightWithText);
        editButton.setVisibility(View.VISIBLE);
        editButton.setText(getResources().getString(R.string.general_edit_button));
        editButton.setOnClickListener(this);

        //Header layout
        mProfileHeader = (ViewGroup)root.findViewById(R.id.loProfileHeader);
//        mProfileHeader.setBackgroundResource(
//                ProfileBackgrounds.getBackgroundResource(
//                        getActivity().getApplicationContext(),
//                        CacheProfile.background_id
//                )
//        );

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
//        mProfileHeader.setBackgroundResource(
//                ProfileBackgrounds.getBackgroundResource(
//                        getActivity().getApplicationContext(),
//                        CacheProfile.background_id
//                )
//        );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNavigationRightWithText:
                startActivity(new Intent(getActivity().getApplicationContext(), EditProfileActivity.class));
                break;
        }
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
                Class fragmentClass = Class.forName(mFragmentsClasses.get(position));
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception ex) {
                Debug.error(ex);
            }
            return fragment;
        }
    }

    public static class HeaderMainFragment extends BaseFragment {

        private ImageViewRemote mAvatar;
        private TextView mName;
        private TextView mCity;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View root = inflater.inflate(R.layout.fragment_profile_header_main, null);
            mAvatar = (ImageViewRemote)root.findViewById(R.id.ivUserAvatar);
            mAvatar.setPhoto(CacheProfile.photo);
            mName = (TextView)root.findViewById(R.id.tvName);
            mName.setText(CacheProfile.getUserNameAgeString());
            mCity = (TextView)root.findViewById(R.id.tvCity);
            mCity.setText(CacheProfile.city_name);
            return root;
        }

        @Override
        public void onResume() {
            super.onResume();
            mAvatar.setPhoto(CacheProfile.photo);
            mName.setText(CacheProfile.getUserNameAgeString());
            mCity.setText(CacheProfile.city_name);
        }
    }

    public static class HeaderStatusFragment extends BaseFragment {

        private TextView mStatus;

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View root = inflater.inflate(R.layout.fragment_profile_header_status, null);
            mStatus = (TextView)root.findViewById(R.id.tvStatus);
            mStatus.setText(CacheProfile.status);
            return root;
        }

        @Override
        public void onResume() {
            super.onResume();
            mStatus.setText(CacheProfile.status);
        }
    }
}

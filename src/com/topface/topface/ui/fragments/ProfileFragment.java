package com.topface.topface.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditProfileActivity;
import com.topface.topface.ui.profile.ProfileFormFragment;
import com.topface.topface.ui.profile.ProfilePhotoFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.IndicatorView;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.NavigationBarController;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.ProfileBackgrounds;

public class ProfileFragment extends BaseFragment implements OnClickListener {

    private ImageViewRemote mUserAvatar;
    private ViewGroup mUserProfileHeader;

    private RadioGroup mUserRadioGroup;
    private RadioButton mUserPhoto;
    private RadioButton mUserForm;
    private RadioButton mUserGifts;

    private IndicatorView mIndicatorView;
    private ViewPager mViewPager;

    private Button mBuyButton;
    private View mUserPowerBkgd;

    private LockerView mLoadingLocker;


//    private HashMap<Integer,Fragment> mFragmentsHash;
    public static final int F_PHOTO = 0;
    public static final int F_FORM = 1;
    public static final int F_GIFTS = 2;
    public static final int F_COUNT = F_GIFTS + 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(R.layout.ac_profile, null);

//		mFragmentsHash = new HashMap<Integer, Fragment>();
        // Navigation bar
        mNavBarController = new NavigationBarController((ViewGroup) view.findViewById(R.id.loNavigationBar));

        view.findViewById(R.id.btnNavigationHome).setOnClickListener((NavigationActivity) getActivity());
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(R.string.profile_header_title);

        mLoadingLocker = (LockerView)view.findViewById(R.id.llvProfileLoading);

        Button editButton = (Button) view.findViewById(R.id.btnNavigationRightWithText);
        editButton.setVisibility(View.VISIBLE);
        editButton.setText(getResources().getString(R.string.general_edit_button));
        editButton.setOnClickListener(this);

        // Avatar, Name, City
        mUserAvatar = (ImageViewRemote) view.findViewById(R.id.ivUserAvatar);
        TextView userName = (TextView) view.findViewById(R.id.ivUserName);
        String userNameString = CacheProfile.first_name + (isAgeOk(CacheProfile.age) ? ", " + CacheProfile.age : "");
        userName.setText(userNameString);
        TextView userCity = (TextView) view.findViewById(R.id.ivUserCity);
        userCity.setText(CacheProfile.city_name);
        mUserProfileHeader = (ViewGroup) view.findViewById(R.id.loProfileHeader);

        // Actions Button
        mUserRadioGroup = (RadioGroup) view.findViewById(R.id.UserRadioGroup);
        mUserPhoto = (RadioButton) view.findViewById(R.id.btnUserPhoto);
        mUserPhoto.setOnClickListener(mInfoClickListener);
        mUserForm = (RadioButton) view.findViewById(R.id.btnUserQuestionnaire);
        mUserForm.setOnClickListener(mInfoClickListener);
        mUserGifts = (RadioButton) view.findViewById(R.id.btnUserGifts);
        mUserGifts.setOnClickListener(mInfoClickListener);

        // Resources
        TextView userMoney = (TextView) view.findViewById(R.id.tvUserMoney);
        userMoney.setText("" + CacheProfile.money);
        TextView userPower = (TextView) view.findViewById(R.id.tvUserPower);
        userPower.setOnClickListener(mBuyClickListener);
        userPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.power));
        userPower.setText("" + CacheProfile.power + "%");

        // View Pager
        mViewPager = (ViewPager) view.findViewById(R.id.UserViewPager);
        mViewPager.setAdapter(new ProfilePageAdapter(getActivity().getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);

        mUserPowerBkgd = view.findViewById(R.id.loUserPowerBkgd);

        // Buy Button
        mBuyButton = (Button) view.findViewById(R.id.btnBuy);
        mBuyButton.setOnClickListener(mBuyClickListener);
        view.findViewById(R.id.loUserRates).setOnClickListener(mBuyClickListener);

        // Indicator
        mIndicatorView = (IndicatorView) view.findViewById(R.id.viewUserIndicator);
        mIndicatorView.setIndicator(F_PHOTO);
        ViewTreeObserver vto = mIndicatorView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onGlobalLayout() {
                mIndicatorView.setButtonMeasure(R.id.btnUserPhoto, mUserPhoto.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserQuestionnaire, mUserForm.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserGifts, mUserGifts.getMeasuredWidth());

                if (mUserPhoto.getMeasuredWidth() > 0) {
                    layoutBuyButton();
                    mIndicatorView.reCompute();
                    ViewTreeObserver obs = mIndicatorView.getViewTreeObserver();
                    //noinspection deprecation
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });

        mUserPhoto.setChecked(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mUserProfileHeader.setBackgroundResource(
                ProfileBackgrounds.getBackgroundResource(
                        getActivity().getApplicationContext(),
                        CacheProfile.background_id
                )
        );

        mUserAvatar.setPhoto(CacheProfile.photo);
    }

    private void layoutBuyButton() {
        int[] in = new int[2];
        int[] out = new int[2];
        //mUserPower.getLocationInWindow(in);
        mBuyButton.getLocationOnScreen(out);
        mUserPowerBkgd.getLocationOnScreen(in);
        LayoutParams lp = mBuyButton.getLayoutParams();
        RelativeLayout.LayoutParams rlp = ((RelativeLayout.LayoutParams) lp);
        int offsetX = (mUserPowerBkgd.getMeasuredWidth() - mBuyButton.getMeasuredWidth()) / 2;
        int offsetY = (mUserPowerBkgd.getMeasuredHeight() - mBuyButton.getMeasuredWidth() / 2);
        rlp.setMargins(rlp.leftMargin + in[0] - out[0] + offsetX,
                rlp.topMargin + in[1] - out[1] + offsetY,
                rlp.rightMargin,
                rlp.bottomMargin);
        mBuyButton.setLayoutParams(rlp);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNavigationRightWithText:
                startActivity(new Intent(getActivity().getApplicationContext(), EditProfileActivity.class));
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            switch (arg0) {
                case F_PHOTO:
                    mIndicatorView.setIndicator(F_PHOTO);
                    ((RadioButton) mUserRadioGroup.getChildAt(F_PHOTO)).setChecked(true);
                    break;
                case F_FORM:
                    mIndicatorView.setIndicator(F_FORM);
                    ((RadioButton) mUserRadioGroup.getChildAt(F_FORM)).setChecked(true);
                    break;
                case F_GIFTS:
                    mIndicatorView.setIndicator(F_GIFTS);
                    ((RadioButton) mUserRadioGroup.getChildAt(F_GIFTS)).setChecked(true);
                    break;
            }
        }
    };

    private View.OnClickListener mInfoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnUserPhoto:
                    mIndicatorView.setIndicator(F_PHOTO);
                    mViewPager.setCurrentItem(F_PHOTO);
                    break;
                case R.id.btnUserQuestionnaire:
                    mIndicatorView.setIndicator(F_FORM);
                    mViewPager.setCurrentItem(F_FORM);
                    break;
                case R.id.btnUserGifts:
                    mIndicatorView.setIndicator(F_GIFTS);
                    mViewPager.setCurrentItem(F_GIFTS);
                    break;
            }
        }
    };

    private View.OnClickListener mBuyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(new Intent(getActivity(), BuyingActivity.class));
            EasyTracker.getTracker().trackEvent("Profile", "BuyClick", "", 1L);
        }
    };

    private boolean isAgeOk(int age) {
        return age > 0;
    }

    /*
    *     ProfilePageAdapter
    */
    public class ProfilePageAdapter extends FragmentStatePagerAdapter {

        public ProfilePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return F_COUNT;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case F_PHOTO:
                    fragment = new ProfilePhotoFragment(mLoadingLocker);
                    break;
                case F_FORM:
                    fragment = new ProfileFormFragment();
                    break;
                case F_GIFTS:
                    fragment = new GiftsFragment();
                    break;
            }

            return fragment;
        }
    }
}

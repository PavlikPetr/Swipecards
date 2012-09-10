package com.topface.topface.ui.fragments;

import com.topface.topface.R;
import com.topface.topface.data.FeedGifts;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedGiftsRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.AuthActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.profile.ProfileFormFragment;
import com.topface.topface.ui.profile.ProfilePhotoFragment;
import com.topface.topface.ui.profile.EditProfileActivity;
import com.topface.topface.ui.views.IndicatorView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.Http;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class ProfileFragment extends BaseFragment implements OnClickListener{
    //Data
    private ImageView mUserAvatar;
    private TextView  mUserName;
    private TextView  mUserCity;
    
    private RadioGroup  mUserRadioGroup;
    private RadioButton mUserPhoto;
    private RadioButton mUserForm;
    private RadioButton mUserGifts;
    
    private TextView mUserMoney;
    private TextView mUserPower;
    
    private IndicatorView mIndicatorView;
    private ViewPager mViewPager;
    
    private ProfilePhotoFragment mPhotoFragment;
    private ProfileFormFragment mFormFragment;
   
    public static final int F_PHOTO = 0;
    public static final int F_FORM = 1;
    public static final int F_GIFTS = 2;
    public static final int F_COUNT = F_GIFTS+1;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
		super.onCreateView(inflater, container, saved);
		View view = inflater.inflate(R.layout.ac_profile, null);
		
		// Title
		((TextView)view.findViewById(R.id.tvNavigationTitle)).setText("Title");
		
        // Home Button
        (view.findViewById(R.id.btnNavigationHome)).setOnClickListener((NavigationActivity)getActivity());
        Button editButton = (Button)view.findViewById(R.id.btnNavigationEditBar);
        editButton.setVisibility(View.VISIBLE);
        editButton.setText(getResources().getString(R.string.navigation_edit));
        editButton.setOnClickListener(this);
		
		// Avatar, Name, City
        mUserAvatar = (ImageView)view.findViewById(R.id.ivUserAvatar);
        mUserName = (TextView)view.findViewById(R.id.ivUserName);
        mUserName.setText(CacheProfile.first_name + ", " + CacheProfile.age);
        mUserCity = (TextView)view.findViewById(R.id.ivUserCity);
        mUserCity.setText(CacheProfile.city_name);
        
        // Actions Button
        mUserRadioGroup = (RadioGroup)view.findViewById(R.id.UserRadioGroup);
        mUserPhoto = (RadioButton)view.findViewById(R.id.btnUserPhoto);
        mUserPhoto.setOnClickListener(mInfoClickListener);
        mUserForm = (RadioButton)view.findViewById(R.id.btnUserQuestionnaire);
        mUserForm.setOnClickListener(mInfoClickListener);
        mUserGifts = (RadioButton)view.findViewById(R.id.btnUserGifts);
        mUserGifts.setOnClickListener(mInfoClickListener);
        
        // Resources
        mUserMoney = (TextView)view.findViewById(R.id.tvUserMoney);
        mUserMoney.setText("" + CacheProfile.money);
        mUserPower = (TextView)view.findViewById(R.id.tvUserPower);
        mUserPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.power));
        mUserPower.setText("" + CacheProfile.power + "%");
        
        // View Pager
        mViewPager = (ViewPager)view.findViewById(R.id.UserViewPager);
        mViewPager.setAdapter(new ProfilePageAdapter(getActivity().getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
        
        // Indicator
        mIndicatorView = (IndicatorView)view.findViewById(R.id.viewUserIndicator);
        mIndicatorView.setIndicator(F_PHOTO);
        ViewTreeObserver vto = mIndicatorView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mIndicatorView.setButtonMeasure(R.id.btnUserPhoto, mUserPhoto.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserQuestionnaire, mUserForm.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserGifts, mUserGifts.getMeasuredWidth());
               
                if(mUserPhoto.getMeasuredWidth() > 0) {
                    mIndicatorView.reCompute();
                    ViewTreeObserver obs = mIndicatorView.getViewTreeObserver();
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
		mUserProfileHeader.setBackgroundResource(CacheProfile.background_res_id);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void clearLayout() {
		Debug.log(this, "SettingsActivity::clearLayout");
	}

	@Override
	public void fillLayout() {
		Debug.log(this, "SettingsActivity::fillLayout");
	}

	@Override
	protected void onUpdateStart(boolean isFlyUpdating) {
	}

	@Override
	protected void onUpdateSuccess(boolean isFlyUpdating) {
	}

	@Override
	protected void onUpdateFail(boolean isFlyUpdating) {
	}
	
    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
        @Override
        public void onPageScrolled(int arg0,float arg1,int arg2) {
        }
        @Override
        public void onPageSelected(int arg0) {
            switch (arg0) {
                case F_PHOTO:
                    mIndicatorView.setIndicator(F_PHOTO);
                    ((RadioButton)mUserRadioGroup.getChildAt(F_PHOTO)).setChecked(true);
                    break;
                case F_FORM:
                    mIndicatorView.setIndicator(F_FORM);
                    ((RadioButton)mUserRadioGroup.getChildAt(F_FORM)).setChecked(true);
                    break;
                case F_GIFTS:
                    mIndicatorView.setIndicator(F_GIFTS);
                    ((RadioButton)mUserRadioGroup.getChildAt(F_GIFTS)).setChecked(true);
                    break;
            }
        }
    };
    
    View.OnClickListener mInfoClickListener = new View.OnClickListener() {
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
    
    /*
     *     ProfilePageAdapter
     */
    public class ProfilePageAdapter extends FragmentPagerAdapter {

        public ProfilePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return F_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null; 
            switch (position) {
                case F_PHOTO:
                    fragment = mPhotoFragment = new ProfilePhotoFragment();
                    break;
                case F_FORM:
                    fragment = mFormFragment = new ProfileFormFragment();
                    break;
                case F_GIFTS:
                    fragment = new GiftsFragment();
                    break;
            }
            return fragment;
        }
    }

    @Override
    public void onClick(View v) {
    	switch (v.getId()) {
		case R.id.btnNavigationEditBar:			
			startActivity(new Intent(getActivity().getApplicationContext(),EditProfileActivity.class));
			break;		
		}
    	
    }
}

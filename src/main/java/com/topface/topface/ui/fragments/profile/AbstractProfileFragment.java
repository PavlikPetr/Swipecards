package com.topface.topface.ui.fragments.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.ui.adapters.ProfilePageAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.gift.PlainGiftsFragment;
import com.topface.topface.ui.fragments.gift.UpdatableGiftsFragment;
import com.topface.topface.ui.views.DarkenImageView;
import com.topface.topface.utils.http.ProfileBackgrounds;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;

public abstract class AbstractProfileFragment extends BaseFragment implements ViewPager.OnPageChangeListener {
    public static final String DEFAULT_ACTIVATED_COLOR = "#AAAAAA";
    public static final String DEFAULT_NON_ACTIVATED = "#FFFFFF";
    public static final String INTENT_UID = "intent_profile_uid";
    public static final String INTENT_ITEM_ID = "intent_profile_item_id";
    public static final String INTENT_CALLING_FRAGMENT = "intent_profile_calling_fragment";
    public static final String ADD_PHOTO_INTENT = "com.topface.topface.ADD_PHOTO_INTENT";
    protected static final String ARG_TAG_INIT_BODY_PAGE = "profile_start_body_class";
    protected static final String ARG_TAG_INIT_HEADER_PAGE = "profile_start_header_class";
    protected static final String ARG_TAG_CALLING_CLASS = "intent_profile_calling_fragment";
    protected static final String ARG_FEED_ITEM_ID = "item_id";
    // state
    protected HeaderMainFragment mHeaderMainFragment;
    protected ProfilePageAdapter mHeaderPagerAdapter;
    private UpdatableGiftsFragment mGiftFragment;
    private ArrayList<String> BODY_PAGES_TITLES = new ArrayList<>();
    private ArrayList<String> BODY_PAGES_CLASS_NAMES = new ArrayList<>();
    private ArrayList<String> HEADER_PAGES_CLASS_NAMES = new ArrayList<>();
    private HeaderStatusFragment mHeaderStatusFragment;
    private UserPhotoFragment mUserPhotoFragment;
    private UserFormFragment mUserFormFragment;
    private Profile mProfile = null;
    ProfileInnerUpdater mProfileUpdater = new ProfileInnerUpdater() {
        @Override
        public void update() {
            setProfile(getProfile());
        }

        public void bindFragment(Fragment fragment) {
            if (fragment instanceof HeaderMainFragment) {
                mHeaderMainFragment = (HeaderMainFragment) fragment;
            } else if (fragment instanceof HeaderStatusFragment) {
                mHeaderStatusFragment = (HeaderStatusFragment) fragment;
            } else if (fragment instanceof UserPhotoFragment) {
                mUserPhotoFragment = (UserPhotoFragment) fragment;
            } else if (fragment instanceof UserFormFragment) {
                mUserFormFragment = (UserFormFragment) fragment;
            } else if (fragment instanceof UpdatableGiftsFragment) {
                mGiftFragment = (UpdatableGiftsFragment) fragment;
            }
        }

        public Profile getProfile() {
            return mProfile;
        }

        @Override
        public int getProfileType() {
            return AbstractProfileFragment.this.getProfileType();
        }
    };
    private String mBodyStartPageClassName;
    private String mHeaderStartPageClassName;
    private int mStartBodyPage = 0;
    private int mStartHeaderPage = 0;
    private String mCallingClass;
    // views
    private ViewPager mBodyPager;
    private ProfilePageAdapter mBodyPagerAdapter;
    private ViewPager mHeaderPager;
    private TabPageIndicator mTabIndicator;
    private DarkenImageView mBackgroundView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View root = inflater.inflate(R.layout.fragment_profile, null);
        mBackgroundView = (DarkenImageView) root.findViewById(R.id.profile_background_image);
        initHeaderPages(root);
        initBodyPages(root);
        // start pages initialization
        int startBodyPage = mBodyPagerAdapter.getFragmentIndexByClassName(mBodyStartPageClassName);
        if (startBodyPage != -1) {
            mStartBodyPage = startBodyPage;
        }
        int startHeaderPage = mHeaderPagerAdapter.getFragmentIndexByClassName(mHeaderStartPageClassName);
        if (startHeaderPage != -1) {
            mStartHeaderPage = startHeaderPage;
        }
        mHeaderPager.setCurrentItem(mStartHeaderPage);
        mBodyPager.setCurrentItem(mStartBodyPage);
        return root;
    }

    @Override
    protected void restoreState() {
        mBodyStartPageClassName = getArguments().getString(ARG_TAG_INIT_BODY_PAGE);
        mHeaderStartPageClassName = getArguments().getString(ARG_TAG_INIT_HEADER_PAGE);
        mCallingClass = getArguments().getString(ARG_TAG_CALLING_CLASS);
    }

    protected void onProfileUpdated() {
    }

    @Override
    public void onPause() {
        super.onPause();
        int key;
        Fragment fragment;
        SparseArrayCompat<Fragment> fragments;
        //Вручную прокидываем событие onPause() в ViewPager, т.к. на onPause() мы отписываемся от событий
        if (mBodyPagerAdapter != null) {
            fragments = mBodyPagerAdapter.getFragmentCache();
            for (int i = 0; i < fragments.size(); i++) {
                key = fragments.keyAt(i);
                fragment = fragments.get(key);
                if (fragment != null) {
                    fragment.onPause();
                }
            }
        }

        if (mHeaderPagerAdapter != null) {
            fragments = mHeaderPagerAdapter.getFragmentCache();
            for (int i = 0; i < fragments.size(); i++) {
                key = fragments.keyAt(i);
                fragment = fragments.get(key);
                if (fragment != null) {
                    fragment.onPause();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTabIndicator != null) {
            mTabIndicator.setOnPageChangeListener(null);
            mTabIndicator.removeAllViews();
            mTabIndicator = null;
        }
        mBodyPager = null;
        mHeaderPager = null;
    }

    protected Profile getProfile() {
        return mProfile;
    }

    protected void setProfile(Profile profile) {
        int previousBackground = mProfile != null ? mProfile.background : -1;
        mProfile = profile;
        if (mProfile != null && previousBackground != mProfile.background && mBackgroundView != null) {
            mBackgroundView.setImageResource(
                    ProfileBackgrounds.getBackgroundResource(getActivity(), mProfile.background)
            );
        }
        if (mHeaderMainFragment != null) {
            mHeaderMainFragment.setProfile(profile);
        }
        if (mHeaderStatusFragment != null) {
            mHeaderStatusFragment.setProfile(profile);
        }
        if (mGiftFragment != null) {
            mGiftFragment.setProfile(profile);
        }
        if (mUserPhotoFragment != null && profile instanceof User) {
            mUserPhotoFragment.setUserData((User) profile);
        }
        if (mUserFormFragment != null && profile instanceof User) {
            mUserFormFragment.setUserData((User) profile);
        }
    }

    protected String getCallingClassName() {
        return mCallingClass;
    }

    private void initHeaderPages(View root) {
        addHeaderPage(HeaderMainFragment.class.getName());
        addHeaderPage(HeaderStatusFragment.class.getName());
        mHeaderPager = (ViewPager) root.findViewById(R.id.vpHeaderFragments);
        //Мы отключаем сохранеие state у фрагментов, т.к. мы устанавливаем данные в методе getItem() адаптера,
        //что приводит к пустым фрагментам. Поэтому мы не пытаемся сохранять и восстанавливать состояние фрагмента
        mHeaderPager.setSaveEnabled(false);
        mHeaderPagerAdapter = new ProfilePageAdapter(getChildFragmentManager(),
                HEADER_PAGES_CLASS_NAMES, mProfileUpdater);
        mHeaderPager.setAdapter(mHeaderPagerAdapter);
        //Tabs for header
        CirclePageIndicator circleIndicator = (CirclePageIndicator) root.findViewById(R.id.cpiHeaderTabs);
        circleIndicator.setViewPager(mHeaderPager);
        circleIndicator.setSnap(true);
        mHeaderPagerAdapter.setPageIndicator(circleIndicator);
        circleIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                AbstractProfileFragment.this.onPageScrolled(position, positionOffset, positionOffsetPixels);
                // when positionOffset is near 1.0f ViewPager changes position and sets positionOffset to 0
                if (position <= 0) {
                    mBackgroundView.setDarkenFrameOpacity(positionOffset);
                }
            }

            @Override
            public void onPageSelected(int position) {
                AbstractProfileFragment.this.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                AbstractProfileFragment.this.onPageScrollStateChanged(state);
            }
        });
    }

    private void initBodyPages(View root) {
        initBody();
        mBodyPager = (ViewPager) root.findViewById(R.id.vpFragments);
        mBodyPagerAdapter = new ProfilePageAdapter(getChildFragmentManager(), BODY_PAGES_CLASS_NAMES,
                BODY_PAGES_TITLES, mProfileUpdater);
        mBodyPager.setAdapter(mBodyPagerAdapter);
        //Мы отключаем сохранеие state у фрагментов, т.к. мы устанавливаем данные в методе getItem() адаптера,
        //что приводит к пустым фрагментам. Поэтому мы не пытаемся сохранять и восстанавливать состояние фрагмента
        mBodyPager.setSaveEnabled(false);
        //Tabs for Body
        mTabIndicator = (TabPageIndicator) root.findViewById(R.id.tpiTabs);
        mTabIndicator.setViewPager(mBodyPager);
        mBodyPagerAdapter.setPageIndicator(mTabIndicator);
        mTabIndicator.setOnPageChangeListener(this);
    }

    protected void initBody() {
    }

    private void addHeaderPage(String className) {
        HEADER_PAGES_CLASS_NAMES.add(className);
    }

    protected void addBodyPage(String className, String pageTitle) {
        BODY_PAGES_TITLES.add(pageTitle);
        BODY_PAGES_CLASS_NAMES.add(className);
    }

    @Override
    public void clearContent() {
        if (mHeaderPager != null) {
            mHeaderPager.setCurrentItem(0);
        }
        if (mHeaderMainFragment != null) mHeaderMainFragment.clearContent();
        if (mHeaderStatusFragment != null) mHeaderStatusFragment.clearContent();
        if (mUserPhotoFragment != null) mUserPhotoFragment.clearContent();
        if (mUserFormFragment != null) mUserFormFragment.clearContent();
    }

    public void resultToNestedFragments(int requestCode, int resultCode, Intent data) {
        int key;
        Fragment fragment;
        SparseArrayCompat<Fragment> mBodyFragments = mBodyPagerAdapter.getFragmentCache();
        for (int i = 0; i < mBodyFragments.size(); i++) {
            key = mBodyFragments.keyAt(i);
            fragment = mBodyFragments.get(key);
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected abstract int getProfileType();

    protected void onStartActivity() {
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        onStartActivity();
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    public void startActivity(Intent intent) {
        onStartActivity();
        super.startActivity(intent);
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {
    }

    @Override
    public void onPageSelected(int i) {
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    public interface ProfileInnerUpdater {
        void update();

        void bindFragment(Fragment fragment);

        Profile getProfile();

        int getProfileType();
    }

    protected UpdatableGiftsFragment getGiftFragment() {
        return mGiftFragment;
    }
}

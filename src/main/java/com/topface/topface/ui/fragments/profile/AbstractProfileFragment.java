package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.ui.adapters.ProfilePageAdapter;
import com.topface.topface.ui.fragments.UserAvatarFragment;
import com.topface.topface.ui.fragments.feed.FeedFragment;
import com.topface.topface.ui.fragments.gift.UpdatableGiftsFragment;
import com.topface.topface.ui.views.slidingtab.SlidingTabLayout;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractProfileFragment extends UserAvatarFragment implements ViewPager.OnPageChangeListener {
    public static final String INTENT_UID = "intent_profile_uid";
    public static final String INTENT_ITEM_ID = "intent_profile_item_id";
    public static final String INTENT_CALLING_FRAGMENT = "intent_profile_calling_fragment";
    public static final String INTENT_IS_CHAT_AVAILABLE = "intent_profile_is_chat_available";
    public static final String INTENT_IS_ADD_TO_FAVORITS_AVAILABLE = "intent_profile_is_add_to_favorits_available";
    public static final String INTENT_START_BODY_PAGE_NAME = "intent_start_body_page";
    public static final String ADD_PHOTO_INTENT = "com.topface.topface.ADD_PHOTO_INTENT";
    protected static final String ARG_TAG_INIT_BODY_PAGE = "profile_start_body_class";
    private static final String CURRENT_BODY_PAGE = "CURRENT_BODY_PAGE";
    // state
    private UpdatableGiftsFragment mGiftFragment;
    private ArrayList<String> BODY_PAGES_TITLES = new ArrayList<>();
    private ArrayList<String> BODY_PAGES_CLASS_NAMES = new ArrayList<>();
    private UserPhotoFragment mUserPhotoFragment;
    private UserFormFragment mUserFormFragment;
    private boolean mIsChatAvailable;
    private boolean mIsAddToFavoritsAvailable;
    private Profile mProfile = null;
    ProfileInnerUpdater mProfileUpdater = new ProfileInnerUpdater() {
        @Override
        public void update() {
            setProfile(getProfile());
        }

        public void bindFragment(Fragment fragment) {
            if (fragment instanceof UserPhotoFragment) {
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
    private int mStartBodyPage = 0;
    // views
    private ViewPager mBodyPager;
    private ProfilePageAdapter mBodyPagerAdapter;
    private SlidingTabLayout mTabIndicator;

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            List<Fragment> fragments = getChildFragmentManager().getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment instanceof FeedFragment) {
                        // clean multiselection, when switching tabs
                        ((FeedFragment) fragment).finishMultiSelection();
                    }
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
        mBodyStartPageClassName = args.getString(INTENT_START_BODY_PAGE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View root = inflater.inflate(R.layout.fragment_profile, null);
        initBodyPages(root);
// start pages initialization
        int startBodyPage = mBodyPagerAdapter.getFragmentIndexByClassName(mBodyStartPageClassName);
        if (startBodyPage != -1) {
            mStartBodyPage = startBodyPage;
        }
        mBodyPager.setCurrentItem(mStartBodyPage);
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mBodyPager.setCurrentItem(savedInstanceState.getInt(CURRENT_BODY_PAGE, 0));
        }
    }

    @Override
    protected void restoreState() {
        mBodyStartPageClassName = getArguments().getString(ARG_TAG_INIT_BODY_PAGE);
    }

    protected void onProfileUpdated() {
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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_BODY_PAGE, mBodyPager.getCurrentItem());
    }

    protected Profile getProfile() {
        return mProfile;
    }

    protected void setProfile(Profile profile) {
        mProfile = profile;
        setActionBarAvatar(getUniversalUser());
        refreshActionBarTitles();
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

    protected boolean isChatAvailable() {
        return mIsChatAvailable;
    }

    public void setIsChatAvailable(boolean isChatAvailable) {
        mIsChatAvailable = isChatAvailable;
    }

    protected boolean isAddToFavoriteAvailable() {
        return mIsAddToFavoritsAvailable;
    }

    public void setIsAddToFavoritsAvailable(boolean isAddToFavoritsAvailable) {
        mIsAddToFavoritsAvailable = isAddToFavoritsAvailable;
    }

    private void initBodyPages(View root) {
        initBody();
        mBodyPager = (ViewPager) root.findViewById(R.id.vpFragments);
        mBodyPager.setSaveEnabled(false);
        mBodyPagerAdapter = new ProfilePageAdapter(getChildFragmentManager(), BODY_PAGES_CLASS_NAMES,
                BODY_PAGES_TITLES, mProfileUpdater);
        mBodyPager.setAdapter(mBodyPagerAdapter);
//Tabs for Body
        mTabIndicator = (SlidingTabLayout) root.findViewById(R.id.tpiTabs);
        mTabIndicator.setUseWeightProportions(true);
        mTabIndicator.setCustomTabView(R.layout.tab_indicator, R.id.tab_title);
        mTabIndicator.setViewPager(mBodyPager);
        mTabIndicator.setOnPageChangeListener(mPageChangeListener);
        mTabIndicator.setOnPageChangeListener(this);
    }

    protected void initBody() {
    }

    protected void addBodyPage(String className, String pageTitle) {
        BODY_PAGES_TITLES.add(pageTitle.toUpperCase());
        BODY_PAGES_CLASS_NAMES.add(className);
    }

    @Override
    public void clearContent() {
        if (mUserPhotoFragment != null) mUserPhotoFragment.clearContent();
        if (mUserFormFragment != null) mUserFormFragment.clearContent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.activityResultToNestedFragments(getChildFragmentManager(), requestCode, resultCode, data);
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

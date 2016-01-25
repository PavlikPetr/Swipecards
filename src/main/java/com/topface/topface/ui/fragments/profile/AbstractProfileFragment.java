package com.topface.topface.ui.fragments.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.ui.adapters.ProfilePageAdapter;
import com.topface.topface.ui.dialogs.TrialVipPopup;
import com.topface.topface.ui.fragments.AnimatedFragment;
import com.topface.topface.ui.fragments.SettingsFragment;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.ui.fragments.feed.FeedFragment;
import com.topface.topface.ui.fragments.feed.TabbedFeedFragment;
import com.topface.topface.ui.views.TabLayoutCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.GoogleMarketApiManager;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class AbstractProfileFragment extends AnimatedFragment implements ViewPager.OnPageChangeListener {
    public static final String INTENT_UID = "intent_profile_uid";
    public static final String INTENT_ITEM_ID = "intent_profile_item_id";
    public static final String INTENT_IS_CHAT_AVAILABLE = "intent_profile_is_chat_available";
    public static final String INTENT_IS_ADD_TO_FAVORITS_AVAILABLE = "intent_profile_is_add_to_favorits_available";
    public static final String ADD_PHOTO_INTENT = "com.topface.topface.ADD_PHOTO_INTENT";
    public static final String CURRENT_BODY_PAGE = "CURRENT_BODY_PAGE";
    public static final int DEFAULT_PAGE = 0;

    // state
    private ArrayList<String> BODY_PAGES_TITLES = new ArrayList<>();
    private ArrayList<String> BODY_PAGES_CLASS_NAMES = new ArrayList<>();
    private UserPhotoFragment mUserPhotoFragment;
    private AbstractFormFragment mFormFragment;
    private Profile mProfile = null;
    private TabLayoutCreator mTabLayoutCreator;
    private ProfilePageAdapter mBodyPagerAdapter;
    ProfileInnerUpdater mProfileUpdater = new ProfileInnerUpdater() {
        @Override
        public void update() {
            // load owners profile in OwnProfileFragment only
            if (isOwnersProfileFragment()) {
                setProfile(getProfile());
            }
        }

        public void bindFragment(Fragment fragment) {
            if (fragment instanceof UserPhotoFragment) {
                mUserPhotoFragment = (UserPhotoFragment) fragment;
            } else if (fragment instanceof AbstractFormFragment) {
                mFormFragment = (AbstractFormFragment) fragment;
            }
        }

        public Profile getProfile() {
            return mProfile;
        }
    };
    // views
    private ViewPager mBodyPager;
    @Bind(R.id.profileTabs)
    TabLayout mTabLayout;

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener() {

        private boolean fromVip = false;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (mTabLayoutCreator != null) {
                mTabLayoutCreator.setTabTitle(position);
            }
            List<Fragment> fragments = getChildFragmentManager().getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment instanceof FeedFragment) {
                        // clean multiselection, when switching tabs
                        ((FeedFragment) fragment).finishMultiSelection();
                    }
                }
            }
            if (mBodyPagerAdapter.getItem(position) instanceof VipBuyFragment) {
                fromVip = true;
            }
            if ((mBodyPagerAdapter.getItem(position) instanceof ProfileFormFragment
                    || mBodyPagerAdapter.getItem(position) instanceof SettingsFragment) && fromVip) {
                fromVip = false;
                if (App.isNeedShowTrial && !CacheProfile.getProfile().premium && new GoogleMarketApiManager().isMarketApiAvailable()
                        && CacheProfile.getOptions().trialVipExperiment.enabled && !CacheProfile.paid) {
                    TrialVipPopup.newInstance(true).show(getActivity().getSupportFragmentManager(), TrialVipPopup.TAG);
                    App.isNeedShowTrial = false;
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View root = inflater.inflate(R.layout.fragment_profile, null);
        ButterKnife.bind(this, root);
        initBodyPages(root);
        mTabLayoutCreator = new TabLayoutCreator(getActivity(), mBodyPager, mTabLayout, BODY_PAGES_TITLES, null);
        mTabLayoutCreator.setTabTitle(DEFAULT_PAGE);
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mBodyPager.setCurrentItem(savedInstanceState.getInt(CURRENT_BODY_PAGE, 0));
        }
        Bundle arg = getArguments();
        if (arg != null) {
            String sLastPage = arg.getString(TabbedFeedFragment.EXTRA_OPEN_PAGE);
            if (!TextUtils.isEmpty(sLastPage)) {
                int lastPage = BODY_PAGES_CLASS_NAMES.indexOf(sLastPage);
                mBodyPager.setCurrentItem(lastPage);
            }
        }

    }

    protected void onProfileUpdated() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBodyPager = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
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
        invalidateUniversalUser();
        setActionBarAvatar(getUniversalUser());
        refreshActionBarTitles();
        if (mUserPhotoFragment != null && profile instanceof User) {
            mUserPhotoFragment.setUserData((User) profile);
        }
        if (mFormFragment != null) {
            mFormFragment.setUserData(profile);
        }
    }

    private void initBodyPages(View root) {
        initBody();
        mBodyPager = (ViewPager) root.findViewById(R.id.vpFragments);
        setAnimatedView(mBodyPager);
        mBodyPager.setSaveEnabled(false);
        mBodyPagerAdapter = new ProfilePageAdapter(getChildFragmentManager(), BODY_PAGES_CLASS_NAMES,
                BODY_PAGES_TITLES, mProfileUpdater);
        mBodyPager.setAdapter(mBodyPagerAdapter);
        //Tabs for Body
        mBodyPager.addOnPageChangeListener(mPageChangeListener);
        mTabLayout.setupWithViewPager(mBodyPager);
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
        if (mFormFragment != null) mFormFragment.clearContent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Utils.activityResultToNestedFragments(getChildFragmentManager(), requestCode, resultCode, data);
    }

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
    }

    @Override
    protected boolean isAnimationRequire() {
        return false;
    }

    protected boolean isOwnersProfileFragment() {
        return true;
    }
}

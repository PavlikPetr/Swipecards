package com.topface.topface.ui.fragments;

import android.annotation.TargetApi;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.FixedViewInfo;
import com.topface.topface.data.HeaderFooterData.OnViewClickListener;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.leftMenu.DrawerLayoutStateData;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.IntegrationSettingsData;
import com.topface.topface.data.leftMenu.LeftMenuData;
import com.topface.topface.data.leftMenu.LeftMenuHeaderData;
import com.topface.topface.data.leftMenu.LeftMenuHeaderViewData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.databinding.FragmentMenuBinding;
import com.topface.topface.state.DrawerLayoutState;
import com.topface.topface.state.OptionsAndProfileProvider;
import com.topface.topface.state.SimpleStateDataUpdater;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.adapters.ItemEventListener.OnRecyclerViewItemClickListener;
import com.topface.topface.ui.adapters.LeftMenuRecyclerViewAdapter;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

import static com.topface.topface.ui.adapters.LeftMenuRecyclerViewAdapter.EMPTY_POS;

/**
 * Created by kirussell on 05.11.13.
 * Left menu for switching NavigationActivity fragments
 * extends ListFragment and does not have any xml layout
 */
public class MenuFragment extends Fragment {

    private static final String BALANCE_TEMPLATE = "%s %s %d %s %d";
    private static final String COINS_ICON = "coins_icon";
    private static final String LIKES_ICON = "likes_icon";
    private static final String COUNTERS_DATA = "counters_data";
    private static final String BALANCE_DATA = "balance_data";
    private static final String SELECTED_POSITION = "selected_position";

    @Inject
    TopfaceAppState mAppState;
    @Inject
    NavigationState mNavigationState;
    @Inject
    DrawerLayoutState mDrawerLayoutState;
    private LeftMenuRecyclerViewAdapter mAdapter;
    private boolean mHardwareAccelerated;
    private CountersData mCountersData;
    private BalanceData mBalanceData;
    private CompositeSubscription mSubscription = new CompositeSubscription();
    private int mSelectedPos = EMPTY_POS;

    private Func1<CountersData, CountersData> mCountersMap = new Func1<CountersData, CountersData>() {
        @Override
        public CountersData call(CountersData countersData) {
            countersData.setBonus(CacheProfile.needShowBonusCounter ? App.from(getActivity()).getOptions().bonus.counter : 0);
            return countersData;
        }
    };

    private Action1<CountersData> mCountersOnNext = new Action1<CountersData>() {
        @Override
        public void call(CountersData countersData) {
            mCountersData = countersData;
            updateCounters();
        }
    };

    private Func1<CountersData, Boolean> mCounterFilter = new Func1<CountersData, Boolean>() {
        @Override
        public Boolean call(CountersData countersData) {
            return !mCountersData.equals(countersData);
        }
    };

    private Action1<Throwable> mSubscriptionOnError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            throwable.printStackTrace();
        }
    };

    private Action1<BalanceData> mBalanceOnNext = new Action1<BalanceData>() {
        @Override
        public void call(BalanceData balanceData) {
            mBalanceData = balanceData;
            updateBallance();
        }
    };

    private Func1<BalanceData, Boolean> mBalanceFilter = new Func1<BalanceData, Boolean>() {
        @Override
        public Boolean call(BalanceData balanceData) {
            return mBalanceData.likes == balanceData.likes || mBalanceData.money == balanceData.money;
        }
    };

    private SimpleStateDataUpdater mStateDataUpdater = new SimpleStateDataUpdater() {
        @Override
        public void onOptionsUpdate(Options options) {
            LeftMenuRecyclerViewAdapter adapter = getAdapter();
            adapter.updateTitle(FragmentIdData.BONUS, options.bonus.buttonText);
            adapter.updateIcon(FragmentIdData.BONUS, options.bonus.buttonPicture);
            updateIntegrationPage(options);
        }

        @Override
        public void onProfileUpdate(Profile profile) {
            getAdapter().updateHeader(getHeaderData(profile));
        }
    };

    private OnViewClickListener<LeftMenuHeaderViewData> mOnHeaderClick = new OnViewClickListener<LeftMenuHeaderViewData>() {
        @Override
        public void onClick(View v, LeftMenuHeaderViewData data) {
            setSelected(new LeftMenuSettingsData(FragmentIdData.PROFILE), WrappedNavigationData.SELECTED_BY_CLICK);
        }
    };

    public void setSelected(LeftMenuSettingsData fragmentSettings, @WrappedNavigationData.NavigationEventSenderType int senderType) {
        if (mSelectedPos != EMPTY_POS) {
            getAdapter().updateSelected(mSelectedPos, false);
        }
        mSelectedPos = fragmentSettings.getUniqueKey();
        getAdapter().updateSelected(mSelectedPos, true);
        mNavigationState.leftMenuItemSelected(new WrappedNavigationData(fragmentSettings, senderType));
    }

    private OnRecyclerViewItemClickListener<LeftMenuData> mItemClickListener = new OnRecyclerViewItemClickListener<LeftMenuData>() {
        @Override
        public void itemClick(View view, int itemPosition, LeftMenuData data) {
            setSelected(data.getSettings(), WrappedNavigationData.SELECTED_BY_CLICK);
        }
    };

    private ArrayList<LeftMenuData> getAddedIntegrationItems(ArrayList<LeftMenuData> data) {
        ArrayList<LeftMenuData> arrayList = new ArrayList<>();
        for (LeftMenuData item : data) {
            if (item.getSettings().getFragmentId() == FragmentIdData.INTEGRATION_PAGE) {
                arrayList.add(item);
            }
        }
        return arrayList;
    }

    private void updateIntegrationPage(Options options) {
        ArrayList<LeftMenuData> data = getAdapter().getData();
        ArrayList<LeftMenuData> integrationData = getIntegrationItems(options);
        ArrayList<LeftMenuData> addedIntegrationData = getAddedIntegrationItems(data);
        if (!Utils.isEqualsArrays(integrationData, addedIntegrationData)) {
            data.removeAll(addedIntegrationData);
            getAdapter().addItemsAfterFragment(integrationData, FragmentIdData.GEO);
        }
    }

    private ArrayList<LeftMenuData> getIntegrationItems(Options options) {
        ArrayList<LeftMenuData> arrayList = new ArrayList<>();
        if (options != null && options.leftMenuItems != null && options.leftMenuItems.size() > 0) {
            int pos = 0;
            for (Options.LeftMenuIntegrationItems leftMenuItem : options.leftMenuItems) {
                arrayList.add(new LeftMenuData(leftMenuItem.iconUrl, new SpannableString(leftMenuItem.title), 0, false, new IntegrationSettingsData(FragmentIdData.INTEGRATION_PAGE, pos, leftMenuItem.url, leftMenuItem.external, leftMenuItem.title)));
                pos++;
            }
        }
        return arrayList;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCountersData = savedInstanceState.getParcelable(COUNTERS_DATA);
            mBalanceData = savedInstanceState.getParcelable(BALANCE_DATA);
            mSelectedPos = savedInstanceState.getInt(SELECTED_POSITION, EMPTY_POS);
        }
        mCountersData = mCountersData == null ? new CountersData() : mCountersData;
        mBalanceData = mBalanceData == null ? new BalanceData() : mBalanceData;
        App.from(getActivity()).inject(this);
        mSubscription.add(mAppState.getObservable(BalanceData.class).filter(mBalanceFilter).subscribe(mBalanceOnNext));
        mSubscription.add(mNavigationState.getSwitchObservable().subscribe(new Action1<WrappedNavigationData>() {
            @Override
            public void call(WrappedNavigationData wrappedLeftMenuSettingsData) {
                if (wrappedLeftMenuSettingsData != null && wrappedLeftMenuSettingsData.getSenderType() != WrappedNavigationData.SELECTED_BY_CLICK) {
                    setSelected(wrappedLeftMenuSettingsData.getData(), wrappedLeftMenuSettingsData.getSenderType());
                }
            }
        }, mSubscriptionOnError));
        mSubscription.add(mDrawerLayoutState.getObservable().subscribe(new Action1<DrawerLayoutStateData>() {
            @Override
            public void call(DrawerLayoutStateData drawerLayoutStateData) {

            }
        }, mSubscriptionOnError));
        new OptionsAndProfileProvider(mStateDataUpdater);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHardwareAccelerated = isHardwareAccelerated(view);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean isHardwareAccelerated(View rootLayout) {
        return Build.VERSION.SDK_INT >= 11 && rootLayout.isHardwareAccelerated();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(COUNTERS_DATA, mCountersData);
        outState.putParcelable(BALANCE_DATA, mBalanceData);
        outState.putInt(SELECTED_POSITION, mSelectedPos);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_menu, null);
        FragmentMenuBinding binding = DataBindingUtil.bind(root);
        binding.rvMenu.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvMenu.setAdapter(getAdapter());
        ((SimpleItemAnimator) binding.rvMenu.getItemAnimator()).setSupportsChangeAnimations(false);
        return root;
    }

    private LeftMenuRecyclerViewAdapter initAdapter() {
        LeftMenuRecyclerViewAdapter adapter = new LeftMenuRecyclerViewAdapter(getLeftMenuItems());
        adapter.setOnItemClickListener(mItemClickListener);
        mSubscription.add(mAppState.getObservable(CountersData.class)
                .map(mCountersMap).filter(mCounterFilter)
                .subscribe(mCountersOnNext, mSubscriptionOnError));
        adapter.setHeader(new FixedViewInfo<>(R.layout.left_menu_header, getHeaderData(App.get().getProfile())));
        return adapter;
    }

    private LeftMenuHeaderData getHeaderData(@NotNull Profile profile) {
        return new LeftMenuHeaderData(profile.photo, profile.getNameAndAge(), profile.city != null ? profile.city.getName() : Utils.EMPTY, mOnHeaderClick);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mAdapter = null;
    }

    private void updateCounters() {
        getAdapter().updateCounters(mCountersData);
    }

    private void updateBallance() {
        getAdapter().updateTitle(FragmentIdData.BALLANCE, getBalanceTitle());
    }

    private ArrayList<LeftMenuData> getLeftMenuItems() {
        Options options = App.from(getActivity()).getOptions();
        ArrayList<LeftMenuData> arrayList = new ArrayList<>();
        arrayList.add(new LeftMenuData(R.drawable.ic_photo_left_menu, R.string.general_photoblog, 0, false, new LeftMenuSettingsData(FragmentIdData.PHOTO_BLOG)));
        arrayList.add(new LeftMenuData(R.drawable.ic_dating_left_menu, R.string.general_dating, 0, false, new LeftMenuSettingsData(FragmentIdData.DATING)));
        arrayList.add(new LeftMenuData(R.drawable.ic_like_left_menu, R.string.general_sympathies, mCountersData.getLikes(), false, new LeftMenuSettingsData(FragmentIdData.TABBED_LIKES)));
        arrayList.add(new LeftMenuData(R.drawable.ic_chat_left_menu, R.string.settings_messages, mCountersData.getDialogs(), false, new LeftMenuSettingsData(FragmentIdData.TABBED_DIALOGS)));
        arrayList.add(new LeftMenuData(R.drawable.ic_guests_left_menu, R.string.general_visitors, mCountersData.getVisitors(), false, new LeftMenuSettingsData(FragmentIdData.TABBED_VISITORS)));
        arrayList.add(new LeftMenuData(R.drawable.ic_people_left_menu, R.string.people_nearby, mCountersData.getPeopleNearby(), false, new LeftMenuSettingsData(FragmentIdData.GEO)));
        if (options.bonus.enabled) {
            arrayList.add(new LeftMenuData(R.drawable.ic_bonus_left_menu, App.get().getOptions().bonus.buttonText, mCountersData.getBonus(), false, new LeftMenuSettingsData(FragmentIdData.BONUS)));
        }
        arrayList.add(new LeftMenuData(R.drawable.ic_balance_left_menu, getBalanceTitle(), 0, false, new LeftMenuSettingsData(FragmentIdData.BALLANCE)));
        arrayList.add(new LeftMenuData("", new SpannableString(getString(R.string.editor_menu_admin)), 0, true, new LeftMenuSettingsData(FragmentIdData.EDITOR)));
        return arrayList;
    }

    private LeftMenuRecyclerViewAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = initAdapter();
        }
        return mAdapter;
    }

    private SpannableString getBalanceTitle() {
        String title = String.format(App.getCurrentLocale(), BALANCE_TEMPLATE,
                getString(R.string.purchase_header_title),
                COINS_ICON,
                mBalanceData.money,
                LIKES_ICON,
                mBalanceData.likes);
        SpannableString titleSpan = new SpannableString(title);
        boolean isNewApi = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
        Drawable coins = isNewApi ? getResources().getDrawable(R.drawable.ic_pay, null) : getResources().getDrawable(R.drawable.ic_pay);
        Drawable likes = isNewApi ? getResources().getDrawable(R.drawable.ic_symp, null) : getResources().getDrawable(R.drawable.ic_symp);
        if (coins != null && likes != null) {
            coins.setBounds(0, 0, coins.getIntrinsicWidth(), coins.getIntrinsicHeight());
            likes.setBounds(0, 0, likes.getIntrinsicWidth(), likes.getIntrinsicHeight());
            ImageSpan coinsSpan = new ImageSpan(coins, ImageSpan.ALIGN_BASELINE);
            ImageSpan likesSpan = new ImageSpan(likes, ImageSpan.ALIGN_BASELINE);
            int iconStartPos = title.split(COINS_ICON)[0].length();
            titleSpan.setSpan(coinsSpan, iconStartPos, iconStartPos + COINS_ICON.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            iconStartPos = title.split(LIKES_ICON)[0].length();
            titleSpan.setSpan(likesSpan, iconStartPos, iconStartPos + LIKES_ICON.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return titleSpan;
    }

    public boolean isHrdwareAccelerated() {
        return mHardwareAccelerated;
    }
}

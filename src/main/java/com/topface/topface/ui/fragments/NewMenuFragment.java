package com.topface.topface.ui.fragments;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.FragmentSettings;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.IntegrationSettingsData;
import com.topface.topface.data.leftMenu.LeftMenuData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.databinding.NewFragmentMenuBinding;
import com.topface.topface.state.OptionsAndProfileProvider;
import com.topface.topface.state.SimpleStateDataUpdater;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.adapters.ItemEventListener;
import com.topface.topface.ui.adapters.LeftMenyRecyclerViewAdapter;
import com.topface.topface.utils.CacheProfile;

import java.util.ArrayList;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by kirussell on 05.11.13.
 * Left menu for switching NavigationActivity fragments
 * extends ListFragment and does not have any xml layout
 */
public class NewMenuFragment extends Fragment {

    private static final String BALANCE_TEMPLATE = "%s %s %d %s %d";
    private static final String COINS_ICON = "coins_icon";
    private static final String LIKES_ICON = "likes_icon";
    private static final String COUNTERS_DATA = "counters_data";
    private static final String BALANCE_DATA = "balance_data";

    private static final String TAG = "NewMenuFragment";

    @Inject
    TopfaceAppState mAppState;
    private LeftMenyRecyclerViewAdapter mAdapter;
    private NewFragmentMenuBinding mBinding;
    private CountersData mCountersData;
    private BalanceData mBalanceData;
    private CompositeSubscription mSubscription = new CompositeSubscription();

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
            Debug.showChunkedLogError(TAG, "mCountersOnNext");
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
            Debug.showChunkedLogError(TAG, "mSubscriptionOnError");
        }
    };

    private Action1<BalanceData> mBalanceOnNext = new Action1<BalanceData>() {
        @Override
        public void call(BalanceData balanceData) {
            Debug.showChunkedLogError(TAG, "mBalanceOnNext");
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
            LeftMenyRecyclerViewAdapter adapter = getAdapter();
            adapter.updateTitle(FragmentIdData.BONUS, options.bonus.buttonText);
            adapter.updateIcon(FragmentIdData.BONUS, options.bonus.buttonPicture);
            adapter.addItemsAfterFragment(getIntegrationItems(options), FragmentIdData.GEO);
        }

        @Override
        public void onProfileUpdate(Profile profile) {

        }
    };

    private ArrayList<LeftMenuData> getIntegrationItems(Options options) {
        ArrayList<LeftMenuData> arrayList = new ArrayList<>();
        if (options != null && options.leftMenuItems != null && options.leftMenuItems.size() > 0) {
            int pos = 0;
            for (Options.LeftMenuIntegrationItems leftMenuItem : options.leftMenuItems) {
                arrayList.add(new LeftMenuData(leftMenuItem.iconUrl, new SpannableString(leftMenuItem.title), 0, false, new IntegrationSettingsData(FragmentIdData.INTEGRATION_PAGE, false, pos, leftMenuItem.url, leftMenuItem.external)));
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
        }
        mCountersData = mCountersData == null ? new CountersData() : mCountersData;
        mBalanceData = mBalanceData == null ? new BalanceData() : mBalanceData;
        App.from(getActivity()).inject(this);
        mSubscription.add(mAppState.getObservable(BalanceData.class).filter(mBalanceFilter).subscribe(mBalanceOnNext));
        new OptionsAndProfileProvider(mStateDataUpdater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(COUNTERS_DATA, mCountersData);
        outState.putParcelable(BALANCE_DATA, mBalanceData);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.new_fragment_menu, null);
        mBinding = DataBindingUtil.bind(root);
        mBinding.rvMenu.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.rvMenu.setAdapter(getAdapter());
        return root;
    }

    private LeftMenyRecyclerViewAdapter initAdapter() {
        LeftMenyRecyclerViewAdapter adapter = new LeftMenyRecyclerViewAdapter(getLeftMenuItems(), new ItemEventListener.OnRecyclerViewItemClickListener<LeftMenuData>() {
            @Override
            public void itemClick(View view, int itemPosition, LeftMenuData data) {
                Debug.showChunkedLogError(TAG, "itemClick position " + itemPosition + " type " + data.getSettings().getFragmentId());
            }
        });
        mSubscription.add(mAppState.getObservable(CountersData.class)
                .map(mCountersMap).filter(mCounterFilter)
                .subscribe(mCountersOnNext, mSubscriptionOnError));
        return adapter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
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
        arrayList.add(new LeftMenuData(R.drawable.ic_dating_left_menu, R.string.general_dating, 0, false, new LeftMenuSettingsData(FragmentIdData.DATING, true)));
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

    private LeftMenyRecyclerViewAdapter getAdapter() {
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

    public void setOnFragmentSelected(MenuFragment.OnFragmentSelectedListener listener) {
    }

    public void selectMenu(FragmentSettings fragmentSettings) {

    }

    public void updateAdapter() {

    }
}

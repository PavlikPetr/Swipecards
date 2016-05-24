package com.topface.topface.ui.fragments;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
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
import com.topface.topface.data.HeaderFooterData;
import com.topface.topface.data.HeaderFooterData.OnViewClickListener;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.leftMenu.DrawerLayoutStateData;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.IntegrationSettingsData;
import com.topface.topface.data.leftMenu.LeftMenuData;
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
import com.topface.topface.utils.Editor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

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
    private static final String SELECTED_POSITION = "selected_position";

    @Inject
    TopfaceAppState mAppState;
    @Inject
    NavigationState mNavigationState;
    @Inject
    DrawerLayoutState mDrawerLayoutState;
    private LeftMenuRecyclerViewAdapter mAdapter;
    private CountersData mCountersData;
    private BalanceData mBalanceData;
    private CompositeSubscription mSubscription = new CompositeSubscription();
    private int mSelectedPos = EMPTY_POS;
    private OptionsAndProfileProvider mOptionsAndProfileProvider;

    private Action1<Throwable> mSubscriptionOnError = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            throwable.printStackTrace();
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
            updateEditorItem(profile);
        }
    };

    private OnViewClickListener<LeftMenuHeaderViewData> mOnHeaderClick = new OnViewClickListener<LeftMenuHeaderViewData>() {
        @Override
        public void onClick(View v, LeftMenuHeaderViewData data) {
            setSelected(new WrappedNavigationData(new LeftMenuSettingsData(FragmentIdData.PROFILE),
                    WrappedNavigationData.SELECT_BY_CLICK));
        }
    };

    private void setSelected(WrappedNavigationData data) {
        if (mSelectedPos != EMPTY_POS) {
            getAdapter().updateSelected(mSelectedPos, false);
        }
        mSelectedPos = data.getData().getUniqueKey();
        getAdapter().updateSelected(mSelectedPos, true);
        if (!data.getStatesStack().contains(WrappedNavigationData.SELECT_ONLY)) {
            mNavigationState.emmitNavigationState(data.addStateToStack(WrappedNavigationData.ITEM_SELECTED));
        }
    }

    private OnRecyclerViewItemClickListener<LeftMenuData> mItemClickListener = new OnRecyclerViewItemClickListener<LeftMenuData>() {
        @Override
        public void itemClick(View view, int itemPosition, LeftMenuData data) {
            setSelected(new WrappedNavigationData(data.getSettings(), WrappedNavigationData.SELECT_BY_CLICK));
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
        if (!Arrays.equals(integrationData.toArray(), addedIntegrationData.toArray())) {
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
        App.from(getActivity()).inject(this);
        if (savedInstanceState != null) {
            mSelectedPos = savedInstanceState.getInt(SELECTED_POSITION, EMPTY_POS);
        }
        mCountersData = mCountersData == null ? new CountersData() : mCountersData;
        mBalanceData = mBalanceData == null ? new BalanceData() : mBalanceData;
        mSubscription.add(mAppState
                .getObservable(BalanceData.class)
                .filter(new Func1<BalanceData, Boolean>() {
                    @Override
                    public Boolean call(BalanceData balanceData) {
                        return mBalanceData.likes == balanceData.likes || mBalanceData.money == balanceData.money;
                    }
                })
                .subscribe(new Action1<BalanceData>() {
                    @Override
                    public void call(BalanceData balanceData) {
                        mBalanceData = balanceData;
                        updateBallance();
                    }
                }));
        mSubscription.add(mNavigationState
                .getNavigationObservable()
                .filter(new Func1<WrappedNavigationData, Boolean>() {
                    @Override
                    public Boolean call(WrappedNavigationData data) {
                        return data != null && !data.getStatesStack().contains(WrappedNavigationData.ITEM_SELECTED);
                    }
                })
                .subscribe(new Action1<WrappedNavigationData>() {
                    @Override
                    public void call(WrappedNavigationData wrappedLeftMenuSettingsData) {
                        setSelected(wrappedLeftMenuSettingsData);
                    }
                }, mSubscriptionOnError));
        mSubscription.add(mDrawerLayoutState
                .getObservable()
                .subscribe(new Action1<DrawerLayoutStateData>() {
                    @Override
                    public void call(DrawerLayoutStateData drawerLayoutStateData) {

                    }
                }, mSubscriptionOnError));
        mOptionsAndProfileProvider = new OptionsAndProfileProvider(mStateDataUpdater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                .map(new Func1<CountersData, CountersData>() {
                    @Override
                    public CountersData call(CountersData countersData) {
                        countersData.setBonus(CacheProfile.needShowBonusCounter ? App.from(getActivity()).getOptions().bonus.counter : 0);
                        return countersData;
                    }
                })
                .filter(new Func1<CountersData, Boolean>() {
                    @Override
                    public Boolean call(CountersData countersData) {
                        return !mCountersData.equals(countersData);
                    }
                })
                .subscribe(new Action1<CountersData>() {
                    @Override
                    public void call(CountersData countersData) {
                        mCountersData = countersData;
                        updateCounters();
                    }
                }, mSubscriptionOnError));
        adapter.setHeader(new FixedViewInfo<>(R.layout.left_menu_header, getHeaderData(App.get().getProfile())));
        return adapter;
    }

    private HeaderFooterData<LeftMenuHeaderViewData> getHeaderData(@NotNull Profile profile) {
        String emptyPhotoUrl = Utils.getLocalResUrl((profile.sex == Profile.BOY ?
                R.drawable.feed_banned_male_avatar : R.drawable.feed_banned_female_avatar));
        return new HeaderFooterData<>(new LeftMenuHeaderViewData(profile.photo, emptyPhotoUrl, profile.getNameAndAge(), profile.city != null ? profile.city.getName() : Utils.EMPTY), mOnHeaderClick);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
        mAdapter = null;
        mOptionsAndProfileProvider.unsubscribe();
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
        if (App.get().getProfile().isEditor()) {
            arrayList.add(getEditorItem());
        }
        return arrayList;
    }

    private LeftMenuData getEditorItem() {
        return new LeftMenuData("", new SpannableString(getString(R.string.editor_menu_admin)), 0, true, new LeftMenuSettingsData(FragmentIdData.EDITOR));
    }

    private void updateEditorItem(@NotNull Profile profile) {
        if (profile.isEditor()) {
            getAdapter().updateEditorsItem(getEditorItem());
        } else {
            getAdapter().removeItem(getEditorItem().getSettings().getUniqueKey());
        }
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
}

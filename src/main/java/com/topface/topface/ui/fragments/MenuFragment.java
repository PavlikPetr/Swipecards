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

import com.topface.framework.imageloader.IPhoto;
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
import com.topface.topface.ui.external_libs.ironSource.IronSourceManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.WeakStorage;
import com.topface.topface.utils.rx.RxUtils;
import com.topface.topface.utils.social.AuthToken;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

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
    public static final String ITEM_TAG_TEMPLATE = "left_menu_%d";
    private static final String BALANCE_TEMPLATE = "%s    %s %d   %s %d";
    private static final String COINS_ICON = "coins_icon";
    private static final String LIKES_ICON = "likes_icon";
    private static final String SELECTED_POSITION = "selected_position";
    private static final String BECOME_VIP_BAGE = "!";
    private TopfaceAppState mAppState;
    private NavigationState mNavigationState;
    private DrawerLayoutState mDrawerLayoutState;
    private WeakStorage mWeakStorage;
    private LeftMenuRecyclerViewAdapter mAdapter;
    private CountersData mCountersData;
    private BalanceData mBalanceData;
    private CompositeSubscription mSubscription = new CompositeSubscription();
    private int mSelectedPos = EMPTY_POS;
    private OptionsAndProfileProvider mOptionsAndProfileProvider;
    private int lastOfIntegrationItemsKey = EMPTY_POS;
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
            LeftMenuData data = getBalanceItem();

            // Показ/скрытие итема "Фотолента" по настройке с сервера
            if (options.peopleNearbyRedesignEnabled) {
                adapter.removeItem(getPhotoblogItem().getSettings().getUniqueKey());
            } else {
                LeftMenuData photoblogItem = getPhotoblogItem();
                LeftMenuData becomeVipItem = getBecomeVipItem();
                if (adapter.getDataPositionByFragmentId(photoblogItem.getSettings().getUniqueKey()) == EMPTY_POS) {
                    int position = adapter.getDataPositionByFragmentId(becomeVipItem.getSettings().getUniqueKey());
                    if (position != EMPTY_POS) {
                        // Вставляем итем фотоленты после BecomeVIP если такой элемент нашли в списке
                        adapter.addItemAfterFragment(getPhotoblogItem(), becomeVipItem.getSettings().getUniqueKey());
                    } else {
                        // Вставляем фотоленту в нулевую позицию
                        adapter.addFirst(getPhotoblogItem());
                    }
                }
            }

            // Добавление итема "Баланса", ибо "Баланс" всегда последним быть должен
            if (options.showRefillBalanceInSideMenu) {
                if (adapter.getDataPositionByFragmentId(data.getSettings().getUniqueKey()) == EMPTY_POS) {
                    adapter.addItemAfterFragment(data, lastOfIntegrationItemsKey, FragmentIdData.FB_INVITE_FRIENDS, FragmentIdData.GEO);
                }
            } else {
                if (adapter.getDataPositionByFragmentId(data.getSettings().getUniqueKey()) != EMPTY_POS) {
                    adapter.removeItem(data);
                }
            }

            // Добавление "Бонусного итема"
            data = getBonusItem();
            if (isBonusAvialable(options)) {
                if (adapter.getDataPositionByFragmentId(data.getSettings().getUniqueKey()) == EMPTY_POS) {
                    adapter.addItemAfterFragment(data,
                            lastOfIntegrationItemsKey,
                            FragmentIdData.FB_INVITE_FRIENDS, FragmentIdData.GEO);
                }
            } else {
                if (adapter.getDataPositionByFragmentId(data.getSettings().getUniqueKey()) != EMPTY_POS) {
                    adapter.removeItem(data);
                }
            }

            // Добавление "приглашений фейсбука"
            data = getFbInvitation();
            if (isNeedToAddFBInvitation(options)) {
                if (adapter.getDataPositionByFragmentId(data.getSettings().getUniqueKey()) == EMPTY_POS) {
                    adapter.addItemAfterFragment(data, FragmentIdData.GEO);
                }
            } else {
                if (adapter.getDataPositionByFragmentId(data.getSettings().getUniqueKey()) != EMPTY_POS) {
                    adapter.removeItem(data);
                }
            }

            // Добавление блока "Интеграций"
            updateIntegrationPage(options);

        }

        @Override
        public void onProfileUpdate(Profile profile) {
            getAdapter().updateHeader(getHeaderData(profile));
            updateEditorItem(profile);
            updateBecomeVipItem(profile.premium);
        }
    };

    private Boolean isBonusAvialable(Options options) {
        return (!options.getOfferwallWithPlaces().getLeftMenu().isEmpty() && options.getOfferwallWithPlaces().getName().equalsIgnoreCase(IronSourceManager.TAG));
    }

    private OnViewClickListener<LeftMenuHeaderViewData> mOnHeaderClick = new OnViewClickListener<LeftMenuHeaderViewData>() {
        @Override
        public void onClick(View v, LeftMenuHeaderViewData data) {
            setSelected(new WrappedNavigationData(new LeftMenuSettingsData(FragmentIdData.PROFILE),
                    WrappedNavigationData.SELECT_BY_CLICK));
        }
    };
    private OnRecyclerViewItemClickListener<LeftMenuData> mItemClickListener = new OnRecyclerViewItemClickListener<LeftMenuData>() {
        @Override
        public void itemClick(View view, int itemPosition, LeftMenuData data) {
            setSelected(new WrappedNavigationData(data.getSettings(), WrappedNavigationData.SELECT_BY_CLICK));
        }
    };

    @NotNull
    private ArrayList<LeftMenuData> getAddedIntegrationItems(ArrayList<LeftMenuData> data) {
        ArrayList<LeftMenuData> arrayList = new ArrayList<>();
        for (LeftMenuData item : data) {
            if (item.getSettings().getFragmentId() == FragmentIdData.INTEGRATION_PAGE) {
                arrayList.add(item);
            }
        }
        return arrayList;
    }

    private void setSelected(WrappedNavigationData data) {
        setItemSelected(data);
        mNavigationState.emmitNavigationState(data.addStateToStack(WrappedNavigationData.ITEM_SELECTED));
    }

    private void setItemSelected(WrappedNavigationData data) {
        if (mSelectedPos != EMPTY_POS) {
            getAdapter().updateSelected(mSelectedPos, false);
        }
        mSelectedPos = data.getData().getUniqueKey();
        getAdapter().updateSelected(mSelectedPos, true);
    }

    private void updateIntegrationPage(Options options) {
        ArrayList<LeftMenuData> data = getAdapter().getData();
        ArrayList<LeftMenuData> integrationData = getIntegrationItems(options);
        ArrayList<LeftMenuData> addedIntegrationData = getAddedIntegrationItems(data);
        if (!Arrays.equals(integrationData.toArray(), addedIntegrationData.toArray())) {
            data.removeAll(addedIntegrationData);
            getAdapter().addItemsAfterFragment(integrationData, FragmentIdData.FB_INVITE_FRIENDS, FragmentIdData.GEO);
            lastOfIntegrationItemsKey = integrationData.size() > 0 ? integrationData.get(integrationData.size() - 1).getSettings().getUniqueKey() : EMPTY_POS;
        }
    }

    @NotNull
    private ArrayList<LeftMenuData> getIntegrationItems(Options options) {
        ArrayList<LeftMenuData> arrayList = new ArrayList<>();
        if (options != null && options.leftMenuItems != null && options.leftMenuItems.size() > 0) {
            int pos = 0;
            for (Options.LeftMenuIntegrationItems leftMenuItem : options.leftMenuItems) {
                arrayList.add(new LeftMenuData(leftMenuItem.iconUrl, new SpannableString(leftMenuItem.title), Utils.EMPTY, false, new IntegrationSettingsData(FragmentIdData.INTEGRATION_PAGE, pos, leftMenuItem.url, leftMenuItem.external, leftMenuItem.title)));
                pos++;
            }
        }
        return arrayList;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppState = App.getAppComponent().appState();
        mDrawerLayoutState = App.getAppComponent().drawerLayoutState();
        mNavigationState = App.getAppComponent().navigationState();
        mWeakStorage = App.getAppComponent().weakStorage();

        if (savedInstanceState != null) {
            mSelectedPos = savedInstanceState.getInt(SELECTED_POSITION, EMPTY_POS);
        }
        mCountersData = mCountersData == null ? new CountersData() : mCountersData;
        mBalanceData = mBalanceData == null ? new BalanceData() : mBalanceData;
        mAdapter = initAdapter();
        mAdapter.updateSelected(mSelectedPos, false);
        mSubscription.add(mAppState.getObservable(CountersData.class)
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
        mSubscription.add(mAppState
                .getObservable(BalanceData.class)
                .filter(new Func1<BalanceData, Boolean>() {
                    @Override
                    public Boolean call(BalanceData balanceData) {
                        return mBalanceData.likes != balanceData.likes
                                || mBalanceData.money != balanceData.money
                                || mBalanceData.premium != balanceData.premium;
                    }
                })
                .subscribe(new RxUtils.ShortSubscription<BalanceData>() {
                    @Override
                    public void onNext(BalanceData balanceData) {
                        super.onNext(balanceData);
                        mBalanceData = balanceData;
                        updateBalance();
                        updateBecomeVipItem(balanceData.premium);
                    }
                }));
        mSubscription.add(mNavigationState
                .getNavigationObservable()
                .filter(new Func1<WrappedNavigationData, Boolean>() {
                    @Override
                    public Boolean call(WrappedNavigationData data) {
                        return data != null;
                    }
                })
                .subscribe(new Action1<WrappedNavigationData>() {
                    @Override
                    public void call(WrappedNavigationData data) {
                        ArrayList<Integer> stack = data.getStatesStack();
                        if (stack.contains(WrappedNavigationData.ITEM_SELECTED)
                                || stack.contains(WrappedNavigationData.SELECT_ONLY)) {
                            setItemSelected(data);
                        } else {
                            setSelected(data);
                        }
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

    @NotNull
    private LeftMenuRecyclerViewAdapter initAdapter() {
        LeftMenuRecyclerViewAdapter adapter = new LeftMenuRecyclerViewAdapter(getLeftMenuItems());
        adapter.setOnItemClickListener(mItemClickListener);
        adapter.setHeader(new FixedViewInfo<>(R.layout.left_menu_header, getHeaderData(App.get().getProfile())));
        return adapter;
    }

    @NotNull
    private HeaderFooterData<LeftMenuHeaderViewData> getHeaderData(@NotNull Profile profile) {
        return new HeaderFooterData<>(new LeftMenuHeaderViewData(
                getValidatedUserPhotoInterface(profile),
                profile.firstName,
                profile.age,
                profile.city != null ? profile.city.getName() : Utils.EMPTY,
                mWeakStorage.getIsTranslucentDating()
        ), mOnHeaderClick);
    }

    @NotNull
    private IPhoto getValidatedUserPhotoInterface(@NotNull Profile profile) {
        if (profile.photo != null && !profile.photo.isFake()) {
            return profile.photo;
        }
        return Utils.getUserPhotoGag(Utils.getLocalResUrl((profile.sex == Profile.BOY ?
                R.drawable.feed_banned_male_avatar : R.drawable.feed_banned_female_avatar)));
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

    private void updateBalance() {
        getAdapter().updateTitle(FragmentIdData.BALLANCE, getBalanceTitle());
    }

    @NotNull
    private ArrayList<LeftMenuData> getLeftMenuItems() {
        Options options = App.get().getOptions();
        ArrayList<LeftMenuData> arrayList = new ArrayList<>();
        if (!App.get().getProfile().premium) {
            arrayList.add(getBecomeVipItem());
        }
        // фотоленту показываем только со старым экранаом "Люди рядом" (без фотоленты в "шапке")
        if (!options.peopleNearbyRedesignEnabled) {
            arrayList.add(getPhotoblogItem());
        }
        arrayList.add(new LeftMenuData(R.drawable.ic_dating_left_menu, R.string.general_dating,
                Utils.EMPTY, false, new LeftMenuSettingsData(FragmentIdData.DATING)));
        arrayList.add(new LeftMenuData(R.drawable.ic_like_left_menu, R.string.general_sympathies,
                String.valueOf(mCountersData.getLikes()), false, new LeftMenuSettingsData(FragmentIdData.TABBED_LIKES)));
        arrayList.add(new LeftMenuData(R.drawable.ic_chat_left_menu, R.string.settings_messages,
                String.valueOf(mCountersData.getDialogs()), false, new LeftMenuSettingsData(FragmentIdData.TABBED_DIALOGS)));
        arrayList.add(new LeftMenuData(R.drawable.ic_guests_left_menu, R.string.general_visitors,
                String.valueOf(mCountersData.getVisitors()), false, new LeftMenuSettingsData(FragmentIdData.TABBED_VISITORS)));
        arrayList.add(new LeftMenuData(R.drawable.ic_people_left_menu, R.string.people_nearby,
                String.valueOf(mCountersData.getPeopleNearby()), false, new LeftMenuSettingsData(FragmentIdData.GEO)));

        // Если авторизован и с сервера пришла необходимость, то показываем пункт меню "Пригласи друга"
        if (isNeedToAddFBInvitation(options)) {
            arrayList.add(getFbInvitation());
        }
        //  Item "Бонус"
        if (isBonusAvialable(options)) {
            arrayList.add(getBonusItem());
        }
        // Item "Баланс"
        if (options.showRefillBalanceInSideMenu) {
            arrayList.add(getBalanceItem());
        }
        // Item "Админка"
        if (App.get().getProfile().isEditor()) {
            arrayList.add(getEditorItem());
        }
        return arrayList;
    }

    @NotNull
    private LeftMenuData getBecomeVipItem() {
        return new LeftMenuData(R.drawable.ic_crown_left_menu, getString(R.string.chat_auto_reply_button),
                BECOME_VIP_BAGE, false, new LeftMenuSettingsData(FragmentIdData.BECOME_VIP));
    }

    @NotNull
    private LeftMenuData getPhotoblogItem() {
        return new LeftMenuData(R.drawable.ic_photo_left_menu, R.string.general_photoblog,
                Utils.EMPTY, false, new LeftMenuSettingsData(FragmentIdData.PHOTO_BLOG));
    }

    @NotNull
    private LeftMenuData getBalanceItem() {
        return new LeftMenuData(R.drawable.ic_balance_left_menu, getBalanceTitle(), Utils.EMPTY, false,
                new LeftMenuSettingsData(FragmentIdData.BALLANCE));
    }

    // пункт меню "Пригласить друзей"
    @NotNull
    private LeftMenuData getFbInvitation() {
        return new LeftMenuData(R.drawable.ic_invite, R.string.fb_invite_friends_menu_item,
                Utils.EMPTY, false, new LeftMenuSettingsData(FragmentIdData.FB_INVITE_FRIENDS));
    }

    @NotNull
    private LeftMenuData getBonusItem() {
        return new LeftMenuData(R.drawable.ic_bonus_left_menu, App.getContext().getString(R.string.general_bonus),
                Utils.EMPTY, false, new LeftMenuSettingsData(FragmentIdData.BONUS));
    }

    @NotNull
    private LeftMenuData getEditorItem() {
        return new LeftMenuData("", new SpannableString(getString(R.string.editor_menu_admin)), Utils.EMPTY,
                true, new LeftMenuSettingsData(FragmentIdData.EDITOR));
    }

    private void updateEditorItem(@NotNull Profile profile) {
        LeftMenuData data = getEditorItem();
        if (profile.isEditor()) {
            if (getAdapter().getDataPositionByFragmentId(data.getSettings().getUniqueKey()) == EMPTY_POS) {
                getAdapter().updateEditorsItem(data);
            }
        } else {
            if (getAdapter().getDataPositionByFragmentId(data.getSettings().getUniqueKey()) != EMPTY_POS) {
                getAdapter().removeItem(data);
            }
        }
    }

    private void updateBecomeVipItem(boolean isPremium) {
        LeftMenuData data = getBecomeVipItem();
        if (!isPremium) {
            int pos = getAdapter().getDataPositionByFragmentId(data.getSettings().getUniqueKey());
            if (pos == EMPTY_POS) {
                getAdapter().addFirst(data);
            }
        } else {
            if (getAdapter().getDataPositionByFragmentId(data.getSettings().getUniqueKey()) != EMPTY_POS) {
                getAdapter().removeItem(data);
            }
        }
    }

    private Boolean isNeedToAddFBInvitation(Options options) {
        return !options.fbInviteSettings.isEmpty() &&
                options.fbInviteSettings.getEnabled() &&
                AuthToken.getInstance().getSocialNet().equals(AuthToken.SN_FACEBOOK);
    }

    @NotNull
    private LeftMenuRecyclerViewAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = initAdapter();
        }
        return mAdapter;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("deprecation")
    @NotNull
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

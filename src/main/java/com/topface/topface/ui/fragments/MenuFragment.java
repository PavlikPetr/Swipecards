package com.topface.topface.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.topface.IllustratedTextView.IllustratedTextView;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.Photo;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.INavigationFragmentsListener;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.LeftMenuAdapter;
import com.topface.topface.ui.fragments.feed.PeopleNearbyFragment;
import com.topface.topface.ui.fragments.feed.PhotoBlogFragment;
import com.topface.topface.ui.fragments.feed.TabbedDialogsFragment;
import com.topface.topface.ui.fragments.feed.TabbedLikesFragment;
import com.topface.topface.ui.fragments.feed.TabbedVisitorsFragment;
import com.topface.topface.ui.fragments.profile.OwnProfileFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.ResourcesUtils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.offerwalls.OfferwallsManager;
import com.topface.topface.utils.social.AuthToken;

import java.io.Serializable;
import java.util.Arrays;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.BONUS;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.DATING;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.GEO;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.PHOTO_BLOG;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.PROFILE;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.TABBED_DIALOGS;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.TABBED_LIKES;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.TABBED_VISITORS;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.UNDEFINED;

/**
 * Created by kirussell on 05.11.13.
 * Left menu for switching NavigationActivity fragments
 * extends ListFragment and does not have any xml layout
 */
public class MenuFragment extends Fragment {
    public static final String SELECT_MENU_ITEM = "com.topface.topface.action.menu.selectitem";
    public static final String SELECTED_FRAGMENT_ID = "com.topface.topface.action.menu.item";
    private static final String CURRENT_FRAGMENT_STATE = "menu_fragment_current_fragment";

    @Inject
    TopfaceAppState mAppState;
    private OnFragmentSelectedListener mOnFragmentSelected;
    private FragmentId mSelectedFragment = UNDEFINED;
    private LeftMenuAdapter mAdapter;
    private boolean mHardwareAccelerated;
    private View mEditorItem;
    private IllustratedTextView textBalance;
    private BalanceData mBalanceData;
    private Action1<BalanceData> mBalanceAction = new Action1<BalanceData>() {
        @Override
        public void call(BalanceData balanceData) {
            mBalanceData = balanceData;
            updateBalance(balanceData);
        }
    };
    private Subscription mBalanceSubscription;
    private Subscription mCountersSubscription;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case CacheProfile.PROFILE_UPDATE_ACTION:
                    initProfileMenuItem(mProfileMenuItem);
                    initEditor();
                    initBonus();
                    break;
                case SELECT_MENU_ITEM:
                    Bundle extras = intent.getExtras();
                    FragmentId fragmentId = null;
                    if (extras != null) {
                        Serializable menuItem = extras.getSerializable(SELECTED_FRAGMENT_ID);
                        /*
                        After update user can have outdated fragment id in instance state. Here we check
                        if it is still presented in BaseFragment.FragmentId enum.
                         */
                        if (Arrays.asList(FragmentId.values()).contains(menuItem)) {
                            fragmentId = (FragmentId) menuItem;
                        } else {
                            fragmentId = CacheProfile.getOptions().startPageFragmentId;
                        }
                    }
                    selectMenu(fragmentId);
                    break;
            }
        }
    };

    private INavigationFragmentsListener mFragmentSwitchListener;
    private ListView mListView;
    private LeftMenuAdapter.ILeftMenuItem mProfileMenuItem;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof INavigationFragmentsListener) {
            mFragmentSwitchListener = (INavigationFragmentsListener) activity;
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentSwitchListener = null;
    }

    public static void selectFragment(FragmentId fragmentId) {
        Intent intent = new Intent();
        intent.setAction(SELECT_MENU_ITEM);
        intent.putExtra(SELECTED_FRAGMENT_ID, fragmentId);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
    }

    private void initBonus() {
        if (CacheProfile.getOptions().bonus.enabled && !mAdapter.hasFragment(BONUS)) {
            mAdapter.addItem(LeftMenuAdapter.newLeftMenuItem(BONUS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE, R.drawable.ic_bonus_selector));
            mAdapter.refreshCounterBadges();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Показываем фрагмент только если мы авторизованы
        if (!AuthToken.getInstance().isEmpty()) {
            if (savedInstanceState != null) {
                FragmentId savedId = (FragmentId) savedInstanceState.getSerializable(CURRENT_FRAGMENT_STATE);
                if (savedId != null) {
                    Debug.log(NavigationActivity.PAGE_SWITCH + "Switch fragment from saved instance state.");
                    switchFragment(savedId, false);
                    return;
                }
            }
            if (getActivity() != null) {
                Intent intent = getActivity().getIntent();
                if (intent != null &&
                        intent.getSerializableExtra(GCMUtils.NEXT_INTENT) != null) {
                    Debug.log(NavigationActivity.PAGE_SWITCH + "Switch fragment from activity intent.");
                    switchFragment((FragmentId) intent.getSerializableExtra(GCMUtils.NEXT_INTENT), false);
                    return;
                }
            }
            Debug.log(NavigationActivity.PAGE_SWITCH + "Switch fragment to default from onCreate().");
            switchFragment(CacheProfile.getOptions().startPageFragmentId, false);
        }
    }

    private void initEditor() {
        if (mListView != null) {
            if (Editor.isEditor()) {
                if (mEditorItem == null) {
                    mEditorItem = View.inflate(getActivity(), R.layout.item_left_menu_button_with_badge, null);
                    TextView btnMenu = (TextView) mEditorItem.findViewById(R.id.btnMenu);
                    //noinspection ResourceType
                    btnMenu.setText(ResourcesUtils.getFragmentNameResId(FragmentId.EDITOR));
                    mEditorItem.setTag(FragmentId.EDITOR);
                    mEditorItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onMenuSelected(FragmentId.EDITOR);
                        }
                    });
                    mListView.addFooterView(mEditorItem);
                }
            } else {
                if (mEditorItem != null) {
                    mListView.removeFooterView(mEditorItem);
                    mEditorItem.setOnClickListener(null);
                    mEditorItem = null;
                }
            }
        }
    }

    public void updateAdapter() {
        initAdapter();
    }

    private void initAdapter() {
        SparseArray<LeftMenuAdapter.ILeftMenuItem> menuItems = new SparseArray<>();
        mProfileMenuItem = LeftMenuAdapter.newLeftMenuItem(PROFILE, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_PHOTO,
                CacheProfile.getProfile().photo);
        menuItems.put(PROFILE.getId(), mProfileMenuItem);
        menuItems.put(DATING.getId(), LeftMenuAdapter.newLeftMenuItem(DATING, LeftMenuAdapter.TYPE_MENU_BUTTON,
                R.drawable.ic_dating_selector));
        menuItems.put(TABBED_DIALOGS.getId(), LeftMenuAdapter.newLeftMenuItem(TABBED_DIALOGS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_dialog_selector));
        menuItems.put(PHOTO_BLOG.getId(), LeftMenuAdapter.newLeftMenuItem(PHOTO_BLOG, LeftMenuAdapter.TYPE_MENU_BUTTON,
                R.drawable.ic_photolenta_selector));
        menuItems.put(TABBED_VISITORS.getId(), LeftMenuAdapter.newLeftMenuItem(TABBED_VISITORS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_guests_selector));
        menuItems.put(TABBED_LIKES.getId(), LeftMenuAdapter.newLeftMenuItem(TABBED_LIKES, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_likes_selector));
        menuItems.put(GEO.getId(), LeftMenuAdapter.newLeftMenuItem(GEO, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.icon_people_close_selector));
        if (CacheProfile.getOptions().bonus.enabled) {
            menuItems.put(BONUS.getId(), LeftMenuAdapter.newLeftMenuItem(BONUS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                    R.drawable.ic_bonus_selector));
        }
        if (mAdapter == null) {
            mAdapter = new LeftMenuAdapter(menuItems);
            mListView.setAdapter(mAdapter);
            mCountersSubscription = mAppState.getObservable(CountersData.class).subscribe(new Action1<CountersData>() {
                @Override
                public void call(CountersData countersData) {
                    if (countersData.isNotEmpty()) {
                        mAdapter.updateCountersBadge(countersData);
                    }
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_menu, null);
        mListView = (ListView) root.findViewById(R.id.lvMenu);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            View lastActivated;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < mAdapter.getCount()) {
                    onMenuSelected(mAdapter.getItem(position).getMenuId());
                    if (lastActivated != null) {
                        lastActivated.setActivated(false);
                    }
                    view.setActivated(true);
                    lastActivated = view;
                } else {
                    onBalanceSelected();
                }
            }
        });
        initAdapter();
        initFooter();
        return root;
    }

    private void onBalanceSelected() {
        startActivity(PurchasesActivity.createBuyingIntent("Menu"));
    }

    private void initFooter() {
        ViewGroup footer = (ViewGroup) View.inflate(getActivity(), R.layout.layout_left_menu_footer, null);
        mListView.addFooterView(footer);
        textBalance = (IllustratedTextView) footer.findViewById(R.id.btnMenu);
        updateBalance(mBalanceData);
        initEditor();
    }

    private void updateBalance(BalanceData balanceData) {
        if (textBalance != null && balanceData != null) {
            textBalance.setText(String.format(getString(R.string.balance), balanceData.money, balanceData.likes));
        }
    }

    private void initProfileMenuItem(LeftMenuAdapter.ILeftMenuItem profileMenuItem) {
        boolean notify = false;
        if (profileMenuItem != null) {
            // update photo
            Photo photo = profileMenuItem.getMenuIconPhoto();
            if (photo != null) {
                if (photo.equals(CacheProfile.getProfile().photo)) {
                    profileMenuItem.setMenuIconPhoto(CacheProfile.getProfile().photo);
                    notify = true;
                }
            }
            // fill data warning icon
            int res = 0;
            if (!CacheProfile.isDataFilled() && CacheProfile.isLoaded()) {
                res = R.drawable.ic_not_enough_data;
            }
            if (res != profileMenuItem.getExtraIconDrawable()) {
                profileMenuItem.setExtraIconDrawable(res);
                notify = true;
            }
            // notify if something changed
            if (notify) {
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(getActivity()).inject(this);
        mBalanceSubscription = mAppState.getObservable(BalanceData.class).subscribe(mBalanceAction);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBalanceSubscription.unsubscribe();
        mCountersSubscription.unsubscribe();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(
                CURRENT_FRAGMENT_STATE,
                getCurrentFragmentId()
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CacheProfile.PROFILE_UPDATE_ACTION);
        filter.addAction(SELECT_MENU_ITEM);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, filter);
        initProfileMenuItem(mProfileMenuItem);
        updateBalance(mBalanceData);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateReceiver);
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

    /**
     * Selects menu item and shows fragment by id
     * Note: incorrect behavior, when method is called in onCreate
     * (Exception: recursive call of executePendingTransactions)
     *
     * @param fragmentId id of fragment that is going to be shown
     */
    public void selectMenu(FragmentId fragmentId) {
        if (fragmentId != mSelectedFragment) {
            Debug.log("MenuFragment: Switch fragment in selectMenu().");
            switchFragment(fragmentId, true);
        } else if (mOnFragmentSelected != null) {
            mOnFragmentSelected.onFragmentSelected(fragmentId);
        }
        notifyDataSetChanged();
    }

    private void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Shows fragment by id
     *
     * @param newFragmentId id of fragment that is going to be shown
     */
    private void switchFragment(FragmentId newFragmentId, boolean executePending) {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment oldFragment = fragmentManager.findFragmentById(R.id.fragment_content);
        String fragmentTag = getTagById(newFragmentId);
        Debug.log("MenuFragment: Try switch to fragment with tag " + fragmentTag + " (old fragment " + mSelectedFragment + ")");
        BaseFragment newFragment = (BaseFragment) fragmentManager.findFragmentByTag(fragmentTag);

        //Если не нашли в FragmentManager уже существующего инстанса, то создаем новый
        if (newFragment == null) {
            newFragment = getFragmentNewInstanceById(newFragmentId);
            Debug.log("MenuFragment: newFragment is null, create new instance");
        }

        if (oldFragment == null || newFragmentId != mSelectedFragment) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            //Меняем фрагменты анимировано, но только на новых устройствах c HW ускорением
            if (mHardwareAccelerated) {
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            if (oldFragment != newFragment && newFragment.isAdded()) {
                transaction.remove(newFragment);
                Debug.error("MenuFragment: try detach already added new fragment " + fragmentTag);
            }
            transaction.replace(R.id.fragment_content, newFragment, fragmentTag);
            transaction.commitAllowingStateLoss();
            //Вызываем executePendingTransactions, если передан соответвующий флаг
            //и сохраняем результат
            String transactionResult = executePending ?
                    Boolean.toString(fragmentManager.executePendingTransactions()) :
                    "no executePending";
            mSelectedFragment = newFragmentId;
            Debug.log("MenuFragment: commit " + transactionResult);
        } else {
            Debug.error("MenuFragment: new fragment already added");
        }
        mSelectedFragment = newFragmentId;

        if (mFragmentSwitchListener != null) {
            mFragmentSwitchListener.onFragmentSwitch(mSelectedFragment);
        }
        //Закрываем меню только после создания фрагмента
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mOnFragmentSelected != null) {
                    mOnFragmentSelected.onFragmentSelected(mSelectedFragment);
                }
            }
        }, 250);
    }

    private String getTagById(FragmentId id) {
        return "fragment_switch_controller_" + id;
    }

    public FragmentId getCurrentFragmentId() {
        return mSelectedFragment == UNDEFINED ? CacheProfile.getOptions().startPageFragmentId : mSelectedFragment;
    }

    private BaseFragment getFragmentNewInstanceById(FragmentId id) {
        BaseFragment fragment;
        switch (id) {
            case VIP_PROFILE:
            case PROFILE:
                fragment = OwnProfileFragment.newInstance();
                break;
            case DATING:
                fragment = new DatingFragment();
                break;
            case GEO:
                fragment = new PeopleNearbyFragment();
                break;
            case BONUS:
                fragment = BonusFragment.newInstance(true);
                break;
            case TABBED_VISITORS:
                fragment = new TabbedVisitorsFragment();
                break;
            case SETTINGS:
                fragment = new SettingsFragment();
                break;
            case EDITOR:
                fragment = null;
                if (Editor.isEditor()) {
                    fragment = new EditorFragment();
                }
                break;
            case PHOTO_BLOG:
                fragment = new PhotoBlogFragment();
                break;
            case TABBED_LIKES:
                fragment = new TabbedLikesFragment();
                break;
            case TABBED_DIALOGS:
                fragment = new TabbedDialogsFragment();
                break;
            default:
                fragment = OwnProfileFragment.newInstance();
                break;
        }
        return fragment;
    }

    public void onMenuSelected(FragmentId id) {
        if (mListView.isClickable()) {
            //Тут сложная работа счетчика, которая отличается от стандартной логики. Мы контроллируем
            //его локально, а не серверно, как это происходит с остальными счетчиками.
            if (id == BONUS) {
                if (CacheProfile.needShowBonusCounter) {
                    UserConfig config = App.getUserConfig();
                    config.setBonusCounterLastShowTime(CacheProfile.getOptions().bonus.timestamp);
                    config.saveConfig();
                }
                CacheProfile.needShowBonusCounter = false;
                mAdapter.refreshCounterBadges();
                if (!TextUtils.isEmpty(CacheProfile.getOptions().bonus.integrationUrl) ||
                        CacheProfile.getOptions().offerwalls.hasOffers()
                        ) {
                    selectMenu(BONUS);
                } else {
                    OfferwallsManager.startOfferwall(getActivity());
                }
            } else {
                selectMenu(id);
            }
        }
    }

    public void setOnFragmentSelected(OnFragmentSelectedListener listener) {
        mOnFragmentSelected = listener;
    }

    public void setClickable(boolean clickable) {
        mListView.setClickable(clickable);
    }

    public interface OnFragmentSelectedListener {
        void onFragmentSelected(FragmentId fragmentId);
    }
}

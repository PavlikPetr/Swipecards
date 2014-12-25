package com.topface.topface.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.data.Products;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.INavigationFragmentsListener;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.LeftMenuAdapter;
import com.topface.topface.ui.dialogs.ClosingsBuyVipDialog;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.ui.fragments.closing.LikesClosingFragment;
import com.topface.topface.ui.fragments.closing.MutualClosingFragment;
import com.topface.topface.ui.fragments.feed.AdmirationFragment;
import com.topface.topface.ui.fragments.feed.BookmarksFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.FansFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.PeopleNearbyFragment;
import com.topface.topface.ui.fragments.feed.TabbedDialogsFragment;
import com.topface.topface.ui.fragments.feed.TabbedLikesFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.ui.fragments.profile.OwnProfileFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.BuyWidgetController;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.ResourcesUtils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.controllers.ClosingsController;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.http.ProfileBackgrounds;
import com.topface.topface.utils.offerwalls.OfferwallsManager;
import com.topface.topface.utils.social.AuthToken;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.ADMIRATIONS;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.BONUS;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.BOOKMARKS;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.DATING;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.DIALOGS;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.FANS;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.GEO;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.LIKES;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.MUTUAL;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.PROFILE;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.TABBED_DIALOGS;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.TABBED_LIKES;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.UNDEFINED;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.VIP_PROFILE;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.VISITORS;

/**
 * Created by kirussell on 05.11.13.
 * Left menu for switching NavigationActivity fragments
 * extends ListFragment and does not have any xml layout
 */
public class MenuFragment extends Fragment implements View.OnClickListener {
    public static final String SELECT_MENU_ITEM = "com.topface.topface.action.menu.selectitem";
    public static final String SELECTED_FRAGMENT_ID = "com.topface.topface.action.menu.item";
    private static final String CURRENT_FRAGMENT_STATE = "menu_fragment_current_fragment";

    private OnFragmentSelectedListener mOnFragmentSelected;
    private FragmentId mSelectedFragment = UNDEFINED;
    private LeftMenuAdapter mAdapter;
    private boolean mHardwareAccelerated;
    private View mHeaderView;
    private TextView mProfileButton;
    private BuyWidgetController mBuyWidgetController;
    private ViewStub mHeaderViewStub;
    private ViewGroup mFooterView;
    private View mEditorItem;

    private ClosingsController mClosingsController;

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case CountersManager.UPDATE_BALANCE:
                    mAdapter.refreshCounterBadges();
                    mBuyWidgetController.updateBalance();
                    if (mClosingsController != null) {
                        mClosingsController.refreshCounterBadges();
                    }
                    break;
                case CacheProfile.PROFILE_UPDATE_ACTION:
                    initProfileMenuItem(mHeaderView);
                    initEditor();
                    initBonus();
                    if (CacheProfile.premium) {
                        mClosingsController.onPremiumObtained();
                    }
                    break;
                case Products.INTENT_UPDATE_PRODUCTS:
                    Products products = CacheProfile.getMarketProducts();
                    if (products != null && mBuyWidgetController != null) {
                        mBuyWidgetController.setSalesEnabled(products.saleExists);
                    }
                    break;
                case SELECT_MENU_ITEM:
                    Bundle extras = intent.getExtras();
                    FragmentId fragmentId = null;
                    if (extras != null) {
                        fragmentId = (FragmentId) extras.getSerializable(SELECTED_FRAGMENT_ID);
                    }
                    selectMenu(fragmentId);
                    break;
                case LikesClosingFragment.ACTION_LIKES_CLOSINGS_PROCESSED:
                    mClosingsController.onClosingsProcessed(FeedRequest.FeedService.LIKES);

                    break;
                case MutualClosingFragment.ACTION_MUTUAL_CLOSINGS_PROCESSED:
                    mClosingsController.onClosingsProcessed(FeedRequest.FeedService.MUTUAL);
                    break;
                case Options.Closing.DATA_FOR_CLOSING_RECEIVED_ACTION:
                    if (!CacheProfile.premium) {
                        mClosingsController.show();
                    }
                    break;
            }
        }
    };
    private INavigationFragmentsListener mFragmentSwitchListener;
    private ListView mListView;

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

    public INavigationFragmentsListener getNavigationFragmentsListener() {
        return mFragmentSwitchListener;
    }

    public static void selectFragment(FragmentId fragmentId) {
        Intent intent = new Intent();
        intent.setAction(SELECT_MENU_ITEM);
        intent.putExtra(SELECTED_FRAGMENT_ID, fragmentId);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
    }

    public static void onLogout() {
        ClosingsController.onLogout();
    }

    private void initBonus() {
        if (CacheProfile.getOptions().bonus.enabled && !mAdapter.hasFragment(BONUS)) {
            mAdapter.addItem(LeftMenuAdapter.newLeftMenuItem(BONUS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE, R.drawable.ic_bonus_1));
            mAdapter.refreshCounterBadges();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void initEditor() {
        if (mFooterView != null) {
            if (Editor.isEditor()) {
                if (mEditorItem == null) {
                    mEditorItem = View.inflate(getActivity(), R.layout.item_left_menu_button_with_badge, null);
                    TextView btnMenu = (TextView) mEditorItem.findViewById(R.id.btnMenu);
                    //noinspection ResourceType
                    btnMenu.setText(ResourcesUtils.getFragmentNameResId(FragmentId.EDITOR));
                    btnMenu.setTag(FragmentId.EDITOR);
                    btnMenu.setOnClickListener(this);
                    mFooterView.addView(mEditorItem);
                }
            } else {
                if (mEditorItem != null) {
                    mFooterView.removeView(mEditorItem);
                    mEditorItem = null;
                }
            }
        }
    }

    private void initHeader() {
        mHeaderView = View.inflate(getActivity(), R.layout.layout_left_menu_header, null);
        mHeaderView.setTag(FragmentId.PROFILE);
        mHeaderView.setOnClickListener(this);
        initProfileMenuItem(mHeaderView);
        mHeaderViewStub = (ViewStub) mHeaderView.findViewById(R.id.vsHeaderStub);
        mListView.addHeaderView(mHeaderView);
    }


    public void updateAdapter() {
        initAdapter();
        if (mClosingsController != null) {
            mClosingsController.unlockLeftMenu();
        }
    }

    private void initAdapter() {
        SparseArray<LeftMenuAdapter.ILeftMenuItem> menuItems = new SparseArray<>();
        //- Profile added as part of header
        menuItems.put(DATING.getId(), LeftMenuAdapter.newLeftMenuItem(DATING, LeftMenuAdapter.TYPE_MENU_BUTTON,
                R.drawable.ic_dating_selector));
        if (CacheProfile.getOptions().messagesWithTabs.isEnabled()) {
            menuItems.put(TABBED_DIALOGS.getId(), LeftMenuAdapter.newLeftMenuItem(TABBED_DIALOGS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                    R.drawable.ic_dialog_selector));
        } else {
            menuItems.put(DIALOGS.getId(), LeftMenuAdapter.newLeftMenuItem(DIALOGS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                    R.drawable.ic_dialog_selector));
            menuItems.put(BOOKMARKS.getId(), LeftMenuAdapter.newLeftMenuItem(BOOKMARKS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                    R.drawable.ic_star_selector));
        }
        menuItems.put(VISITORS.getId(), LeftMenuAdapter.newLeftMenuItem(VISITORS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_guests_selector));
        if (CacheProfile.getOptions().likesWithThreeTabs.isEnabled()) {
            menuItems.put(TABBED_LIKES.getId(), LeftMenuAdapter.newLeftMenuItem(TABBED_LIKES, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                    R.drawable.ic_likes_selector));
        } else {
            menuItems.put(LIKES.getId(), LeftMenuAdapter.newLeftMenuItem(LIKES, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                    R.drawable.ic_likes_selector));
            menuItems.put(ADMIRATIONS.getId(), LeftMenuAdapter.newLeftMenuItem(ADMIRATIONS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                    R.drawable.ic_admirations_selector));
            menuItems.put(MUTUAL.getId(), LeftMenuAdapter.newLeftMenuItem(MUTUAL, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                    R.drawable.ic_mutual_selector));
        }
        menuItems.put(FANS.getId(), LeftMenuAdapter.newLeftMenuItem(FANS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_fans_selector));
        menuItems.put(GEO.getId(), LeftMenuAdapter.newLeftMenuItem(GEO, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.icon_people_close));
        if (CacheProfile.getOptions().bonus.enabled) {
            menuItems.put(BONUS.getId(), LeftMenuAdapter.newLeftMenuItem(BONUS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                    R.drawable.ic_bonus_1));
        }
        mAdapter = new LeftMenuAdapter(this, menuItems);
        mListView.setAdapter(mAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_menu, null);
        mListView = (ListView) root.findViewById(R.id.lvMenu);
        // init & add header with profile selector view
        initHeader();
        // init adapter
        initAdapter();
        // init & add footer
        initFooter(root);
        // set listview settings
        // controller for closings uses ViewStub in header to be inflated
        mClosingsController = new ClosingsController(this, mHeaderViewStub, mAdapter);
        return root;
    }

    private void initFooter(View root) {
        mFooterView = (ViewGroup) root.findViewById(R.id.llCounters);
        mBuyWidgetController = new BuyWidgetController(getActivity(),
                mFooterView.findViewById(R.id.countersLayout));

        Products products = CacheProfile.getMarketProducts();
        mBuyWidgetController.setSalesEnabled(products != null && products.saleExists);
        initEditor();
    }

    private void initProfileMenuItem(View headerView) {
        if (headerView == null) return;
        final View profileLayout = headerView.findViewById(R.id.btnProfileLayout);
        final View profileLayoutWithBackground = headerView.findViewById(R.id.btnProfileLayoutWithBackground);
        // detect right layout for profile button and init photo and background if needed
        View currentLayout;
        if (CacheProfile.premium
                && ProfileBackgrounds.isVipBackgroundId(getActivity(), CacheProfile.background_id)) {
            profileLayout.setVisibility(View.GONE);
            profileLayoutWithBackground.setVisibility(View.VISIBLE);
            currentLayout = profileLayoutWithBackground;
            String name = CacheProfile.first_name.length() <= 1
                    ? getString(R.string.general_profile) : CacheProfile.first_name;
            ((Button) profileLayoutWithBackground.findViewById(R.id.btnUserName)).setText(name);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                    ProfileBackgrounds.getBackgroundResource(getActivity(), CacheProfile.background_id));
            ((ImageViewRemote) profileLayoutWithBackground.findViewById(R.id.profile_menu_background_image))
                    .setRemoteImageBitmap(bitmap);
        } else {
            profileLayout.setVisibility(View.VISIBLE);
            ((TextView) profileLayout.findViewById(R.id.profile_button))
                    .setText(R.string.general_profile);
            profileLayoutWithBackground.setVisibility(View.GONE);
            currentLayout = profileLayout;
        }
        // init warning(fill profile info) icon
        ImageView profileInfo = (ImageView) currentLayout.findViewWithTag("profileInfo");
        if (profileInfo != null) {
            if (!CacheProfile.isDataFilled() && CacheProfile.isLoaded()) {
                profileInfo.setImageResource(R.drawable.ic_not_enough_data);
                profileInfo.setVisibility(View.VISIBLE);
            } else {
                profileInfo.setVisibility(View.GONE);
            }
        }
        // set avatar photo for current profile menu item
        ImageViewRemote avatarView = ((ImageViewRemote) currentLayout.findViewWithTag("ivMenuAvatar"));
        if (avatarView != null) {
            avatarView.setPhoto(CacheProfile.photo);
        }
        // set OnClickListener for profile menu item
        mProfileButton = (TextView) currentLayout.findViewWithTag("profile_fragment_button_tag");
        if (mProfileButton == null) {
            mProfileButton = (TextView) currentLayout.findViewWithTag(PROFILE);
        } else {
            mProfileButton.setTag(FragmentId.PROFILE);
        }
        if (mProfileButton != null) {
            mProfileButton.setOnClickListener(this);
            notifyDataSetChanged(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(
                CURRENT_FRAGMENT_STATE,
                getCurrentFragmentId()
        );
    }

    public void hideBuyWidget() {
        if (mBuyWidgetController != null) {
            mBuyWidgetController.hide();
        }
    }

    public void showBuyWidjet() {
        if (mBuyWidgetController != null) {
            mBuyWidgetController.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CacheProfile.PROFILE_UPDATE_ACTION);
        filter.addAction(Products.INTENT_UPDATE_PRODUCTS);
        filter.addAction(CountersManager.UPDATE_BALANCE);
        filter.addAction(SELECT_MENU_ITEM);
        filter.addAction(LikesClosingFragment.ACTION_LIKES_CLOSINGS_PROCESSED);
        filter.addAction(MutualClosingFragment.ACTION_MUTUAL_CLOSINGS_PROCESSED);
        filter.addAction(Options.Closing.DATA_FOR_CLOSING_RECEIVED_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, filter);
        initProfileMenuItem(mHeaderView);
        if (mBuyWidgetController != null) {
            mBuyWidgetController.updateBalance();
            Products products = CacheProfile.getMarketProducts();
            if (products != null && products.saleExists == !mBuyWidgetController.salesEnabled) {
                mBuyWidgetController.setSalesEnabled(products.saleExists);
            }
        }
        // We need to clean state if there was a logout in other Activity
        mClosingsController.onLogoutWasInitiated();
        if (CacheProfile.premium) {
            mClosingsController.onPremiumObtained();
        }

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
        notifyDataSetChanged(false);
    }

    /**
     * To change selected state of menu items from Header & Adapter
     */
    private void notifyDataSetChanged(boolean updateOnlyHeader) {
        if (mAdapter != null && !updateOnlyHeader) {
            mAdapter.notifyDataSetChanged();
        }
        if (mProfileButton != null) {
            if (mSelectedFragment == PROFILE || mSelectedFragment == VIP_PROFILE) {
                mProfileButton.setSelected(true);
                if (mClosingsController != null) {
                    mClosingsController.unselectMenuItems();
                    mClosingsController.unlockLeftMenu();
                }
            } else {
                mProfileButton.setSelected(false);
            }
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

        //Закрываем меню только после создания фрагмента
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mFragmentSwitchListener != null) {
                    mFragmentSwitchListener.onFragmentSwitch(mSelectedFragment);
                }
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
                fragment = OwnProfileFragment.newInstance(VipBuyFragment.class.getName());
                break;
            case PROFILE:
                fragment = OwnProfileFragment.newInstance();
                break;
            case DATING:
                fragment = new DatingFragment();
                break;
            case ADMIRATIONS:
                fragment = new AdmirationFragment();
                break;
            case LIKES:
                fragment = new LikesFragment();
                break;
            case LIKES_CLOSINGS:
                fragment = new LikesClosingFragment();
                break;
            case MUTUAL:
                fragment = new MutualFragment();
                break;
            case MUTUAL_CLOSINGS:
                fragment = new MutualClosingFragment();
                break;
            case DIALOGS:
                fragment = new DialogsFragment();
                break;
            case BOOKMARKS:
                fragment = new BookmarksFragment();
                break;
            case FANS:
                fragment = new FansFragment();
                break;
            case GEO:
                fragment = new PeopleNearbyFragment();
                break;
            case BONUS:
                fragment = BonusFragment.newInstance(true);
                break;
            case VISITORS:
                fragment = new VisitorsFragment();
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

    @Override
    public void onClick(View v) {
        if (mListView.isClickable()) {
            FragmentId id = null;
            if (v.getTag() instanceof LeftMenuAdapter.ViewHolder) {
                id = ((LeftMenuAdapter.ViewHolder) v.getTag()).getFragmentId();
            }
            if (v.getTag() instanceof FragmentId) {
                id = (FragmentId) v.getTag();
            }
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

    public void onLoadProfile() {
        // We don't have counters' values from cached data
        // so we have to make actions after we will receive data from server.
        // Another call is in BroadcastReceiver of MenuFragment
        if (!CacheProfile.premium) mClosingsController.show();
    }

    public boolean isLockedByClosings() {
        return mClosingsController.isLeftMenuLocked();
    }

    public void setOnFragmentSelected(OnFragmentSelectedListener listener) {
        mOnFragmentSelected = listener;
    }

    public void setClickable(boolean clickable) {
        mListView.setClickable(clickable);
    }

    public void showClosingsDialog() {
        showClosingsDialog(mSelectedFragment);
    }

    public void showClosingsDialog(FragmentId selectedFragment) {
        if (ClosingsBuyVipDialog.opened) return;

        ClosingsBuyVipDialog newFragment = ClosingsBuyVipDialog.newInstance(selectedFragment);

        newFragment.setOnRespondToLikesListener(new ClosingsBuyVipDialog.IRespondToLikesListener() {
            @Override
            public void onRespondToLikes() {
                if (mClosingsController != null) {
                    mClosingsController.respondToLikes();
                }
            }
        });

        newFragment.show(getActivity().getSupportFragmentManager(), ClosingsBuyVipDialog.TAG);
    }

    public static interface OnFragmentSelectedListener {
        public void onFragmentSelected(FragmentId fragmentId);
    }

    public boolean isClosingsAvailable() {
        return !mAdapter.hasTabbdedPages();
    }
}

package com.topface.topface.ui.fragments;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.GooglePlayProducts;
import com.topface.topface.data.Options;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.BonusFragment;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.LeftMenuAdapter;
import com.topface.topface.ui.dialogs.ClosingsBuyVipDialog;
import com.topface.topface.ui.fragments.closing.LikesClosingFragment;
import com.topface.topface.ui.fragments.closing.MutualClosingFragment;
import com.topface.topface.ui.fragments.feed.AdmirationFragment;
import com.topface.topface.ui.fragments.feed.BookmarksFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.FansFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.BackgroundThread;
import com.topface.topface.utils.BuyWidgetController;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.ResourcesUtils;
import com.topface.topface.utils.controllers.ClosingsController;
import com.topface.topface.utils.http.ProfileBackgrounds;
import com.topface.topface.utils.offerwalls.Offerwalls;
import com.topface.topface.utils.social.AuthToken;

import java.util.ArrayList;
import java.util.List;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.*;

/**
 * Created by kirussell on 05.11.13.
 * Left menu for switching NavigationActivity fragments
 * extends ListFragment and does not have any xml layout
 */
public class MenuFragment extends ListFragment implements View.OnClickListener {
    public static final String SELECT_MENU_ITEM = "com.topface.topface.action.menu.selectitem";
    public static final String SELECTED_FRAGMENT_ID = "com.topface.topface.action.menu.item";
    private static final String CURRENT_FRAGMENT_STATE = "menu_fragment_current_fragment";

    private OnFragmentSelectedListener mOnFragmentSelected;
    private FragmentId mSelectedFragment;
    private LeftMenuAdapter mAdapter;
    private boolean mHardwareAccelerated;
    private View mHeaderView;
    private Button mProfileButton;
    private BuyWidgetController mBuyWidgetController;
    private ViewStub mHeaderViewStub;
    private ViewGroup mFooterView;
    private View mEditorItem;

    private ClosingsController mClosingsController;

    private static boolean mEditorInitializationForSessionInvoked = false;

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case CountersManager.UPDATE_BALANCE_COUNTERS:
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
                case GooglePlayProducts.INTENT_UPDATE_PRODUCTS:
                    if (mBuyWidgetController != null) {
                        mBuyWidgetController.setButtonBackgroundResource(
                                CacheProfile.getGooglePlayProducts().saleExists ?
                                        R.drawable.btn_sale_selector : R.drawable.btn_blue_selector
                        );
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
                    if (!CacheProfile.premium) mClosingsController.show();
                    break;
            }
        }
    };

    private void initBonus() {
        if (CacheProfile.getOptions().bonus.enabled && !mAdapter.hasFragment(F_BONUS)) {
            mAdapter.addItem(LeftMenuAdapter.newLeftMenuItem(F_BONUS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE, R.drawable.ic_bonus_1));
            mAdapter.refreshCounterBadges();
        }
    }

    private void initEditor() {
        if (mEditorInitializationForSessionInvoked) return;
        if (mFooterView != null) {
            if (Editor.isEditor() && mEditorItem == null) {
                mEditorItem = View.inflate(getActivity(), R.layout.item_left_menu_button_with_badge, null);
                Button btnMenu = (Button) mEditorItem.findViewById(R.id.btnMenu);
                btnMenu.setText(ResourcesUtils.getFragmentNameResId(FragmentId.F_EDITOR));
                btnMenu.setTag(FragmentId.F_EDITOR);
                btnMenu.setOnClickListener(this);
                mFooterView.addView(mEditorItem);
            } else {
                mFooterView.removeView(mEditorItem);
            }
        }
        mEditorInitializationForSessionInvoked = true;
    }

    public static void selectFragment(FragmentId fragmentId) {
        Intent intent = new Intent();
        intent.setAction(SELECT_MENU_ITEM);
        intent.putExtra(SELECTED_FRAGMENT_ID, fragmentId);
        LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // init & add header with profile selector view
        initHeader();
        // init adapter
        initAdapter();
        // init & add footer
        initFooter();
        // set listview settings
        ListView list = getListView();
        list.setDividerHeight(0);
        list.setDivider(null);
        list.setBackgroundColor(getResources().getColor(R.color.bg_left_menu));
        // controller for closings uses ViewStub in header to be inflated
        mClosingsController = new ClosingsController(this, mHeaderViewStub, mAdapter);
    }

    private void initHeader() {
        mHeaderView = View.inflate(getActivity(), R.layout.layout_left_menu_header, null);
        initProfileMenuItem(mHeaderView);
        mHeaderViewStub = (ViewStub) mHeaderView.findViewById(R.id.vsHeaderStub);
        getListView().addHeaderView(mHeaderView);
    }

    private void initAdapter() {
        List<LeftMenuAdapter.ILeftMenuItem> menuItems = new ArrayList<>();
        //- Profile added as part of header
        menuItems.add(LeftMenuAdapter.newLeftMenuItem(F_DATING, LeftMenuAdapter.TYPE_MENU_BUTTON,
                R.drawable.ic_dating_selector));
        menuItems.add(LeftMenuAdapter.newLeftMenuItem(F_LIKES, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_likes_selector));
        menuItems.add(LeftMenuAdapter.newLeftMenuItem(F_ADMIRATIONS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_admirations_selector));
        menuItems.add(LeftMenuAdapter.newLeftMenuItem(F_MUTUAL, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_mutual_selector));
        menuItems.add(LeftMenuAdapter.newLeftMenuItem(F_DIALOGS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_dialog_selector));
        menuItems.add(LeftMenuAdapter.newLeftMenuItem(F_BOOKMARKS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_star_selector));
        menuItems.add(LeftMenuAdapter.newLeftMenuItem(F_FANS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_fans_selector));
        menuItems.add(LeftMenuAdapter.newLeftMenuItem(F_VISITORS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_guests_selector));
        if (CacheProfile.getOptions().bonus.enabled) {
            menuItems.add(LeftMenuAdapter.newLeftMenuItem(F_BONUS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                    R.drawable.ic_bonus_1));
        }
        mAdapter = new LeftMenuAdapter(this, menuItems);
        setListAdapter(mAdapter);
    }

    private void initFooter() {
        mFooterView = (ViewGroup) View.inflate(getActivity(), R.layout.layout_left_menu_footer, null);
        mBuyWidgetController = new BuyWidgetController(getActivity(),
                mFooterView.findViewById(R.id.countersLayout));
        getListView().addFooterView(mFooterView);
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
            ((ImageViewRemote) profileLayoutWithBackground.findViewById(R.id.ivProfileBackground))
                    .setRemoteImageBitmap(bitmap);
        } else {
            profileLayout.setVisibility(View.VISIBLE);
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
        mProfileButton = (Button) currentLayout.findViewWithTag("btnFragmentProfile");
        if (mProfileButton == null) {
            mProfileButton = (Button) currentLayout.findViewWithTag(F_PROFILE);
        } else {
            mProfileButton.setTag(F_PROFILE);
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
            FragmentId id = FragmentId.F_DATING;
            if (savedInstanceState != null) {
                FragmentId savedId = (FragmentId) savedInstanceState.getSerializable(CURRENT_FRAGMENT_STATE);
                if (savedId != null) {
                    id = savedId;
                }
            }
            switchFragment(id, false);
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

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CacheProfile.PROFILE_UPDATE_ACTION);
        filter.addAction(GooglePlayProducts.INTENT_UPDATE_PRODUCTS);
        filter.addAction(CountersManager.UPDATE_BALANCE_COUNTERS);
        filter.addAction(SELECT_MENU_ITEM);
        filter.addAction(LikesClosingFragment.ACTION_LIKES_CLOSINGS_PROCESSED);
        filter.addAction(MutualClosingFragment.ACTION_MUTUAL_CLOSINGS_PROCESSED);
        filter.addAction(Options.Closing.DATA_FOR_CLOSING_RECEIVED_ACTION);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, filter);
        initProfileMenuItem(mHeaderView);
        if (mBuyWidgetController != null) {
            mBuyWidgetController.updateBalance();
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
            if (mSelectedFragment == F_PROFILE || mSelectedFragment == F_VIP_PROFILE) {
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
            if (newFragment.isAdded()) {
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
        return mSelectedFragment == F_UNDEFINED ? F_DATING : mSelectedFragment;
    }

    private BaseFragment getFragmentNewInstanceById(FragmentId id) {
        BaseFragment fragment;
        switch (id) {
            case F_VIP_PROFILE:
                fragment = ProfileFragment.newInstance(CacheProfile.uid, ProfileFragment.TYPE_MY_PROFILE,
                        VipBuyFragment.class.getName());
                break;
            case F_PROFILE:
                fragment = ProfileFragment.newInstance(CacheProfile.uid, ProfileFragment.TYPE_MY_PROFILE);
                break;
            case F_DATING:
                fragment = new DatingFragment();
                break;
            case F_ADMIRATIONS:
                fragment = new AdmirationFragment();
                break;
            case F_LIKES:
                fragment = new LikesFragment();
                break;
            case F_LIKES_CLOSINGS:
                fragment = new LikesClosingFragment();
                break;
            case F_MUTUAL:
                fragment = new MutualFragment();
                break;
            case F_MUTUAL_CLOSINGS:
                fragment = new MutualClosingFragment();
                break;
            case F_DIALOGS:
                fragment = new DialogsFragment();
                break;
            case F_BOOKMARKS:
                fragment = new BookmarksFragment();
                break;
            case F_FANS:
                fragment = new FansFragment();
                break;
            case F_BONUS:
                fragment = new BonusFragment();
                break;
            case F_VISITORS:
                fragment = new VisitorsFragment();
                break;
            case F_SETTINGS:
                fragment = new SettingsFragment();
                break;
            case F_EDITOR:
                fragment = null;
                if (Editor.isEditor()) {
                    fragment = new EditorFragment();
                }
                break;
            default:
                fragment = ProfileFragment.newInstance(CacheProfile.uid, ProfileFragment.TYPE_MY_PROFILE);
                break;
        }
        return fragment;
    }

    @Override
    public void onClick(View v) {
        if (getListView().isClickable()) {
            FragmentId id = (FragmentId) v.getTag();
            //Сделано так, потому что при нажатии на кнопку бонусов не должно открываться нового фрагмента
            //к тому же тут сложная работа счетчика, которая отличается от стандартной логики. Мы контроллируем
            //его локально, а не серверно, как это происходит с остальными счетчиками.
            if (id == F_BONUS) {
                if (CacheProfile.NEED_SHOW_BONUS_COUNTER) {
                    new BackgroundThread() {
                        @Override
                        public void execute() {
                            SharedPreferences preferences = getActivity().getSharedPreferences(NavigationActivity.BONUS_COUNTER_TAG, Context.MODE_PRIVATE);
                            preferences.edit().putLong(NavigationActivity.BONUS_COUNTER_LAST_SHOW_TIME, CacheProfile.getOptions().bonus.timestamp).commit();
                        }
                    };
                }
                CacheProfile.NEED_SHOW_BONUS_COUNTER = false;
                mAdapter.refreshCounterBadges();
                Offerwalls.startOfferwall(getActivity());
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

    public static interface OnFragmentSelectedListener {
        public void onFragmentSelected(FragmentId fragmentId);
    }

    public void setOnFragmentSelected(OnFragmentSelectedListener listener) {
        mOnFragmentSelected = listener;
    }

    public void setClickable(boolean clickable) {
        getListView().setClickable(clickable);
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
        try {
            newFragment.show(getActivity().getSupportFragmentManager(), ClosingsBuyVipDialog.TAG);
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    public static void onLogout() {
        ClosingsController.onLogout();
        mEditorInitializationForSessionInvoked = false;
    }
}

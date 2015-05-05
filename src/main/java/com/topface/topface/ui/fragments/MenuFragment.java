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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Products;
import com.topface.topface.ui.INavigationFragmentsListener;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.LeftMenuAdapter;
import com.topface.topface.ui.fragments.feed.PeopleNearbyFragment;
import com.topface.topface.ui.fragments.feed.PhotoBlogFragment;
import com.topface.topface.ui.fragments.feed.TabbedDialogsFragment;
import com.topface.topface.ui.fragments.feed.TabbedLikesFragment;
import com.topface.topface.ui.fragments.feed.TabbedVisitorsFragment;
import com.topface.topface.ui.fragments.profile.OwnProfileFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.BuyWidgetController;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.ResourcesUtils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.http.ProfileBackgrounds;
import com.topface.topface.utils.offerwalls.OfferwallsManager;
import com.topface.topface.utils.social.AuthToken;

import java.io.Serializable;
import java.util.Arrays;

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
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.VIP_PROFILE;

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
    private ViewGroup mFooterView;
    private View mEditorItem;

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            switch (action) {
                case CountersManager.UPDATE_BALANCE:
                    mAdapter.refreshCounterBadges();
                    mBuyWidgetController.updateBalance();
                    break;
                case CacheProfile.PROFILE_UPDATE_ACTION:
                    initProfileMenuItem(mHeaderView);
                    initEditor();
                    initBonus();
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
                    mEditorItem.setTag(FragmentId.EDITOR);
                    mEditorItem.setOnClickListener(this);
                    mFooterView.addView(mEditorItem);
                }
            } else {
                if (mEditorItem != null) {
                    mFooterView.removeView(mEditorItem);
                    mEditorItem.setOnClickListener(null);
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
        mListView.addHeaderView(mHeaderView);
    }


    public void updateAdapter() {
        initAdapter();
    }

    private void initAdapter() {
        SparseArray<LeftMenuAdapter.ILeftMenuItem> menuItems = new SparseArray<>();
        //- Profile added as part of header
        menuItems.put(DATING.getId(), LeftMenuAdapter.newLeftMenuItem(DATING, LeftMenuAdapter.TYPE_MENU_BUTTON,
                R.drawable.ic_dating_selector));
        menuItems.put(TABBED_DIALOGS.getId(), LeftMenuAdapter.newLeftMenuItem(TABBED_DIALOGS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_dialog_selector));
        menuItems.put(PHOTO_BLOG.getId(), LeftMenuAdapter.newLeftMenuItem(PHOTO_BLOG, LeftMenuAdapter.TYPE_MENU_BUTTON,
                R.drawable.ic_photolenta));
        menuItems.put(TABBED_VISITORS.getId(), LeftMenuAdapter.newLeftMenuItem(TABBED_VISITORS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_guests_selector));
        menuItems.put(TABBED_LIKES.getId(), LeftMenuAdapter.newLeftMenuItem(TABBED_LIKES, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.drawable.ic_likes_selector));
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

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CacheProfile.PROFILE_UPDATE_ACTION);
        filter.addAction(Products.INTENT_UPDATE_PRODUCTS);
        filter.addAction(CountersManager.UPDATE_BALANCE);
        filter.addAction(SELECT_MENU_ITEM);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, filter);
        initProfileMenuItem(mHeaderView);
        if (mBuyWidgetController != null) {
            mBuyWidgetController.updateBalance();
            Products products = CacheProfile.getMarketProducts();
            if (products != null && products.saleExists == !mBuyWidgetController.salesEnabled) {
                mBuyWidgetController.setSalesEnabled(products.saleExists);
            }
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

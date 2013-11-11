package com.topface.topface.ui.fragments;

import android.annotation.TargetApi;
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
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.GooglePlayProducts;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.fragments.feed.AdmirationFragment;
import com.topface.topface.ui.fragments.feed.BookmarksFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.FansFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.BuyWidgetController;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.http.ProfileBackgrounds;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.*;
import static com.topface.topface.ui.fragments.BaseFragment.*;

/**
 * Created by kirussell on 05.11.13.
 * Left menu for switching NavigationActivity fragments
 */
public class MenuFragment extends ListFragment implements View.OnClickListener {
    public static final String SELECT_MENU_ITEM = "com.topface.topface.action.menu.selectitem";
    public static final String SELECTED_FRAGMENT_ID = "com.topface.topface.action.menu.item";

    private OnFragmentSelectedListener mOnFragmentSelected;
    private FragmentId mSelectedFragment;
    private LeftMenuAdapter mAdapter;
    private boolean mHardwareAccelerated;
    private View mHeaderView;
    private Button mProfileButton;
    private BuyWidgetController mBuyWidgetController;

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            if (action.equals(CountersManager.UPDATE_BALANCE_COUNTERS)) {
                mAdapter.refreshCounterBadges();
                mBuyWidgetController.updateBalance();
            } else if (action.equals(ProfileRequest.PROFILE_UPDATE_ACTION)) {
                initProfileMenuItem(mHeaderView);
            } else if (action.equals(GooglePlayProducts.INTENT_UPDATE_PRODUCTS)) {
                if (mBuyWidgetController != null) {
                    mBuyWidgetController.setButtonBackgroundResource(
                            CacheProfile.getGooglePlayProducts().saleExists ?
                                    R.drawable.btn_sale_selector : R.drawable.btn_blue_selector
                    );
                }
            } else if (action.equals(SELECT_MENU_ITEM)) {
                FragmentId fragmentId = (FragmentId) intent.getExtras().getSerializable(SELECTED_FRAGMENT_ID);
                selectMenu(fragmentId);
            }
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // init & add header with profile selector view
        initHeader();
        //init adapter
        initAdapter();
        // init & add footer
        initFooter();
        // set listview settings
        getListView().setDividerHeight(0);
        getListView().setDivider(null);
    }

    private void initHeader() {
        mHeaderView = View.inflate(getActivity(), R.layout.item_left_menu_button_profile, null);
        initProfileMenuItem(mHeaderView);
        getListView().addHeaderView(mHeaderView);
    }

    private void initAdapter() {
        List<ILeftMenuItem> menuItems = new ArrayList<ILeftMenuItem>();
        //- Profile added as part of header
        menuItems.add(newLeftMenuItem(F_DATING, LeftMenuAdapter.TYPE_MENU_BUTTON,
                R.string.general_dating, R.drawable.ic_dating_selector));
        menuItems.add(newLeftMenuItem(F_LIKES, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.string.general_likes, R.drawable.ic_likes_selector));
        menuItems.add(newLeftMenuItem(F_ADMIRATIONS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.string.general_admirations, R.drawable.ic_admirations_selector));
        menuItems.add(newLeftMenuItem(F_MUTUAL, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.string.general_mutual, R.drawable.ic_mutual_selector));
        menuItems.add(newLeftMenuItem(F_DIALOGS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.string.general_dialogs, R.drawable.ic_dialog_selector));
        menuItems.add(newLeftMenuItem(F_BOOKMARKS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.string.general_bookmarks, R.drawable.ic_star_selector));
        menuItems.add(newLeftMenuItem(F_FANS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.string.general_fans, R.drawable.ic_fans_selector));
        menuItems.add(newLeftMenuItem(F_VISITORS, LeftMenuAdapter.TYPE_MENU_BUTTON_WITH_BADGE,
                R.string.general_visitors, R.drawable.ic_guests_selector));
        mAdapter = new LeftMenuAdapter(menuItems);
        setListAdapter(mAdapter);
    }

    private void initFooter() {
        View footerView = View.inflate(getActivity(), R.layout.buy_widget, null);
        mBuyWidgetController = new BuyWidgetController(getActivity(), footerView);
        getListView().addFooterView(footerView);
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
            notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ProfileRequest.PROFILE_UPDATE_ACTION);
        filter.addAction(GooglePlayProducts.INTENT_UPDATE_PRODUCTS);
        filter.addAction(CountersManager.UPDATE_BALANCE_COUNTERS);
        filter.addAction(SELECT_MENU_ITEM);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, filter);
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

    private ILeftMenuItem newLeftMenuItem(final FragmentId menuId, final int menuType,
                                          final int menuTextResId, final int menuIconResId) {
        return new ILeftMenuItem() {
            @Override
            public FragmentId getMenuId() {
                return menuId;
            }

            @Override
            public int getMenuType() {
                return menuType;
            }

            @Override
            public String getMenuText() {
                return getString(menuTextResId);
            }

            @Override
            public int getMenuIconResId() {
                return menuIconResId;
            }
        };
    }

    /**
     * Selects menu item and shows fragment by id
     *
     * @param fragmentId id of fragment that is going to be shown
     */
    public void selectMenu(FragmentId fragmentId) {
        if (fragmentId != mSelectedFragment) {
            showFragment(fragmentId);
        } else if (mOnFragmentSelected != null) {
            mOnFragmentSelected.onFragmentSelected(fragmentId);
        }
        notifyDataSetChanged();
    }

    /**
     * Needs to change selected state of menu items from Header, Adapter
     */
    private void notifyDataSetChanged() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        if (mProfileButton != null) {
            mProfileButton.setSelected(
                    mSelectedFragment == F_PROFILE || mSelectedFragment == F_VIP_PROFILE
            );
        }
    }

    /**
     * Shows fragment by id
     *
     * @param fragmentId id of fragment that is going to be shown
     */
    public void showFragment(FragmentId fragmentId) {
        // TODO rewrite after investigation of fragmentTransaction process
        FragmentManager fragmentManager = getFragmentManager();
        Fragment oldFragment = fragmentManager.findFragmentById(R.id.fragment_content);

        String fragmentTag = getTagById(fragmentId);
        Debug.log("MenuFragment: Try switch to fragment with tag" + fragmentTag);
        BaseFragment newFragment = (BaseFragment) fragmentManager.findFragmentByTag(fragmentTag);
        //Если не нашли в FragmentManager уже существующего инстанса, то создаем новый
        if (newFragment == null) {
            newFragment = getFragmentNewInstanceById(fragmentId);
            Debug.log("MenuFragment: newFragment is null, create new instance");
        }

        if (oldFragment == null || newFragment != oldFragment) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            //Меняем фрагменты анимировано, но только на новых устройствах c HW ускорением
            if (mHardwareAccelerated) {
                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            if (newFragment.isAdded()) {
                transaction.detach(newFragment);
                Debug.error("MenuFragment: try detach already added new fragment" + fragmentTag);
            }

            if (oldFragment != null) {
                transaction.remove(oldFragment);
                Debug.log("MenuFragment: remove old fragment " + oldFragment.getTag());
            }
            transaction.add(R.id.fragment_content, newFragment, fragmentTag);
            transaction.commitAllowingStateLoss();
        } else {
            Debug.error("MenuFragment: new fragment already added");
        }
        mSelectedFragment = fragmentId;

        //Закрываем меню только после создания фрагмента
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mOnFragmentSelected.onFragmentSelected(mSelectedFragment);
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
            case F_MUTUAL:
                fragment = new MutualFragment();
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
            selectMenu((FragmentId) v.getTag());
        }
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

    /**
     * Classes for Adapter's work: ILeftMenuItem, LeftMenuAdapter
     */
    private interface ILeftMenuItem {
        FragmentId getMenuId();

        int getMenuType();

        String getMenuText();

        int getMenuIconResId();
    }

    private class LeftMenuAdapter extends BaseAdapter {
        private static final int TYPE_MENU_BUTTON = 0;
        private static final int TYPE_MENU_BUTTON_WITH_BADGE = 1;
        private static final int TYPE_COUNT = 2;
        private final List<ILeftMenuItem> mItems;
        private HashMap<FragmentId, TextView> mCountersBadgesMap = new HashMap<FragmentId, TextView>();

        public LeftMenuAdapter(List<ILeftMenuItem> items) {
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public ILeftMenuItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).getMenuId().ordinal();
        }

        @NotNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // initialize holder and convertView
            final ViewHolder holder;
            // get menu item on current position
            final ILeftMenuItem item = getItem(position);
            //init convertView
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(getActivity(), R.layout.item_left_menu_button_with_badge, null);
                holder.mBtnMenu = (Button) convertView.findViewById(R.id.btnMenu);
                holder.mCounterBadge = (TextView) convertView.findViewById(R.id.tvCounterBadge);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // initiate views' state in holder
            switch (item.getMenuType()) {
                case TYPE_MENU_BUTTON:
                    holder.mBtnMenu.setText(item.getMenuText());
                    holder.mCounterBadge.setVisibility(View.GONE);
                    unregisterCounterBadge(item);
                    break;
                case TYPE_MENU_BUTTON_WITH_BADGE:
                    holder.mBtnMenu.setText(item.getMenuText());
                    registerCounterBadge(item, holder.mCounterBadge);
                    break;
                default:
                    break;
            }
            // init menu item icon
            holder.mBtnMenu.setCompoundDrawablesWithIntrinsicBounds(
                    App.getContext().getResources().getDrawable(item.getMenuIconResId()),
                    null, null, null);
            // processing click events
            holder.mBtnMenu.setTag(item.getMenuId());
            holder.mBtnMenu.setOnClickListener(MenuFragment.this);
            // switch selected state of menu item button view
            holder.mBtnMenu.setSelected(getCurrentFragmentId() == item.getMenuId());
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return mItems.get(position).getMenuType();
        }

        @Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
        }

        private void registerCounterBadge(ILeftMenuItem item, TextView mCounterBadge) {
            FragmentId id = item.getMenuId();
            mCountersBadgesMap.put(item.getMenuId(), mCounterBadge);
            updateCounterBadge(id, mCounterBadge);
        }

        private void unregisterCounterBadge(ILeftMenuItem item) {
            mCountersBadgesMap.remove(item.getMenuId());
        }

        public void refreshCounterBadges() {
            for (ILeftMenuItem item : mItems) {
                if (item.getMenuType() == TYPE_MENU_BUTTON_WITH_BADGE) {
                    FragmentId menuId = item.getMenuId();
                    TextView mCounterBadgeView = mCountersBadgesMap.get(menuId);
                    updateCounterBadge(menuId, mCounterBadgeView);
                }
            }
        }

        private void updateCounterBadge(FragmentId menuId, TextView mCounterBadgeView) {
            int unreadCounter = CacheProfile.getUnreadCounterByFragmentId(menuId);
            if (unreadCounter > 0) {
                mCounterBadgeView.setText(Integer.toString(unreadCounter));
                mCounterBadgeView.setVisibility(View.VISIBLE);
            } else {
                mCounterBadgeView.setVisibility(View.GONE);
            }
        }

        class ViewHolder {
            Button mBtnMenu;
            TextView mCounterBadge;
        }
    }
}

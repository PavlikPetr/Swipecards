package com.topface.topface.utils.controllers;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.FeedMutual;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.Options;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ParallelApiRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.INavigationFragmentsListener;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.LeftMenuAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.BaseFragment.FragmentId;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.fragments.ViewUsersListFragment;
import com.topface.topface.ui.views.HackyDrawerLayout;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.cache.UsersListCacheManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kirussell on 12.11.13.
 * Controller for closings. All closings logic here for removing without pain
 */
public class ClosingsController implements View.OnClickListener {
    public static final String TAG = "Closings";
    public static final String LIKES_CACHE_KEY = "likes_cache_key";
    public static final String MUTUALS_CACHE_KEY = "mutuals_cache_key";

    private final MenuFragment mMenuFragment;
    private LeftMenuAdapter mAdapter;
    private View likesMenuItem;
    private View mutualsMenuItem;
    private ViewStub mViewStub;
    private View mClosingsWidget;
    private List<TextView> mCounterBadges = new ArrayList<>();
    private UsersListCacheManager mCacheManager;
    private static boolean mClosingsPassed = false; // need flag for session, skip on logout
    private int mReceivedUnreadLikes = 0;
    private int mReceivedUnreadMutuals = 0;
    private boolean mMutualClosingsActive = false;
    private boolean mLikesClosingsActive = false;
    private List<View> menuItemsButtons = new ArrayList<>();
    private boolean mLeftMenuLocked = false;
    private static boolean mLogoutWasInitiated = false;
    private INavigationFragmentsListener mNavigationFragmentsListener;

    public ClosingsController(@NotNull final MenuFragment menuFragment, @NotNull ViewStub mHeaderViewStub, @NotNull LeftMenuAdapter adapter) {
        mMenuFragment = menuFragment;
        mNavigationFragmentsListener = mMenuFragment.getNavigationFragmentsListener();
        mViewStub = mHeaderViewStub;
        mViewStub.setLayoutResource(R.layout.layout_left_menu_closings_widget);
        mAdapter = adapter;
        mCacheManager = new UsersListCacheManager(null, null);
    }

    /**
     * Safe show of closings
     * It won't show closing if it is not applicable
     *
     * @return true show of closings initiated successfully,
     * but still after retrieving feeds there can be no closings at all
     */
    public boolean show() {
        return canShowClosings() ? showInner() : false;
    }

    /**
     * Initiates show of closings
     * Note: first check if you can show closings with {@link this.canShowClosings()}
     *
     * @return true show of closings initiated successfully,
     * but still after retrieving feeds there can be no closings at all
     */
    private boolean showInner() {
        Context context = mMenuFragment.getActivity();
        ApiRequest likesRequest = getUsersListRequest(FeedRequest.FeedService.LIKES, context);
        likesRequest.callback(getDataRequestHandler(FeedRequest.FeedService.LIKES));
        ApiRequest mutualsRequest = getUsersListRequest(FeedRequest.FeedService.MUTUAL, context);
        mutualsRequest.callback(getDataRequestHandler(FeedRequest.FeedService.MUTUAL));

        ApiHandler handler = new SimpleApiHandler() {

            @Override
            public void success(IApiResponse response) {
                super.success(response);
                Options.Closing closings = CacheProfile.getOptions().closing;
                boolean needLikesClosings = mReceivedUnreadLikes > 0 && closings.isLikesAvailable();
                boolean needMutualsClosings = mReceivedUnreadMutuals > 0 && closings.isMutualAvailable();
                Debug.log(ClosingsController.TAG, "has likes=" + needLikesClosings +
                        " has mutuals=" + needMutualsClosings);
                if (needLikesClosings || needMutualsClosings) {
                    Debug.log(ClosingsController.TAG, "all passed to show and lock menu");
                    mClosingsWidget = getClosingsWidget();
                    mClosingsWidget.setVisibility(View.VISIBLE);
                    mClosingsWidget.findViewById(R.id.btnBuyVipFromClosingsWidget)
                            .setOnClickListener(ClosingsController.this);
                    likesMenuItem = mClosingsWidget.findViewById(R.id.itemLikesClosings);
                    if (initMenuItem(likesMenuItem, R.string.general_likes, R.drawable.ic_likes_selector,
                            needLikesClosings,
                            FragmentId.F_LIKES_CLOSINGS)) {
                        mAdapter.hideItem(FragmentId.F_LIKES);
                        mLikesClosingsActive = true;
                    }
                    mutualsMenuItem = mClosingsWidget.findViewById(R.id.itemMutualsClosings);
                    if (initMenuItem(mutualsMenuItem, R.string.general_mutual, R.drawable.ic_mutual_selector,
                            needMutualsClosings,
                            FragmentId.F_MUTUAL_CLOSINGS)) {
                        mAdapter.hideItem(FragmentId.F_MUTUAL);
                        mMutualClosingsActive = true;
                    }
                    mAdapter.setEnabled(false);
                    mAdapter.notifyDataSetChanged();
                    lockLeftMenu();
                } else {
                    Debug.log(ClosingsController.TAG, "no closings");
                }
            }
        };

        new ParallelApiRequest(App.getContext())
                .addRequest(likesRequest)
                .addRequest(mutualsRequest)
                .callback(handler)
                .exec();
        return true;
    }

    private View getClosingsWidget() {
        if (mClosingsWidget == null) {
            mClosingsWidget = mViewStub.inflate();
        }
        return mClosingsWidget;
    }

    private DataApiHandler<UsersList> getDataRequestHandler(final FeedRequest.FeedService serviceType) {
        final Class itemClass;
        final String cacheKey;
        switch (serviceType) {
            case MUTUAL:
                itemClass = FeedMutual.class;
                cacheKey = MUTUALS_CACHE_KEY;
                break;
            case LIKES:
                itemClass = FeedLike.class;
                cacheKey = LIKES_CACHE_KEY;
                break;
            default:
                itemClass = null;
                cacheKey = null;
                break;
        }
        if (itemClass == null) return null;
        return new DataApiHandler<UsersList>() {
            boolean isDataCached = false;

            @Override
            protected void success(UsersList data, IApiResponse response) {
                if (isDataCached) {
                    switch (serviceType) {
                        case MUTUAL:
                            mReceivedUnreadMutuals = data.size();
                            break;
                        case LIKES:
                            mReceivedUnreadLikes = data.size();
                            break;
                        default:
                            break;
                    }
                }
            }

            @Override
            protected UsersList parseResponse(ApiResponse response) {
                FeedListData<FeedItem> items = new FeedListData<>(response.getJsonResult(), itemClass);
                UsersList data = new UsersList<>(items, FeedUser.class);
                if (data.size() > 0) {
                    mCacheManager.changeCacheKeyTo(cacheKey, itemClass);
                    mCacheManager.setCache(data);
                    isDataCached = true;
                }
                return data;
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
            }
        };
    }

    protected ApiRequest getUsersListRequest(FeedRequest.FeedService feedType, Context context) {
        FeedRequest request = new FeedRequest(feedType, context);
        request.limit = ViewUsersListFragment.LIMIT;
        request.unread = true;
        request.leave = true;
        return request;
    }

    /**
     * Initializes closing menu items if needed
     *
     * @param menuItem     menu item view
     * @param btnTextResId test resourse id
     * @param iconResId    icon resource id
     * @param visible      true if menu item has to be shown
     * @param fragmentId   id for fragment which will be shown when menu item will be chosen
     * @return true if closing menu item is visible
     */
    private boolean initMenuItem(View menuItem, int btnTextResId, int iconResId, boolean visible,
                                 FragmentId fragmentId) {
        menuItem.setVisibility(visible ? View.VISIBLE : View.GONE);
        Button menuButton = (Button) menuItem.findViewById(R.id.btnMenu);
        menuButton.setOnClickListener(this);
        menuButton.setText(btnTextResId);
        menuButton.setTag(fragmentId);
        menuButton.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
        menuItemsButtons.add(menuButton);
        TextView counterBadge = (TextView) menuItem.findViewById(R.id.tvCounterBadge);
        counterBadge.setTag(fragmentId);
        mCounterBadges.add(counterBadge);
        updateCounterBadge(counterBadge);
        return visible;
    }

    public void refreshCounterBadges() {
        for (TextView badge : mCounterBadges) {
            if (badge != null) {
                updateCounterBadge(badge);
            }
        }
    }

    private void updateCounterBadge(TextView badge) {
        int unread = CacheProfile.getUnreadCounterByFragmentId((FragmentId) badge.getTag());
        if (unread > 0) {
            badge.setText(Integer.toString(unread));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if (tag instanceof FragmentId) {
            switch ((FragmentId) tag) {
                case F_LIKES_CLOSINGS:
                    selectMenuItem(FragmentId.F_LIKES_CLOSINGS);
                    break;
                case F_MUTUAL_CLOSINGS:
                    selectMenuItem(FragmentId.F_MUTUAL_CLOSINGS);
                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.btnBuyVipFromClosingsWidget:
                    mMenuFragment.startActivity(PurchasesActivity.createVipBuyIntent(null, "Menu"));
                    break;
                default:
                    break;
            }
        }
    }

    private void selectMenuItem(FragmentId id) {
        if (id != null) {
            if (mNavigationFragmentsListener != null) {
                mNavigationFragmentsListener.onShowActionBar();
            }
            unlockLeftMenu();
            MenuFragment.selectFragment(id);
        }
        for (View item : menuItemsButtons) {
            Object tag = item.getTag();
            if (tag instanceof FragmentId) {
                item.setSelected(tag == id);
            } else {
                item.setSelected(false);
            }
        }
    }

    public void onClosingsProcessed(FeedRequest.FeedService service) {
        if (mClosingsPassed) return;
        if (service == FeedRequest.FeedService.LIKES) {
            if (!mMutualClosingsActive) {
                removeClosings();
            } else {
                if (mLikesClosingsActive && likesMenuItem != null) {
                    likesMenuItem.setVisibility(View.GONE);
                    if (mAdapter != null) {
                        mAdapter.showItem(FragmentId.F_LIKES);
                        mAdapter.notifyDataSetChanged();
                    }
                    selectMenuItem(FragmentId.F_MUTUAL_CLOSINGS);
                }
            }
            CacheProfile.getOptions().closing.onStopLikesClosings();
            mLikesClosingsActive = false;
        } else if (service == FeedRequest.FeedService.MUTUAL) {
            if (!mLikesClosingsActive) {
                removeClosings();
            } else {
                if (mMutualClosingsActive && mutualsMenuItem != null) {
                    mutualsMenuItem.setVisibility(View.GONE);
                    if (mAdapter != null) {
                        mAdapter.showItem(FragmentId.F_MUTUAL);
                        mAdapter.notifyDataSetChanged();
                    }
                    selectMenuItem(FragmentId.F_LIKES_CLOSINGS);
                }
            }
            CacheProfile.getOptions().closing.onStopMutualClosings();
            mMutualClosingsActive = false;
        } else {
            throw new IllegalArgumentException("Only LIKES and MUTUAL services can be passed");
        }
    }

    private void removeClosings() {
        removeClosings(null);
    }

    private void removeClosings(FragmentId currentSelectedFragmentInLeftMenu) {
        if (mClosingsWidget != null) mClosingsWidget.setVisibility(View.GONE);
        if (mAdapter != null) {
            mAdapter.setEnabled(true);
            mAdapter.showAllItems();
            mAdapter.notifyDataSetChanged();
        }
        // switch to DatingFragment after closings are passed
        unlockLeftMenu();
        if (currentSelectedFragmentInLeftMenu != FragmentId.F_PROFILE) {
            MenuFragment.selectFragment(BaseFragment.FragmentId.F_DATING);
        }
        mClosingsPassed = true;
        mLikesClosingsActive = false;
        mMutualClosingsActive = false;
        if (mNavigationFragmentsListener != null) {
            mNavigationFragmentsListener.onShowActionBar();
        }
    }

    public static void onLogout() {
        mClosingsPassed = false;
        mLogoutWasInitiated = true;
    }

    /**
     * Try to show appropriate closings fragment
     * First try MutualClosings then LikesClosings
     */
    public void respondToLikes() {
        if (mMutualClosingsActive) {
            selectMenuItem(FragmentId.F_MUTUAL_CLOSINGS);
        } else if (mLikesClosingsActive) {
            selectMenuItem(FragmentId.F_LIKES_CLOSINGS);
        }
    }

    public void unselectMenuItems() {
        selectMenuItem(null);
    }

    private void lockLeftMenu() {
        if (!mLeftMenuLocked) {
            if (mMenuFragment.getActivity() instanceof NavigationActivity) {
                NavigationActivity activity = (NavigationActivity) mMenuFragment.getActivity();
                if (mNavigationFragmentsListener != null) {
                    mNavigationFragmentsListener.onShowActionBar();
                }
                activity.setMenuLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, new HackyDrawerLayout.IBackPressedListener() {
                    @Override
                    public void onBackPressed() {
                        mMenuFragment.showClosingsDialog();
                    }
                });
                activity.showContent();
                activity.getSupportActionBar().setDisplayUseLogoEnabled(false);
            }
            mLeftMenuLocked = true;
        }
    }

    public void unlockLeftMenu() {
        if (mLeftMenuLocked) {
            mLeftMenuLocked = false;
            if (mMenuFragment.getActivity() instanceof NavigationActivity) {
                NavigationActivity activity = ((NavigationActivity) mMenuFragment.getActivity());
                activity.setMenuLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                activity.getSupportActionBar().setDisplayUseLogoEnabled(true);
            }
        }
    }

    public boolean isLeftMenuLocked() {
        return mLeftMenuLocked;
    }

    public void onLogoutWasInitiated() {
        if (mLogoutWasInitiated) {
            removeClosings();
            mClosingsPassed = false;
        }
        mLogoutWasInitiated = false;
    }

    public void onPremiumObtained(FragmentId fragmentId) {
        if (!mClosingsPassed || mLikesClosingsActive || mMutualClosingsActive) {
            removeClosings(fragmentId);
        }
    }

    /**
     * Check if you can try to show closings
     * You can't show closings if:
     * - closings are already passed
     * - closings are already showing
     * - server do not allow to show closings at this time
     *
     * @return tru if you can show closings now
     */
    public boolean canShowClosings() {
        return !(mClosingsPassed || mLikesClosingsActive || mMutualClosingsActive) &&
                CacheProfile.getOptions().closing.isClosingsEnabled();
    }

    @SuppressWarnings("UnusedDeclaration")
    public IStartAction createStartAction(final int priority) {
        return new AbstractStartAction() {
            @Override
            public void callInBackground() {
            }

            @Override
            public void callOnUi() {
                showInner();
            }

            @Override
            public boolean isApplicable() {
                return canShowClosings() && !CacheProfile.premium;
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String getActionName() {
                return "Closings";
            }
        };
    }
}

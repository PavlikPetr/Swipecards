package com.topface.topface.ui.fragments.feed.people_nearby;

import android.Manifest;
import android.database.DataSetObserver;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.gson.reflect.TypeToken;
import com.topface.statistics.generated.PeopleNearbyStatisticsGeneratedStatistics;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.FeedGeo;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Options;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PeopleNearbyAccessRequest;
import com.topface.topface.requests.PeopleNearbyRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.statistics.PeopleNearbyStatistics;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.PeopleNearbyAdapter;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.feed.NoFilterFeedFragment;
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyFragmentViewModel;
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.FlurryManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.extensions.PermissionsExtensions;
import com.topface.topface.utils.extensions.PermissionsExtensionsKt;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.geo.GeoLocationManager;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import rx.Subscription;
import rx.functions.Action1;

import static com.topface.topface.utils.FlurryManager.PEOPLE_NEARBY_UNLOCK;

@RuntimePermissions
public class PeopleNearbyFragment extends NoFilterFeedFragment<FeedGeo> {

    private final static int WAIT_LOCATION_DELAY = 10000;
    public static final String PAGE_NAME = "PeopleNerby";
    private DataSetObserver mObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            if (getListAdapter() != null && getListAdapter().getData().isEmpty()) {
                updateListWithOldGeo();
            }
        }
    };

    @Inject
    TopfaceAppState mAppState;
    private boolean mIsHistoryLoad = false;
    private boolean mIsMakeItemsRead = false;
    private GeoLocationManager mGeoLocationManager;
    private Subscription mSubscriptionLocation;
    private CountDownTimer mWaitLocationTimer;
    private int mCoins;
    private Action1<Location> mLocationAction = new Action1<Location>() {
        @Override
        public void call(Location location) {
            if (isGeoEnabled() && GeoLocationManager.isValidLocation(location)) {
                sendPeopleNearbyRequest(location, mIsHistoryLoad, mIsMakeItemsRead);
            }
        }
    };
    private Action1<BalanceData> mBalanceAction = new Action1<BalanceData>() {
        @Override
        public void call(BalanceData balanceData) {
            mCoins = balanceData.money;
        }
    };
    private Subscription mBalanceSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        App.from(getActivity()).inject(this);
        mSubscriptionLocation = mAppState.getObservable(Location.class).subscribe(mLocationAction);
        mBalanceSubscription = mAppState.getObservable(BalanceData.class).subscribe(mBalanceAction);
        super.onCreate(savedInstanceState);
        if (PermissionsExtensionsKt.isGrantedPermissions(getContext(), Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            geolocationManagerInit();
            sendShowStatistics();
        } else {
            onEmptyFeed(ErrorCodes.CANNOT_GET_GEO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PeopleNearbyFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
        App.getAppConfig().putPermissionsState(permissions, grantResults);
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void geolocationManagerInit() {
        mGeoLocationManager = new GeoLocationManager();
        mGeoLocationManager.registerProvidersChangedActionReceiver();
        startWaitLocationTimer();
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void doNotNeed() {
        ListView lv = getListView();
        if (lv != null) {
            lv.setVisibility(View.GONE);
        }
        onEmptyFeed(ErrorCodes.CANNOT_GET_GEO);
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    protected void allViewsInitialized() {
        super.allViewsInitialized();
        if (!isGeoEnabled()) {
            stopWaitLocationTimer();
            showCannotGetGeoErrorOnEmptyScreen();
        }
    }

    @Override
    protected Type getFeedListDataType() {
        return new TypeToken<FeedList<FeedGeo>>() {
        }.getType();
    }

    @Override
    protected Class getFeedListItemClass() {
        return FeedGeo.class;
    }

    @Override
    public void onDestroy() {
        stopWaitLocationTimer();
        if (null != mSubscriptionLocation) {
            mSubscriptionLocation.unsubscribe();
        }
        if (null != mBalanceSubscription) {
            mBalanceSubscription.unsubscribe();
        }
        if (mGeoLocationManager != null) {
            mGeoLocationManager.unregisterProvidersChangedActionReceiver();
            mGeoLocationManager.stopLocationListener();
        }
        super.onDestroy();
    }

    @Override
    protected int[] getTypesForGCM() {
        return new int[]{GCMUtils.GCM_TYPE_PEOPLE_NEARBY};
    }

    @Override
    protected int getFeedType() {
        return CountersManager.PEOPLE_NEARLY;
    }

    @Override
    protected FeedAdapter<FeedGeo> createNewAdapter() {
        PeopleNearbyAdapter adapter = new PeopleNearbyAdapter(getActivity(), getUpdaterCallback());
        adapter.registerDataSetObserver(mObserver);
        return adapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getListAdapter().unregisterDataSetObserver(mObserver);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.GEO;
    }

    @Override
    protected void updateData(boolean isPullToRefreshUpdating, final boolean isHistoryLoad, final boolean makeItemsRead) {
        mIsUpdating = true;
        mIsHistoryLoad = isHistoryLoad;
        mIsMakeItemsRead = makeItemsRead;
        if (isGeoEnabled()) {
            mGeoLocationManager.getLastKnownLocation();
        } else {
            showCannotGetGeoErrorOnEmptyScreen();
            refreshCompleted();
        }
    }

    private void showCannotGetGeoErrorOnEmptyScreen() {
        FeedAdapter<FeedGeo> adapter = getListAdapter();
        if (adapter != null) {
            adapter.removeAllData();
            adapter.notifyDataSetChanged();
        }
        showCannotGetGeoError();
    }

    private void sendPeopleNearbyRequest(Location location, final boolean isHistoryLoad, final boolean makeItemsRead) {
        if (location != null) {
            stopWaitLocationTimer();
            PeopleNearbyRequest request = new PeopleNearbyRequest(getActivity(), location.getLatitude(), location.getLongitude());
            request.callback(new DataApiHandler<FeedListData<FeedGeo>>() {

                @Override
                protected void success(FeedListData<FeedGeo> data, IApiResponse response) {
                    processSuccessUpdate(data, isHistoryLoad, false, makeItemsRead, getListAdapter().getLimit());
                }

                @Override
                protected FeedListData<FeedGeo> parseResponse(ApiResponse response) {
                    return getFeedList(response.jsonResult);
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    processFailUpdate(codeError, isHistoryLoad, getListAdapter(), false);

                }

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
                    refreshCompleted();
                }
            }).exec();
        } else {
            getListView().setVisibility(View.GONE);
            onEmptyFeed(ErrorCodes.CANNOT_GET_GEO);
        }
    }

    @Override
    protected void initLockedFeed(View inflated, int errorCode) {
        initEmptyScreenOnBlocked(findViewFlipper(inflated), App.get().getOptions().blockPeople);
    }

    @Nullable
    private ViewFlipper findViewFlipper(View inflated) {
        return (ViewFlipper) inflated.findViewById(R.id.vfEmptyViews);
    }

    @Override
    protected void initEmptyFeedView(final View inflated, int errorCode) {
        initEmptyScreen(findViewFlipper(inflated), errorCode);
    }

    private void initEmptyScreen(ViewFlipper emptyView, int errorCode) {
        if (emptyView != null) {
            switch ((int) PermissionsExtensionsKt.getPermissionStatus(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case (int) PermissionsExtensions.PERMISSION_GRANTED:
                    emptyView.setDisplayedChild(0);
                    ((TextView) emptyView.findViewById(R.id.blocked_geo_text))
                            .setText(errorCode == ErrorCodes.CANNOT_GET_GEO
                                    ? R.string.cannot_get_geo
                                    : R.string.nobody_nearby);
                    break;
                case (int) PermissionsExtensions.PERMISSION_DENIED:
                    sendPeopleNearbyPermissionOpen();
                    emptyView.setDisplayedChild(2);
                    com.topface.topface.databinding.LayoutUnavailableGeoBinding binding = DataBindingUtil.bind(emptyView.findViewById(R.id.unavailableGeoRootView));
                    binding.setViewModel(new UnavailableGeoViewModel(binding, new Function0<Unit>() {
                        @Override
                        public Unit invoke() {
                            PeopleNearbyFragmentPermissionsDispatcher.geolocationManagerInitWithCheck(PeopleNearbyFragment.this);
                            return null;
                        }
                    }));
                    break;
                case (int) PermissionsExtensions.PERMISSION_NEVER_ASK_AGAIN:
                    sendPeopleNearbyPermissionOpen();
                    emptyView.setDisplayedChild(2);
                    binding = DataBindingUtil.bind(emptyView.findViewById(R.id.unavailableGeoRootView));
                    binding.setViewModel(new DontAskGeoViewModel(binding));
                    break;
            }
        }

    }

    private void sendShowStatistics() {
        PeopleNearbyStatisticsGeneratedStatistics.sendNow_PEOPLE_NEARBY_OPEN(Utils.
                getUniqueKeyStatistic(PeopleNearbyStatistics.PEOPLE_NEARBY_OPEN));
        PeopleNearbyStatisticsGeneratedStatistics.sendNow_PEOPLE_NOT_UNIQUE_NEARBY_OPEN();
        AppConfig appConfig = App.getAppConfig();
        appConfig.incrGeoScreenShowCount();
        appConfig.saveConfig();
        if (appConfig.getGeoScreenShowCount() == PeopleNearbyFragmentViewModel.SEND_SHOW_SCREEN_STATISTICS) {
            PeopleNearbyStatisticsGeneratedStatistics.sendNow_PEOPLE_NEARBY_FIFTH_OPEN(Utils
                    .getUniqueKeyStatistic(PeopleNearbyStatistics.PEOPLE_NEARBY_FIFTH_OPEN));
        }
    }

    private void sendPeopleNearbyPermissionOpen() {
        PeopleNearbyStatisticsGeneratedStatistics.sendNow_PEOPLE_NEARBY_PERMISSION_OPEN(Utils.
                getUniqueKeyStatistic(PeopleNearbyStatistics.PEOPLE_NEARBY_PERMISSION_OPEN));
    }

    private void initEmptyScreenOnBlocked(ViewFlipper emptyView, final Options.BlockPeopleNearby blockPeopleNearby) {
        if (emptyView != null) {
            emptyView.setDisplayedChild(0);
            ((TextView) emptyView.findViewById(R.id.blocked_geo_text)).setText(blockPeopleNearby.text);
            initBuyCoinsButton(emptyView, blockPeopleNearby);
            initBuyVipButton(emptyView, blockPeopleNearby);
        }
    }

    private void initBuyVipButton(View emptyView, final Options.BlockPeopleNearby blockPeopleNearby) {
        final Button buyButton = (Button) emptyView.findViewById(R.id.buy_vip_button);
        TextView buyText = (TextView) emptyView.findViewById(R.id.buy_vip_text);
        if (App.from(getActivity()).getOptions().unlockAllForPremium) {
            initButtonForBlockedScreen(
                    buyText, blockPeopleNearby.textPremium,
                    buyButton, blockPeopleNearby.buttonTextPremium,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(
                                    PurchasesActivity.createVipBuyIntent(null, "PeopleNearby"),
                                    PurchasesActivity.INTENT_BUY_VIP
                            );
                        }
                    }
            );
        } else {
            buyText.setVisibility(View.GONE);
            buyButton.setVisibility(View.GONE);
        }
    }

    private void initBuyCoinsButton(final View emptyView, final Options.BlockPeopleNearby blockPeopleNearby) {
        final Button btnBuy = (Button) emptyView.findViewById(R.id.buy_coins_button);
        final ProgressBar progress = (ProgressBar) emptyView.findViewById(R.id.prsLoading);
        initButtonForBlockedScreen(btnBuy, blockPeopleNearby.buttonText, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCoins >= blockPeopleNearby.price) {
                    btnBuy.setVisibility(View.INVISIBLE);
                    progress.setVisibility(View.VISIBLE);
                    PeopleNearbyAccessRequest request = new PeopleNearbyAccessRequest(getActivity());
                    request.callback(new SimpleApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            FlurryManager.getInstance().sendSpendCoinsEvent(blockPeopleNearby.price, PEOPLE_NEARBY_UNLOCK);
                            if (isAdded()) {
                                emptyView.setVisibility(View.GONE);
                                updateData(false, true);
                            }
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            super.fail(codeError, response);
                            if (isAdded() && codeError == ErrorCodes.PAYMENT) {
                                openBuyScreenOnBlockedGeo(blockPeopleNearby);
                            }
                        }

                        @Override
                        public void always(IApiResponse response) {
                            super.always(response);
                            if (isAdded()) {
                                btnBuy.setVisibility(View.VISIBLE);
                                progress.setVisibility(View.GONE);
                            }
                        }
                    }).exec();
                } else {
                    openBuyScreenOnBlockedGeo(blockPeopleNearby);
                }
            }
        });
    }

    private void openBuyScreenOnBlockedGeo(Options.BlockPeopleNearby blockPeopleNearby) {
        startActivity(
                PurchasesActivity.createBuyingIntent("PeoplePaidNearby"
                        , PurchasesFragment.TYPE_PEOPLE_NEARBY, blockPeopleNearby.price, App.from(getActivity()).getOptions().topfaceOfferwallRedirect)
        );
    }

    private void startWaitLocationTimer() {
        stopWaitLocationTimer();
        mWaitLocationTimer = new CountDownTimer(WAIT_LOCATION_DELAY, WAIT_LOCATION_DELAY) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                updateListWithOldGeo();
                stopWaitLocationTimer();
            }
        }.start();
    }

    private void stopWaitLocationTimer() {
        if (mWaitLocationTimer != null) {
            mWaitLocationTimer.cancel();
        }
        mWaitLocationTimer = null;
    }

    private void updateListWithOldGeo() {
        Location location = App.getUserConfig().getUserGeoLocation();
        if (!isGeoEnabled()) {
            showCannotGetGeoError();
        } else if (location.getLatitude() != UserConfig.DEFAULT_USER_LATITUDE_LOCATION && location.getLongitude() != UserConfig.DEFAULT_USER_LONGITUDE_LOCATION) {
            sendPeopleNearbyRequest(location, mIsHistoryLoad, mIsMakeItemsRead);
        } else {
            showCannotGetGeoError();
        }
    }

    private void showCannotGetGeoError() {
        FeedAdapter<FeedGeo> adapter = getListAdapter();
        if (adapter == null || adapter.getCount() <= 0) {
            onEmptyFeed(ErrorCodes.CANNOT_GET_GEO);
        }
    }

    private boolean isGeoEnabled() {
        return mGeoLocationManager != null && mGeoLocationManager.getEnabledProvider() != GeoLocationManager.NavigationType.DISABLE;
    }

    @Override
    public void onResume() {
        super.onResume();
        ToolbarManager.INSTANCE.setToolbarSettings(new ToolbarSettingsData(getString(R.string.people_nearby)));
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_geo;
    }

    @Override
    protected int getContextMenuLayoutRes() {
        return R.menu.feed_context_menu_fans;
    }

    @Override
    protected int getUnreadCounter() {
        return mCountersData.getPeopleNearby();
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List ids) {
        return null;
    }

    @Override
    protected String getGcmUpdateAction() {
        return GCMUtils.GCM_PEOPLE_NEARBY_UPDATE;
    }

}
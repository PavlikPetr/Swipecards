package com.topface.topface.ui.fragments.feed;

import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
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
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.PeopleNearbyAdapter;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.FlurryManager;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.geo.GeoLocationManager;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

import static com.topface.topface.utils.FlurryManager.PEOPLE_NEARBY_UNLOCK;


public class PeopleNearbyFragment extends NoFilterFeedFragment<FeedGeo> {

    private final static int WAIT_LOCATION_DELAY = 10000;
    private static final String PAGE_NAME = "PeopleNerby";

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
            if (isGeoEnabled()) {
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
        startWaitLocationTimer();
        mGeoLocationManager = new GeoLocationManager();
        mGeoLocationManager.registerProvidersChangedActionReceiver();
        mSubscriptionLocation = mAppState.getObservable(Location.class).subscribe(mLocationAction);
        mBalanceSubscription = mAppState.getObservable(BalanceData.class).subscribe(mBalanceAction);
        super.onCreate(savedInstanceState);
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
        return new PeopleNearbyAdapter(getActivity(), getUpdaterCallback());
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
        initEmptyScreenOnBlocked(inflated, App.get().getOptions().blockPeople);
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        initEmptyScreen(inflated, errorCode);
    }

    private void initEmptyScreen(View emptyView, int errorCode) {
        if (emptyView != null) {
            emptyView.findViewById(R.id.controls_layout).setVisibility(View.GONE);
            ((TextView) emptyView.findViewById(R.id.blocked_geo_text)).setText(
                    errorCode == ErrorCodes.CANNOT_GET_GEO ? R.string.cannot_get_geo : R.string.nobody_nearby
            );
        }
    }

    private void initEmptyScreenOnBlocked(final View emptyView, final Options.BlockPeopleNearby blockPeopleNearby) {
        if (emptyView != null) {
            emptyView.findViewById(R.id.controls_layout).setVisibility(View.VISIBLE);
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
    protected String getTitle() {
        return getString(R.string.people_nearby);
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
        return mCountersData.peopleNearby;
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

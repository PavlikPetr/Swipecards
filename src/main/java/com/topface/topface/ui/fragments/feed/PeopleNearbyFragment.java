package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.topface.GCMUtils;
import com.topface.topface.R;
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
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.PeopleNearbyAdapter;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.geo.GeoLocationManager;

import org.json.JSONObject;

import java.util.List;


public class PeopleNearbyFragment extends NoFilterFeedFragment<FeedGeo> {
    protected View mEmptyFeedView;

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.background_people_close);
    }

    @Override
    protected int[] getTypesForGCM() {
        return new int[]{GCMUtils.GCM_TYPE_PEOPLE_NEARBY};
    }

    @Override
    protected int getTypeForCounters() {
        return CountersManager.GEO;
    }

    @Override
    protected FeedAdapter<FeedGeo> createNewAdapter() {
        return new PeopleNearbyAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected FeedListData<FeedGeo> getFeedList(JSONObject response) {
        return new FeedListData<>(response, FeedGeo.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.GEO;
    }

    @Override
    protected void updateData(boolean isPullToRefreshUpdating, final boolean isHistoryLoad, final boolean makeItemsRead) {
        mIsUpdating = true;
        onUpdateStart(isPullToRefreshUpdating || isHistoryLoad);
        Location location = GeoLocationManager.getLastKnownLocation(getActivity());
        if (location != null) {
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
            }).exec();
        } else {
            onEmptyFeed(ErrorCodes.CANNOT_GET_GEO);
        }
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        if (mEmptyFeedView == null) mEmptyFeedView = inflated;
        Options.BlockPeopleNearby blockPeopleNearby = CacheProfile.getOptions().blockPeople;
        if (errorCode == ErrorCodes.BLOCKED_PEOPLE_NEARBY) {
            initEmptyScreenOnBlocked(inflated, blockPeopleNearby);
        } else {
            initEmptyScreen(inflated, errorCode);
        }
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
        if (CacheProfile.getOptions().unlockAllForPremium) {
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
                if (CacheProfile.money >= blockPeopleNearby.price) {
                    btnBuy.setVisibility(View.INVISIBLE);
                    progress.setVisibility(View.VISIBLE);
                    PeopleNearbyAccessRequest request = new PeopleNearbyAccessRequest(getActivity());
                    request.callback(new SimpleApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
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
                PurchasesActivity.createBuyingIntent("PeoplePaidNearby", blockPeopleNearby.price)
        );
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
    protected DeleteAbstractRequest getDeleteRequest(List ids) {
        return null;
    }

    @Override
    protected String getGcmUpdateAction() {
        return GCMUtils.GCM_PEOPLE_NEARBY_UPDATE;
    }

}

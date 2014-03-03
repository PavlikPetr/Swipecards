package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

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
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.PeopleNearbyAdapter;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.GeoUtils.GeoLocationManager;

import org.json.JSONObject;

import java.util.List;


public class PeopleNearbyFragment extends FeedFragment{
    protected View mEmptyFeedView;

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.background_people_close);
    }

    @Override
    protected int getTypeForGCM() {
        return GCMUtils.GCM_TYPE_GEO;
    }

    @Override
    protected int getTypeForCounters() {
        return CountersManager.GEO;
    }

    @Override
    protected FeedAdapter getNewAdapter() {
        return new PeopleNearbyAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected FeedListData getFeedList(JSONObject response) {
        return new FeedListData(response, FeedGeo.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.GEO;
    }

    @Override
    protected void updateData(final boolean isPullToRefreshUpdating, final boolean isHistoryLoad, final boolean makeItemsRead) {
        mIsUpdating = true;
        onUpdateStart(isPullToRefreshUpdating || isHistoryLoad);
        Location location = GeoLocationManager.getLastKnownLocation(getActivity());
        PeopleNearbyRequest request = new PeopleNearbyRequest(getActivity(), location.getLatitude(), location.getLongitude());
        request.callback(new DataApiHandler<FeedListData<FeedGeo>>() {

            @Override
            protected void success(FeedListData<FeedGeo> data, IApiResponse response) {
                processSuccessUpdate(data, isHistoryLoad, isPullToRefreshUpdating, makeItemsRead, getListAdapter().getLimit());
            }

            @Override
            protected FeedListData<FeedGeo> parseResponse(ApiResponse response) {
                return getFeedList(response.jsonResult);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                processFailUpdate(codeError, isHistoryLoad, getListAdapter(), isPullToRefreshUpdating);
            }
        }).exec();
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        if (mEmptyFeedView == null) mEmptyFeedView = inflated;
        ViewFlipper viewFlipper = (ViewFlipper) inflated.findViewById(R.id.vfEmptyViews);
        Options.BlockPeopleNearby blockPeopleNearby = CacheProfile.getOptions().blockPeople;
        if (blockPeopleNearby.enabled && errorCode == ErrorCodes.BLOCKED_PEOPLE_NEARBY) {
            initEmptyScreenOnBlocked(inflated, viewFlipper, blockPeopleNearby);
        } else {
            initEmptyScreen(viewFlipper);
        }
    }

    private void initEmptyScreen(ViewFlipper viewFlipper) {
        viewFlipper.setDisplayedChild(0);
    }

    private void initEmptyScreenOnBlocked(final View inflated, ViewFlipper viewFlipper, final Options.BlockPeopleNearby blockPeopleNearby) {
        viewFlipper.setDisplayedChild(1);
        View currentView = viewFlipper.getChildAt(1);

        if (currentView != null) {
            ((TextView) currentView.findViewById(R.id.tvText)).setText(blockPeopleNearby.text);
            final Button btnBuy = (Button) currentView.findViewById(R.id.btnBuyCoins);
            final ProgressBar progress = (ProgressBar) currentView.findViewById(R.id.prsLoading);
            btnBuy.setText(blockPeopleNearby.buttonText);
            btnBuy.setOnClickListener(new View.OnClickListener() {
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
                                    inflated.setVisibility(View.GONE);
                                    updateData(false, true);
                                }
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                super.fail(codeError, response);
                                if (isAdded() && codeError == ErrorCodes.PAYMENT) {
                                    Toast.makeText(getActivity(), R.string.not_enough_coins, Toast.LENGTH_LONG).show();
                                    openBuyScreenOnBlockedGeo();
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
                        openBuyScreenOnBlockedGeo();
                    }
                }
            });
        }
    }

    private void openBuyScreenOnBlockedGeo() {
        startActivity(ContainerActivity.getBuyingIntent("PeoplePaidNearby"));
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

}

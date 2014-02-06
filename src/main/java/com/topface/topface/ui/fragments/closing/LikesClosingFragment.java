package com.topface.topface.ui.fragments.closing;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.FeedUser;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.SkipAllClosedRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.controllers.ClosingsController;

public class LikesClosingFragment extends ClosingFragment implements View.OnClickListener {

    public static final String ACTION_LIKES_CLOSINGS_PROCESSED = "action_closings_likes_processed";

    private TextView mUserName;
    private TextView mUserCity;

    @Override
    protected void initActionBarControls() {
    }

    @Override
    protected String getTitle() {
        return getString(R.string.sympathies);
    }

    @Override
    protected String getSubtitle() {
        return Utils.getQuantityString(
                R.plurals.number_of_sympathies,
                CacheProfile.unread_likes,
                CacheProfile.unread_likes
        );
    }

    @Override
    public Integer getTopPanelLayoutResId() {
        return R.layout.controls_closing_top_panel;
    }

    @Override
    protected String getCacheKey() {
        return ClosingsController.LIKES_CACHE_KEY;
    }

    @Override
    protected Integer getControlsLayoutResId() {
        return R.layout.controls_closed_likes;
    }

    @Override
    protected void initTopPanel(View topPanelView) {
        topPanelView.findViewById(R.id.btnWatchAsList).setOnClickListener(this);
        addViewsToHide(topPanelView);
    }

    @Override
    protected View initControls(View controlsView) {
        controlsView.findViewById(R.id.btnSkip).setOnClickListener(this);
        controlsView.findViewById(R.id.btnSkipAll).setOnClickListener(this);
        View btnSkipAll = controlsView.findViewById(R.id.btnSkipAll);
        btnSkipAll.setOnClickListener(this);
        if (CacheProfile.unread_likes > CacheProfile.getOptions().closing.limitSympathies) {
            btnSkipAll.setVisibility(View.VISIBLE);
        }
        controlsView.findViewById(R.id.btnMutual).setOnClickListener(this);
        controlsView.findViewById(R.id.btnChat).setOnClickListener(this);
        mUserName = (TextView) controlsView.findViewById(R.id.tvUserName);
        mUserCity = (TextView) controlsView.findViewById(R.id.tvUserCity);
        addViewsToHide(mUserName);
        addViewsToHide(mUserCity);
        return controlsView;
    }

    @Override
    protected void lockControls() {
    }

    @Override
    protected void unlockControls() {
    }

    @Override
    protected void onShowUser() {
    }

    @Override
    public Class getFeedUserContainerClass() {
        return FeedLike.class;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnMutual:
                EasyTracker.getTracker().sendEvent(getTrackName(), "Mutual", "", 1L);
                FeedUser currentUser = getCurrentUser();
                if (currentUser != null) {
                    getRateController().onLike(
                            currentUser.id,
                            SendLikeRequest.DEFAULT_MUTUAL,
                            new RateController.OnRateRequestListener() {
                                @Override
                                public void onRateCompleted() {
                                    if (isAdded()) refreshActionBarTitles();
                                }

                                @Override
                                public void onRateFailed(int userId, int mutualId, SendLikeRequest.Place place) {
                                }
                            });
                    showNextUser();
                }
                break;
            default:
                super.onClick(v);
        }
    }

    @Override
    protected void onUsersProcessed() {
        LocalBroadcastManager.getInstance(App.getContext())
                .sendBroadcast(new Intent(ACTION_LIKES_CLOSINGS_PROCESSED));
        super.onUsersProcessed();
    }

    @Override
    protected void setUserInfo(FeedUser user) {
        mUserName.setText(user.getNameAndAge());
        mUserCity.setText(user.city.name);
    }

    @Override
    protected int getSkipAllRequestType() {
        return SkipAllClosedRequest.LIKES;
    }

    @Override
    protected FeedRequest.FeedService getFeedType() {
        return FeedRequest.FeedService.LIKES;
    }

    @Override
    protected String getTrackName() {
        return "LikesClosing";
    }
}

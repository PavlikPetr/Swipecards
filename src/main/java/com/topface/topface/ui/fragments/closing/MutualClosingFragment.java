package com.topface.topface.ui.fragments.closing;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.FeedUser;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.SkipAllClosedRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.controllers.ClosingsController;

public class MutualClosingFragment extends ClosingFragment implements View.OnClickListener {

    public static final String ACTION_MUTUAL_CLOSINGS_PROCESSED = "action_closings_mutuals_processed";
    private View mBtnSkipAll;
    private TextView mUserName;
    private TextView mUserCity;

    @Override
    protected String getTitle() {
        return getString(R.string.mutual_sympathies);
    }

    @Override
    protected String getSubtitle() {
        return Utils.getQuantityString(R.plurals.number_of_sympathies, CacheProfile.unread_mutual, CacheProfile.unread_mutual);
    }

    @Override
    protected Integer getControlsLayoutResId() {
        return R.layout.controls_closed_mutuals;
    }

    @Override
    protected void initControls(View controlsView) {
        mBtnSkipAll = controlsView.findViewById(R.id.btnSkipAll);
        mBtnSkipAll.setOnClickListener(this);
        if (CacheProfile.unread_mutual > CacheProfile.getOptions().closing.limitMutual) {
            mBtnSkipAll.setVisibility(View.VISIBLE);
        }
        View btnSkip = controlsView.findViewById(R.id.btnSkip);
        btnSkip.setOnClickListener(this);
        controlsView.findViewById(R.id.btnChat).setOnClickListener(this);
        mUserName = (TextView) controlsView.findViewById(R.id.tvUserName);
        mUserCity = (TextView) controlsView.findViewById(R.id.tvUserCity);
        addViewsToHide(mUserName);
        addViewsToHide(mUserCity);
    }

    @Override
    protected void initTopPanel(View topPanelView) {
        topPanelView.findViewById(R.id.btnWatchAsList).setOnClickListener(this);
        addViewsToHide(topPanelView);
    }

    @Override
    protected FeedRequest.FeedService getFeedType() {
        return FeedRequest.FeedService.MUTUAL;
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
    protected String getCacheKey() {
        return ClosingsController.MUTUALS_CACHE_KEY;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void onUsersProcessed() {
        LocalBroadcastManager.getInstance(App.getContext())
                .sendBroadcast(new Intent(ACTION_MUTUAL_CLOSINGS_PROCESSED));
        super.onUsersProcessed();
    }

    @Override
    protected void setUserInfo(FeedUser user) {
        mUserName.setText(user.getNameAndAge());
        mUserCity.setText(user.city.name);
    }

    @Override
    protected int getSkipAllRequestType() {
        return SkipAllClosedRequest.MUTUAL;
    }

    @Override
    protected String getTrackName() {
        return "MutualsClosing";
    }
}

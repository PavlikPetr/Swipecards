package com.topface.topface.ui.fragments.closing;

import android.view.View;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.FeedUser;
import com.topface.topface.requests.DeleteFeedRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SkipAllClosedRequest;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class MutualClosingFragment extends ClosingFragment implements View.OnClickListener {

    public static boolean usersProcessed;

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
        controlsView.findViewById(R.id.btnForget).setOnClickListener(this);
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
    }

    @Override
    protected void initTopPanel(View topPanelView) {
        topPanelView.findViewById(R.id.btnWatchAsList).setOnClickListener(this);
    }

    @Override
    protected FeedRequest.FeedService getFeedType() {
        return FeedRequest.FeedService.MUTUAL;
    }

    @Override
    protected void lockControls() {
        //TODO change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void unlockControls() {
        //TODO change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onShowUser() {
        //TODO
    }

    @Override
    public Class getFeedUserContainerClass() {
        return FeedLike.class;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnForget:
                EasyTracker.getTracker().trackEvent(getTrackName(), "Forget", "", 1L);
                FeedUser user = getCurrentUser();
                if (user != null) {
                    DeleteFeedRequest deleteRequest = new DeleteFeedRequest(user.feedItem.id, getActivity());
                    deleteRequest.callback(new SimpleApiHandler() {
                        @Override
                        public void always(IApiResponse response) {
                            if (isAdded()) refreshActionBarTitles(getView());
                        }
                    });
                    deleteRequest.exec();
                }
                showNextUser();
                break;
            default:
                super.onClick(v);
        }
    }

    @Override
    protected void onUsersProcessed() {
        usersProcessed = true;
        LikesClosingFragment.usersProcessed = false;
        CacheProfile.getOptions().closing.onStopMutualClosings();
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
    protected boolean alowSkipForNonPremium() {
        return false;
    }

    @Override
    protected String getTrackName() {
        return "MutualsClosing";
    }
}

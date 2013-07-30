package com.topface.topface.ui.fragments.closing;

import android.view.View;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

public class MutualClosingFragment extends ClosingFragment implements View.OnClickListener {

    public static boolean usersProcessed;

    private View mBtnSkipAll;

    @Override
    protected void initActionBarControls(ActionBar actionbar) {
    }

    @Override
    protected String getTitle() {
        return getString(R.string.mutual_sympathies);
    }

    @Override
    protected String getSubtitle() {
        return Utils.getQuantityString(R.plurals.number_of_sympathies, CacheProfile.unread_mutual, CacheProfile.unread_mutual);
    }

    @Override
    public Integer getTopPanelLayoutResId() {
        return R.layout.controls_closing_top_panel;
    }

    @Override
    protected Integer getControlsLayoutResId() {
        return R.layout.controls_closed_mutuals;
    }

    @Override
    protected void initTopPanel(View topPanelView) {
        topPanelView.findViewById(R.id.btnWatchAsList).setOnClickListener(this);
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
        btnSkip.setActivated(true);
        controlsView.findViewById(R.id.btnChat).setOnClickListener(this);
    }

    @Override
    protected void onPageSelected(int position) {
    }

    @Override
    protected UsersList<FeedUser> createUsersList() {
        return new UsersList<FeedUser>(FeedUser.class);
    }

    @Override
    protected ApiRequest getUsersListRequest() {
        FeedRequest request = new FeedRequest(FeedRequest.FeedService.MUTUAL, getActivity());
        request.limit = LIMIT;
        request.unread = true;
        String lastFeedId = getLastFeedId();
        if (lastFeedId != null)
            request.to = lastFeedId;
        return request;
    }

    @Override
    public Class getItemsClass() {
        return FeedUser.class;
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
                if (getCurrentUser() != null) {
                    DeleteFeedRequest deleteRequest = new DeleteFeedRequest(getCurrentUser().feedItem.id, getActivity());
                    deleteRequest.callback(new SimpleApiHandler() {
                        @Override
                        public void always(ApiResponse response) {
                            refreshActionBarTitles(getView());
                        }
                    });
                    registerRequest(deleteRequest);
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
        super.onUsersProcessed();
    }

    @Override
    protected int getSkipAllRequestType() {
        return SkipAllClosedRequest.MUTUAL;
    }

    @Override
    protected boolean alowSkipForNonPremium() {
        return false;
    }
}

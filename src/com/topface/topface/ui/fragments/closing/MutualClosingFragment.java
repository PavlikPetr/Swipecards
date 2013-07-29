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
        controlsView.findViewById(R.id.btnSkip).setOnClickListener(this);
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
            case R.id.btnSkipAll:
                skipAllRequests(SkipAllClosedRequest.MUTUAL);
                break;
            case R.id.btnSkip:
                SkipClosedRequest request = new SkipClosedRequest(getActivity());
                request.callback(new SimpleApiHandler() {
                    @Override
                    public void always(ApiResponse response) {
                        refreshActionBarTitles(getView());
                    }
                });
                registerRequest(request);
                request.item = getCurrentUser().feedItem.id;
                request.exec();
                showNextUser();
                break;
            case R.id.btnChat:
                showChat();
                break;
            case R.id.btnWatchAsList:
                //TODO as list
                break;
            default:
                break;
        }
    }

}

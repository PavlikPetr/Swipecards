package com.topface.topface.ui.fragments.closing;

import android.view.View;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.fragments.ViewUsersListFragment;
import com.topface.topface.utils.ActionBar;

public class LikesClosingFragment extends ViewUsersListFragment<FeedUser> implements View.OnClickListener{
    public static final int LIMIT = 40;

    @Override
    protected void initActionBarControls(ActionBar actionbar) {
    }

    @Override
    protected String getTitle() {
        return getString(R.string.sympathies);
    }

    @Override
    protected String getSubtitle() {
        return null;
    }

    @Override
    public Integer getTopPanelLayoutResId() {
        return R.layout.controls_closing_top_panel;
    }

    @Override
    protected Integer getControlsLayoutResId() {
        return R.layout.controls_closed_likes;
    }

    @Override
    protected void initTopPanel(View topPanelView) {
        //TODO change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void initControls(View controlsView) {
        controlsView.findViewById(R.id.btnSkip).setOnClickListener(this);
        controlsView.findViewById(R.id.btnMutual).setOnClickListener(this);
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
        FeedRequest request = new FeedRequest(FeedRequest.FeedService.LIKES, getActivity());
        request.limit = LIMIT;
        request.unread = false;
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
        //TODO change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Class getFeedUserContainerClass() {
        return FeedLike.class;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSkip:
                //TODO skipLike
                showNextUser();
                break;
            case R.id.btnMutual:
                //TODO mutual
                showNextUser();
                break;
            case R.id.btnChat:
                //TODO chat
                break;
            default:
                break;
        }
    }
}

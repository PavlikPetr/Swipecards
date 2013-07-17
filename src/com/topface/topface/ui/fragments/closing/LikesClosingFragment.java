package com.topface.topface.ui.fragments.closing;

import com.topface.topface.data.FeedUser;
import com.topface.topface.data.search.CachableUsersList;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.fragments.ViewUsersListFragment;

public class LikesClosingFragment extends ViewUsersListFragment<FeedUser>{
    @Override
    protected Integer getControlsLayoutResId() {
        return null;
    }

    @Override
    protected void onPageSelected(int position) {

    }

    @Override
    protected UsersList<FeedUser> createUsersList() {
        return new CachableUsersList<FeedUser>(FeedUser.class);
    }

    @Override
    protected ApiRequest getUsersListRequest() {
        return new FeedRequest(FeedRequest.FeedService.LIKES, getActivity());
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
}

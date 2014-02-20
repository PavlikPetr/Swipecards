package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.FeedAdapter;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by ilya on 20.02.14.
 */
public class PeopleCloseFragment extends FeedFragment{
    @Override
    protected Drawable getBackIcon() {
        return null;
    }

    @Override
    protected int getTypeForGCM() {
        return 0;
    }

    @Override
    protected int getTypeForCounters() {
        return 0;
    }

    @Override
    protected FeedAdapter getNewAdapter() {
        return null;
    }

    @Override
    protected FeedListData getFeedList(JSONObject response) {
        return null;
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return null;
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {

    }

    @Override
    protected int getEmptyFeedLayout() {
        return 0;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List ids) {
        return null;
    }

    @Override
    public void onAvatarClick(Object item, View view) {

    }
}

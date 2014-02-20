package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.topface.topface.GCMUtils;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.utils.CountersManager;

import org.json.JSONObject;

import java.util.List;

import ad.labs.sdk.R;

public class PeopleCloseFragment extends FeedFragment{
    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.background_people_close);
    }

    @Override
    protected int getTypeForGCM() {
        return GCMUtils.GCM_TYPE_UNKNOWN;
    }

    @Override
    protected int getTypeForCounters() {
        return CountersManager.UNKNOWN_TYPE;
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

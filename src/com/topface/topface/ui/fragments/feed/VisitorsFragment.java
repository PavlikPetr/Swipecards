package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Visitor;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.VisitorsListAdapter;
import com.topface.topface.utils.CountersManager;
import org.json.JSONObject;


public class VisitorsFragment extends FilterDisabledFragment<Visitor> {
    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.visitors_back_icon);
    }

    @Override
    protected int getTitle() {
        return R.string.general_visitors;
    }

    @Override
    protected FeedAdapter<Visitor> getNewAdapter() {
        return new VisitorsListAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected FeedListData<Visitor> getFeedList(JSONObject response) {
        return new FeedListData<Visitor>(response, Visitor.class);
    }


    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.VISITORS;
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.visitors_background_text;
    }

    @Override
    protected void decrementCounters() {
        CountersManager.getInstance(getActivity().getApplicationContext()).decrementCounter(CountersManager.VISITORS);
    }

    @Override
    protected int getTypeForGCM() {
        return GCMUtils.GCM_TYPE_GUESTS;
    }

    @Override
    protected int getTypeForCounters() {
        return CountersManager.VISITORS;
    }
}

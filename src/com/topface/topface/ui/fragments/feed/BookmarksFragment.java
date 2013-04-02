package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.FeedBookmark;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Visitor;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.BookmarksListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.adapters.VisitorsListAdapter;
import com.topface.topface.utils.CountersManager;
import org.json.JSONObject;

public class BookmarksFragment extends NoFilterFeedFragment<FeedBookmark> {

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.favorite);
    }

    @Override
    protected int getTitle() {
        return R.string.general_bookmarks_title;
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
    protected FeedAdapter<FeedBookmark> getNewAdapter() {
        return new BookmarksListAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected FeedListData<FeedBookmark> getFeedList(JSONObject response) {
        return  new FeedListData<FeedBookmark>(response, FeedBookmark.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.BOOKMARKS;
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.general_empty_bookmarks;
    }
}

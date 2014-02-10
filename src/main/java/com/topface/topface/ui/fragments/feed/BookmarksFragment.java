package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.FeedBookmark;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteBookmarksRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.BookmarksListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.utils.CountersManager;

import org.json.JSONObject;

import java.util.List;

public class BookmarksFragment extends NoFilterFeedFragment<FeedBookmark> {

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.favorite);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_bookmarks);
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
        return new FeedListData<>(response, FeedBookmark.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.BOOKMARKS;
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_bookmarks;
    }

    @Override
    protected int getContextMenuLayoutRes() {
        //Из избранного нельзя добавить в черный список (вернее можно, но не известно что из этого получится),
        //поэтому используем AB без соответсвующей кнопк
        return R.menu.feed_context_menu_blacklist;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        return new DeleteBookmarksRequest(ids, getActivity());
    }
}

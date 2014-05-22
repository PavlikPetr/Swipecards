package com.topface.topface.ui.fragments.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.FeedBookmark;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteBookmarksRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.adapters.BookmarksListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.utils.CountersManager;

import org.json.JSONObject;

import java.util.List;

public class BookmarksFragment extends NoFilterFeedFragment<FeedBookmark> {

    public static final int SELECTION_LIMIT = 10;

    private BroadcastReceiver mBookmarkedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(ContainerActivity.TYPE) &&
                    intent.getSerializableExtra(ContainerActivity.TYPE)
                            .equals(ContainerActivity.ActionTypes.BOOKMARK) && isAdded()) {
                int[] ids = intent.getIntArrayExtra(ContainerActivity.FEED_IDS);
                boolean hasValue = intent.hasExtra(ContainerActivity.VALUE);
                boolean value = intent.getBooleanExtra(ContainerActivity.VALUE, false);
                if (ids != null && hasValue) {
                    if (!value) {
                        getListAdapter().removeByUserIds(ids);
                    } else {
                        updateData(false, false, false);
                    }
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBookmarkedReceiver,
                new IntentFilter(ContainerActivity.UPDATE_USER_CATEGORY));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBookmarkedReceiver);
    }

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
    protected FeedAdapter<FeedBookmark> createNewAdapter() {
        return new BookmarksListAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected FeedListData<FeedBookmark> getFeedList(JSONObject response) {
        return new FeedListData<>(response, FeedBookmark.class);
    }

    /**
     * Этот метод используется для получения id элементов ленты при удалении.
     * Но в диалогах у нас работает не так как в остальных лентах
     * и приходится вручную пробрасывать id юзеров вместо id итема
     */
    @Override
    protected List<String> getSelectedFeedIds(FeedAdapter<FeedBookmark> adapter) {
        return adapter.getSelectedUsersStringIds();
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.BOOKMARKS;
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
    }

    @Override
    protected int getMultiSelectionLimit() {
        return SELECTION_LIMIT;
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

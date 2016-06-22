package com.topface.topface.ui.fragments.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedBookmark;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteBookmarksRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.adapters.BookmarksListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.config.FeedsCache;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

@FlurryOpenEvent(name = BookmarksFragment.PAGE_NAME)
public class BookmarksFragment extends NoFilterFeedFragment<FeedBookmark> {

    public static final int SELECTION_LIMIT = 10;

    @Inject
    NavigationState mNavigationState;

    public static final String PAGE_NAME = "Bookmarks";

    private BroadcastReceiver mBookmarkedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(BlackListAndBookmarkHandler.TYPE) &&
                    intent.getSerializableExtra(BlackListAndBookmarkHandler.TYPE)
                            .equals(BlackListAndBookmarkHandler.ActionTypes.BOOKMARK) && isAdded()) {
                int[] ids = intent.getIntArrayExtra(BlackListAndBookmarkHandler.FEED_IDS);
                boolean hasValue = intent.hasExtra(BlackListAndBookmarkHandler.VALUE);
                boolean value = intent.getBooleanExtra(BlackListAndBookmarkHandler.VALUE, false);
                if (hasValue) {
                    if (!value && ids != null) {
                        getListAdapter().removeByUserIds(ids);
                    } else {
                        updateOnResume();
                    }
                }
            }
        }
    };

    @Override
    protected Type getFeedListDataType() {
        return new TypeToken<FeedList<FeedBookmark>>() {
        }.getType();
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected Class getFeedListItemClass() {
        return FeedBookmark.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get().inject(this);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBookmarkedReceiver,
                new IntentFilter(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBookmarkedReceiver);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_bookmarks);
    }

    @Override
    protected int getFeedType() {
        return CountersManager.UNKNOWN_TYPE;
    }

    @NotNull
    @Override
    protected FeedsCache.FEEDS_TYPE getFeedsType() {
        return FeedsCache.FEEDS_TYPE.DATA_BOOKMARKS_FEEDS;
    }

    @Override
    protected FeedAdapter<FeedBookmark> createNewAdapter() {
        return new BookmarksListAdapter(getActivity(), getUpdaterCallback());
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
    protected void initLockedFeed(View inflated, int errorCode) {
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        inflated.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNavigationState.emmitNavigationState(new WrappedNavigationData(new LeftMenuSettingsData(FragmentIdData.DATING), WrappedNavigationData.SELECT_EXTERNALY));
            }
        });
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

    @Override
    protected int getUnreadCounter() {
        return 0;
    }
}

package com.topface.topface.ui.fragments.feed;

import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.R;
import com.topface.topface.data.BlackListItem;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteBlackListRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.BlackListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.utils.CountersManager;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Черный список. Сюда попадают заблокированые пользователи, отныне от них не приходит никакая активность
 */
public class BlackListFragment extends NoFilterFeedFragment<BlackListItem> implements View.OnClickListener {

    private static final String PAGE_NAME = "blacklist";

    @Override
    protected String getTitle() {
        return getString(R.string.black_list_title);
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    protected int getFeedType() {
        return CountersManager.UNKNOWN_TYPE;
    }

    @Override
    protected BlackListAdapter createNewAdapter() {
        return new BlackListAdapter(getActivity().getApplicationContext(), getUpdaterCallback());
    }

    @Override
    protected Type getFeedListDataType() {
        return new TypeToken<FeedList<BlackListItem>>() {
        }.getType();
    }

    @Override
    protected Class getFeedListItemClass() {
        return BlackListItem.class;
    }

    /**
     * Этот метод используется для получения id элементов ленты при удалении.
     * Но в диалогах у нас работает не так как в остальных лентах
     * и приходится вручную пробрасывать id юзеров вместо id итема
     */
    @Override
    protected List<String> getSelectedFeedIds(FeedAdapter<BlackListItem> adapter) {
        return adapter.getSelectedUsersStringIds();
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.BLACK_LIST;
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    protected void initLockedFeed(View inflated, int errorCode) {
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_blacklist;
    }

    @Override
    protected int getContextMenuLayoutRes() {
        return R.menu.feed_context_menu_blacklist;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        return new DeleteBlackListRequest(ids, getActivity());
    }

    @Override
    protected int getUnreadCounter() {
        return 0;
    }

    @Override
    protected boolean whetherDeleteIfBlacklisted() {
        return false;
    }
}

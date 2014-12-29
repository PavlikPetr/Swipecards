package com.topface.topface.ui.fragments.feed;

import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.data.BlackListItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteBlackListRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.BlackListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;

import org.json.JSONObject;

import java.util.List;

/**
 * Черный список. Сюда попадают заблокированые пользователи, отныне от них не приходит никакая активность
 */
public class BlackListFragment extends NoFilterFeedFragment<BlackListItem> implements View.OnClickListener {

    @Override
    protected String getTitle() {
        return getString(R.string.black_list_title);
    }

    @Override
    protected int getTypeForCounters() {
        return -1;
    }

    @Override
    protected BlackListAdapter createNewAdapter() {
        return new BlackListAdapter(getActivity().getApplicationContext(), getUpdaterCallback());
    }

    @Override
    protected FeedListData<BlackListItem> getFeedList(JSONObject response) {
        return new FeedListData<>(response, BlackListItem.class);
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
    protected void initDoubleButton(View view) {
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_blacklist;
    }

    @Override
    protected void initFloatBlock(ViewGroup view) {
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
    protected boolean whetherDeleteIfBlacklisted() {
        return false;
    }
}

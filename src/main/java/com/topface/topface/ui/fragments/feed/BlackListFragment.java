package com.topface.topface.ui.fragments.feed;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.BlackListItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.DeleteFeedsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.BlackListAdapter;

import org.json.JSONObject;

import java.util.List;

/**
 * Черный список. Сюда попадают заблокированые пользователи, отныне от них не приходит никакая активность
 */
public class BlackListFragment extends FeedFragment<BlackListItem> implements View.OnClickListener {

    private static final int BLACK_LIST_DELETE_BUTTON = 0;

    @Override
    protected int getLayout() {
        return R.layout.ac_feed_black_list;
    }

    @Override
    protected Drawable getBackIcon() {
        return null;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.black_list_title);
    }

    @Override
    protected int getTypeForGCM() {
        return GCMUtils.GCM_TYPE_UNKNOWN;
    }

    @Override
    protected int getTypeForCounters() {
        return -1;
    }

    @Override
    protected BlackListAdapter getNewAdapter() {
        return new BlackListAdapter(getActivity().getApplicationContext(), getUpdaterCallback());
    }

    @Override
    protected FeedListData<BlackListItem> getFeedList(JSONObject response) {
        return new FeedListData<BlackListItem>(response, BlackListItem.class);
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
    protected void initEmptyFeedView(View inflated) {
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
    protected DeleteFeedsRequest getDeleteRequest(List<String> ids, Context context) {
        return null;
    }
}

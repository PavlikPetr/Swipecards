package com.topface.topface.ui.fragments.feed;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.BlackListItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListDeleteRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.handlers.VipApiHandler;
import com.topface.topface.ui.adapters.BlackListAdapter;
import com.topface.topface.utils.ActionBar;
import org.json.JSONObject;

/**
 * Черный список. Сюда попадают заблокированые пользователи, отныне от них не приходит никакая активность
 */
public class BlackListFragment extends FeedFragment<BlackListItem> implements View.OnClickListener {

    private static final int BLACK_LIST_DELETE_BUTTON = 0;
    private ActionBar mActionBar;

    @Override
    protected int getLayout() {
        return R.layout.ac_feed_black_list;
    }

    @Override
    protected Drawable getBackIcon() {
        return null;
    }

    @Override
    protected int getTitle() {
        return R.string.black_list_title;
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
    protected void initNavigationBar(View view) {
        // Navigation bar
        mActionBar = getActionBar(view);
        mActionBar.setTitleText(getString(getTitle()));
        mActionBar.showBackButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }


    @Override
    protected DialogInterface.OnClickListener getLongTapActionsListener(final int position) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case BLACK_LIST_DELETE_BUTTON:
                        mLockView.setVisibility(View.VISIBLE);
                        onRemoveFromBlackList(position);
                        break;
                }
            }
        };
    }

    private void onRemoveFromBlackList(final int position) {
        mLockView.setVisibility(View.VISIBLE);
        new BlackListDeleteRequest(getItem(position).user.id, getActivity())
                .callback(new VipApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        if (isAdded()) {
                            getListAdapter().removeItem(position);
                        }
                    }

                    @Override
                    public void always(ApiResponse response) {
                        if (isAdded()) {
                            if (mLockView != null) {
                                mLockView.setVisibility(View.GONE);
                            }
                        }
                    }

                }).exec();
    }

    @Override
    protected String[] getLongTapActions() {
        if (editButtonsNames == null) {
            editButtonsNames = new String[]{getString(R.string.black_list_delete)};
        }
        return editButtonsNames;
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
}

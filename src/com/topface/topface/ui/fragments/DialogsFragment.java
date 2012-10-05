package com.topface.topface.ui.fragments;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;
import com.topface.topface.R;
import com.topface.topface.data.Dialog;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DialogRequest;
import com.topface.topface.ui.adapters.DialogListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.utils.CacheProfile;

public class DialogsFragment extends FeedFragment<DialogListAdapter> {
    @Override
    protected boolean isHasUnread() {
        return CacheProfile.unread_messages > 0;
    }

    @Override
    protected int getTitle() {
        return R.string.dashbrd_btn_chat;
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.inbox_background_text;
    }

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.dialogs_back_icon);
    }

    @Override
    protected DialogListAdapter getAdapter() {
        return new DialogListAdapter(getActivity().getApplicationContext(), getUpdaterCallback());
    }

    protected void updateData(final boolean isPushUpdating, final boolean isHistoryLoad) {
        mIsUpdating = true;
        onUpdateStart(isPushUpdating || isHistoryLoad);

        DialogRequest dialogRequest = new DialogRequest(getActivity());
        registerRequest(dialogRequest);
        Dialog lastItem = mListAdapter.getLastFeedItem();
        if (isHistoryLoad && lastItem != null) {
            dialogRequest.before = lastItem.id;
        }
        dialogRequest.limit = FeedAdapter.LIMIT;
        dialogRequest.unread = mDoubleButton.isRightButtonChecked();
        dialogRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                final FeedList<Dialog> dialogList = Dialog.parse(response);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        CacheProfile.unread_messages = 0;
                        if (isHistoryLoad) {
                            mListAdapter.addData(dialogList);
                        }
                        else {
                            mListAdapter.setData(dialogList);
                        }
                        onUpdateSuccess(isPushUpdating || isHistoryLoad);
                        mListView.onRefreshComplete();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        if (isHistoryLoad) {
                            mListAdapter.showRetryItem();
                        }
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        onUpdateFail(isPushUpdating || isHistoryLoad);
                        mListView.onRefreshComplete();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
                    }
                });
            }
        }).exec();
    }

}

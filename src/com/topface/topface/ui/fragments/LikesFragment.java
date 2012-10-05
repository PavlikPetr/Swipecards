package com.topface.topface.ui.fragments;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedLikesRequest;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.LikesListAdapter;
import com.topface.topface.utils.CacheProfile;

public class LikesFragment extends FeedFragment<LikesListAdapter> {
    @Override
    protected boolean isHasUnread() {
        return CacheProfile.unread_likes > 0;
    }

    @Override
    protected int getTitle() {
        return R.string.dashbrd_btn_likes;
    }

    @Override
    protected LikesListAdapter getAdapter() {
        return new LikesListAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.likes_background_text;
    }

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.likes_back_icon);
    }

    @Override
    protected void updateData(final boolean isPushUpdating, final boolean isHistoryLoad) {
        mIsUpdating = true;
        onUpdateStart(isPushUpdating || isHistoryLoad);

        FeedLikesRequest likesRequest = new FeedLikesRequest(getActivity().getApplicationContext());
        registerRequest(likesRequest);
        likesRequest.limit = FeedAdapter.LIMIT;
        likesRequest.unread = mDoubleButton.isRightButtonChecked();
        FeedLike lastLike = mListAdapter.getLastFeedItxem();
        if (isHistoryLoad && lastLike != null) {
            likesRequest.from = lastLike.id;
        }
        likesRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                final FeedList<FeedLike> likes = FeedLike.parse(response);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        if (isHistoryLoad) {
                            mListAdapter.addData(likes);
                        }
                        else {
                            mListAdapter.setData(likes);
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
                        onUpdateFail(isPushUpdating || isHistoryLoad);
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mListView.onRefreshComplete();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
                        if (isHistoryLoad) {
                            mListAdapter.showRetryItem();
                        }
                    }
                });
            }
        }).exec();
    }

}

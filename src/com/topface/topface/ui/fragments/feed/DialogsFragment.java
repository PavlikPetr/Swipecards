package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DialogDeleteRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.adapters.DialogListAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import org.json.JSONObject;

public class DialogsFragment extends FeedFragment<FeedDialog> {

    public DialogsFragment() {
        super();
    }

    @Override
    protected int getTitle() {
        return R.string.general_dialogs;
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.chat_background_text;
    }

    @Override
    protected void makeAllItemsRead() {

    }

    @Override
    protected void decrementCounters() {
        CountersManager.getInstance(App.getContext()).decrementCounter(CountersManager.DIALOGS);
    }

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.chat);
    }

    @Override
    protected DialogListAdapter getNewAdapter() {
        return new DialogListAdapter(getActivity().getApplicationContext(), getUpdaterCallback());
    }

    @Override
    protected FeedListData<FeedDialog> getFeedList(JSONObject data) {
        return new FeedListData<FeedDialog>(data, FeedDialog.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.DIALOGS;
    }

    @Override
    protected int getTypeForGCM() {
        return GCMUtils.GCM_TYPE_DIALOGS;
    }

    @Override
    protected int getTypeForCounters() {
        return CountersManager.DIALOGS;
    }

    @Override
    protected void onDeleteItem(final int position) {
        FeedDialog item = mListAdapter.getItem(position);
        new DialogDeleteRequest(item.user.id, getActivity())
                .callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        mLockView.setVisibility(View.GONE);
                        FeedList<FeedDialog> mFeedList = mListAdapter.getData();
                        mFeedList.remove(position);
                        mListAdapter.setData(mFeedList);
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        Debug.log(response.toString());
                        mLockView.setVisibility(View.GONE);
                        if (codeError != ApiResponse.PREMIUM_ACCESS_ONLY) {
                            Utils.showErrorMessage(getActivity());
                        }
                    }
                }).exec();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}

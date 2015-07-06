package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.view.View;

import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.History;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteDialogsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.DialogListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.json.JSONObject;

import java.util.List;

public class DialogsFragment extends FeedFragment<FeedDialog> {

    private boolean mNeedRefresh = false;

    public DialogsFragment() {
        super();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Проверяем флаг, нужно ли обновлять диалоги
        if (mNeedRefresh) {
            updateData(true, false);
            mNeedRefresh = false;
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_messages);
    }

    @Override
    protected void makeAllItemsRead() {
    }

    @Override
    protected DialogListAdapter createNewAdapter() {
        return new DialogListAdapter(getActivity().getApplicationContext(), getUpdaterCallback());
    }

    @Override
    protected FeedListData<FeedDialog> getFeedList(JSONObject data) {
        return new FeedListData<>(data, FeedDialog.class);
    }

    @Override
    protected void makeItemReadWithFeedId(String id) {
        //feed will be marked read in another method
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.DIALOGS;
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        inflated.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PurchasesActivity.createBuyingIntent("EmptyDialogs"));
            }
        });

        inflated.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuFragment.selectFragment(FragmentId.DATING);
            }
        });
    }

    /**
     * Этот метод используется для получения id элементов ленты при удалении.
     * Но в диалогах у нас работает не так как в остальных лентах
     * и приходится вручную пробрасывать id юзеров вместо id итема
     */
    @Override
    protected List<String> getSelectedFeedIds(FeedAdapter<FeedDialog> adapter) {
        return adapter.getSelectedUsersStringIds();
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_dialogs;
    }

    @Override
    protected int[] getTypesForGCM() {
        return new int[]{GCMUtils.GCM_TYPE_DIALOGS, GCMUtils.GCM_TYPE_MESSAGE, GCMUtils.GCM_TYPE_GIFT};
    }

    @Override
    protected int getFeedType() {
        return CountersManager.DIALOGS;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        return new DeleteDialogsRequest(ids, getActivity());
    }

    @Override
    protected int getUnreadCounter() {
        // dialogs are not auto-read
        return mCountersData.dialogs;
    }

    @Override
    protected String getGcmUpdateAction() {
        return GCMUtils.GCM_DIALOGS_UPDATE;
    }

    @Override
    protected boolean considerDublicates(FeedDialog first, FeedDialog second) {
        return first.user == null ? second.user == null : first.user.id == second.user.id;
    }

    @Override
    protected void onChatActivityResult(int resultCode, Intent data) {
        super.onChatActivityResult(resultCode, data);
        if (data != null) {
            History history = data.getParcelableExtra(ChatActivity.LAST_MESSAGE);
            int userId = data.getIntExtra(ChatActivity.LAST_MESSAGE_USER_ID, -1);
            if (history != null && userId > 0) {
                if (getListAdapter() instanceof DialogListAdapter) {
                    DialogListAdapter adapter = (DialogListAdapter) getListAdapter();
                    FeedDialog dialog;
                    for (int i = 0; i < adapter.getCount(); i++) {
                        dialog = adapter.getItem(i);
                        if (dialog.user != null && dialog.user.id == userId) {
                            adapter.replacePreview(i, history);
                        }
                    }
                }
            }
        }
    }
}

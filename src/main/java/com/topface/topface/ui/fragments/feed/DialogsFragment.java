package com.topface.topface.ui.fragments.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteDialogsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.DialogListAdapter;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.json.JSONObject;

import java.util.List;

public class DialogsFragment extends FeedFragment<FeedDialog> {

    public static final String REFRESH_DIALOGS = "refresh_dialogs";

    private boolean mNeedRefresh = false;

    private BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mNeedRefresh = true;
        }
    };


    public DialogsFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRefreshReceiver, new IntentFilter(REFRESH_DIALOGS));
        return super.onCreateView(inflater, container, saved);
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
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRefreshReceiver);
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
    protected void makeItemReadWithId(String id) {
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        return new DeleteDialogsRequest(ids, getActivity());
    }

    @Override
    protected int getUnreadCounter() {
        // dialogs are not auto-read
        return 0;
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_feed_filtered;
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
    public PageInfo.PageName getPageName() {
        return PageInfo.PageName.DIALOGS;
    }
}

package com.topface.topface.ui.fragments.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.DialogListAdapter;
import com.topface.topface.utils.CountersManager;
import org.json.JSONObject;

public class DialogsFragment extends FeedFragment<FeedDialog> {

    public static final String UPDATE_DIALOGS = "update_dialogs";
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateData(false, true);
        }
    };


    public DialogsFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, new IntentFilter(UPDATE_DIALOGS));
        return super.onCreateView(inflater, container, saved);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateReceiver);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_dialogs);
    }

    @Override
    protected void makeAllItemsRead() {

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
    protected void initEmptyFeedView(View inflated) {
        inflated.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ContainerActivity.getBuyingIntent("EmptyDialogs"));
            }
        });

        inflated.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigationActivity.selectFragment(F_DATING);
            }
        });
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_dialogs;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected int getContextMenuLayoutRes() {
        return R.menu.feed_context_menu_dialogs;
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_feed_filtered;
    }
}

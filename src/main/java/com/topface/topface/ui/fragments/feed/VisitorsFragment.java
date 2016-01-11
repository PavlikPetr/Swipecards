package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.R;
import com.topface.topface.data.Visitor;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteVisitorsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.VisitorsListAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.config.FeedsCache;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;


public class VisitorsFragment extends NoFilterFeedFragment<Visitor> {

    @Override
    protected String getTitle() {
        return getString(R.string.general_visitors);
    }

    @Override
    protected Type getFeedListDataType() {
        return new TypeToken<FeedList<Visitor>>() {
        }.getType();
    }

    @Override
    protected Class getFeedListItemClass() {
        return Visitor.class;
    }

    @Override
    protected int getFeedType() {
        return CountersManager.VISITORS;
    }

    @NotNull
    @Override
    protected FeedsCache.FEEDS_TYPE getFeedsType() {
        return FeedsCache.FEEDS_TYPE.DATA_VISITORS_FEEDS;
    }

    @Override
    protected FeedAdapter<Visitor> createNewAdapter() {
        return new VisitorsListAdapter(getActivity(), getUpdaterCallback());
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.VISITORS;
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        View btnBuyVip = inflated.findViewById(R.id.btnBuyVip);
        if (CacheProfile.premium) {
            ((TextView) inflated.findViewById(R.id.tvText)).setText(R.string.go_dating_message);
            ((Button) btnBuyVip).setText(R.string.general_get_dating);
            btnBuyVip.setVisibility(View.VISIBLE);

        } else {
            inflated.findViewById(R.id.tvText).setVisibility(View.VISIBLE);
            btnBuyVip.setVisibility(View.VISIBLE);
        }
        btnBuyVip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CacheProfile.premium) {
                    MenuFragment.selectFragment(BaseFragment.DATING);
                } else {
                    Intent intent = PurchasesActivity.createVipBuyIntent(null, "Visitors");
                    startActivityForResult(intent, PurchasesActivity.INTENT_BUY_VIP);
                }
            }
        });
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_visitors;
    }

    @Override
    protected int[] getTypesForGCM() {
        return new int[]{GCMUtils.GCM_TYPE_GUESTS};
    }

    @Override
    protected boolean isForPremium() {
        return true;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        return new DeleteVisitorsRequest(ids, getActivity());
    }

    @Override
    protected int getUnreadCounter() {
        return mCountersData.visitors;
    }

    @Override
    protected String getGcmUpdateAction() {
        return GCMUtils.GCM_GUESTS_UPDATE;
    }

}

package com.topface.topface.ui.fragments.feed;

import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.R;
import com.topface.topface.data.UnlockFunctionalityOption;
import com.topface.topface.data.Visitor;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteVisitorsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.VisitorsListAdapter;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.config.FeedsCache;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;


public class VisitorsFragment extends NoFilterFeedFragment<Visitor> {

    public static final String UNLOCK_FUCTIONALITY_TYPE = "visitors";
    public static final String SCREEN_TYPE = "Visitors";

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
    protected void initLockedFeed(View inflated, int errorCode) {
        initGagView(inflated, R.string.with_vip_find_your_visitors, R.string.buying_vip_status, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(PurchasesActivity.createVipBuyIntent(null, SCREEN_TYPE), PurchasesActivity.INTENT_BUY_VIP);
            }
        });
        Button button = getUnlockButtonView(inflated);
        button.setVisibility(View.VISIBLE);
        setUnlockButtonView(button);
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        initGagView(inflated, R.string.go_dating_message, R.string.general_get_dating, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuFragment.selectFragment(FragmentId.DATING.getFragmentSettings());
            }
        });
        Button button = getUnlockButtonView(inflated);
        button.setVisibility(View.GONE);
    }

    private Button getUnlockButtonView(View view) {
        return (Button) view.findViewById(R.id.btnUnlock);
    }

    private void initGagView(@NotNull View inflated, @StringRes int text, @StringRes int buttonText, View.OnClickListener listener) {
        Button btnBuyVip = (Button) inflated.findViewById(R.id.btnBuyVip);
        TextView textView = (TextView) inflated.findViewById(R.id.tvText);
        textView.setText(text);
        btnBuyVip.setText(buttonText);
        btnBuyVip.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        btnBuyVip.setOnClickListener(listener);
    }

    @Override
    protected String getUnlockFunctionalityType() {
        return UNLOCK_FUCTIONALITY_TYPE;
    }

    @Override
    protected UnlockFunctionalityOption.UnlockScreenCondition getUnlockScreenCondition(UnlockFunctionalityOption data) {
        return data.getUnlockVisitorsCondition();
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

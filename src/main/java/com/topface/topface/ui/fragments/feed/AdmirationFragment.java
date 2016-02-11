package com.topface.topface.ui.fragments.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.data.UnlockFunctionalityOption;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteAdmirationsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.FeedsCache;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdmirationFragment extends LikesFragment {

    public static final String UNLOCK_FUCTIONALITY_TYPE = "admirations";
    public static final String SCREEN_TYPE = "Admirations";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        return super.onCreateView(inflater, container, saved);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_sympathies);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mEmptyFeedView != null) {
            initEmptyFeedView(mEmptyFeedView, ErrorCodes.RESULT_OK);
        }
    }

    @Override
    protected void initLockedFeed(View inflated, int errorCode) {
        setEmptyFeedView(inflated);
        if (mCountersData.admirations > 0) {
            ((ViewFlipper) inflated.findViewById(R.id.vfEmptyViews)).setDisplayedChild(SECOND_CHILD);
            int curCounter = mCountersData.admirations;
            if (curCounter == 0) {
                curCounter = CacheProfile.getOptions().premiumAdmirations.getCount();
            }

            ((TextView) inflated.findViewById(R.id.tvTitle)).setText(Utils.getQuantityString(R.plurals.popup_vip_admirations, curCounter, curCounter));
            inflated.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(PurchasesActivity.createVipBuyIntent(null, SCREEN_TYPE), PurchasesActivity.INTENT_BUY_VIP);
                }
            });
            ((ImageViewRemote) inflated.findViewById(R.id.ivOne))
                    .setResourceSrc(CacheProfile.dating.sex == Profile.GIRL ? R.drawable.likes_male_one : R.drawable.likes_female_one);
            ((ImageViewRemote) inflated.findViewById(R.id.ivTwo))
                    .setResourceSrc(CacheProfile.dating.sex == Profile.GIRL ? R.drawable.likes_male_two : R.drawable.likes_female_two);
            ((ImageViewRemote) inflated.findViewById(R.id.ivThree))
                    .setResourceSrc(CacheProfile.dating.sex == Profile.GIRL ? R.drawable.likes_male_three : R.drawable.likes_female_three);
            setUnlockButtonView((Button) inflated.findViewById(R.id.btnUnlock));
        } else {
            chooseFirstChild(inflated);
        }
    }

    private void setEmptyFeedView(View emptyFeedView) {
        if (mEmptyFeedView == null) {
            mEmptyFeedView = emptyFeedView;
        }
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        setEmptyFeedView(inflated);
        chooseFirstChild(inflated);
    }

    @Override
    protected String getUnlockFunctionalityType() {
        return UNLOCK_FUCTIONALITY_TYPE;
    }

    @Override
    protected UnlockFunctionalityOption.UnlockScreenCondition getUnlockScreenCondition(UnlockFunctionalityOption data) {
        return data.getUnlockAdmirationCondition();
    }

    private void chooseFirstChild(View view) {
        ((ViewFlipper) view.findViewById(R.id.vfEmptyViews)).setDisplayedChild(FIRST_CHILD);
        view.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PurchasesActivity.createBuyingIntent("EmptyAdmirations"));
            }
        });
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.ADMIRATIONS;
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_admirations;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        return new DeleteAdmirationsRequest(ids, getActivity());
    }

    @Override
    protected int getUnreadCounter() {
        return mCountersData.admirations;
    }

    @Override
    protected void makeAllItemsRead() {
        baseMakeAllItemsRead();
    }

    @Override
    protected boolean isReadFeedItems() {
        return false;
    }

    @NotNull
    @Override
    protected FeedsCache.FEEDS_TYPE getFeedsType() {
        return FeedsCache.FEEDS_TYPE.DATA_ADMIRATION_FEEDS;
    }
}

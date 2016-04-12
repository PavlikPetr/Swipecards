package com.topface.topface.ui.fragments.feed;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.data.UnlockFunctionalityOption;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteAdmirationsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.FeedsCache;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdmirationFragment extends LikesFragment {

    public static final String UNLOCK_FUCTIONALITY_TYPE = "admirations";
    public static final String SCREEN_TYPE = "Admirations";
    private ViewFlipper mStubFlipper;
    private final String FLIPPER_CHILD_POSITION = "flipper_child_position";
    private int mFlipperPos = 1;

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
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void initLockedFeed(View inflated, int errorCode) {
        setEmptyFeedView(inflated);
        if (mCountersData.admirations > 0) {
            mStubFlipper = ((ViewFlipper) inflated.findViewById(R.id.vfEmptyViews));
            mStubFlipper.setDisplayedChild(SECOND_CHILD);
            setUnlockButtonView(getUnlockButtonView(SECOND_CHILD));
            int curCounter = mCountersData.admirations;
            if (curCounter == 0) {
                curCounter = App.get().getOptions().premiumAdmirations.getCount();
            }

            ((TextView) inflated.findViewById(R.id.tvTitle)).setText(Utils.getQuantityString(R.plurals.popup_vip_admirations, curCounter, curCounter));
            inflated.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(PurchasesActivity.createVipBuyIntent(null, SCREEN_TYPE), PurchasesActivity.INTENT_BUY_VIP);
                }
            });
            Profile profile = App.get().getProfile();
            ((ImageViewRemote) inflated.findViewById(R.id.ivOne))
                    .setResourceSrc(profile.dating.sex == Profile.GIRL ? R.drawable.likes_male_one : R.drawable.likes_female_one);
            ((ImageViewRemote) inflated.findViewById(R.id.ivTwo))
                    .setResourceSrc(profile.dating.sex == Profile.GIRL ? R.drawable.likes_male_two : R.drawable.likes_female_two);
            ((ImageViewRemote) inflated.findViewById(R.id.ivThree))
                    .setResourceSrc(profile.dating.sex == Profile.GIRL ? R.drawable.likes_male_three : R.drawable.likes_female_three);
        } else {
            setUnlockButtonView(getUnlockButtonView(FIRST_CHILD));
            chooseFirstChild(inflated);
        }
    }

    private void setEmptyFeedView(View emptyFeedView) {
        if (mEmptyFeedView == null) {
            mEmptyFeedView = emptyFeedView;
        }
    }

    private Button getUnlockButtonView(int child) {
        return (Button) mStubFlipper.getChildAt(child).findViewWithTag("btnUnlock");
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        setEmptyFeedView(inflated);
        getUnlockButtonView(FIRST_CHILD).setVisibility(View.GONE);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null && mStubFlipper != null) {
            outState.putInt(FLIPPER_CHILD_POSITION, mStubFlipper.getDisplayedChild());
        }
    }

    @Override
    protected void restoreInstanceState(Bundle saved) {
        super.restoreInstanceState(saved);
        if (saved != null) {
            mFlipperPos = saved.getInt(FLIPPER_CHILD_POSITION);
        }
    }

    private void chooseFirstChild(View view) {
        if (mCountersData.admirations > 0 && mBalanceData.premium) {
            mStubFlipper.setVisibility(View.GONE);
        } else {
            mStubFlipper.setDisplayedChild(mFlipperPos);
            view.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(PurchasesActivity.createBuyingIntent("EmptyAdmirations", App.get().getOptions().topfaceOfferwallRedirect));
                }
            });
        }
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

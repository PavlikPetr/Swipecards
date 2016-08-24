package com.topface.topface.ui.fragments.feed;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.UnlockFunctionalityOption;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteAdmirationsRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.FeedsCache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@FlurryOpenEvent(name = AdmirationFragment.SCREEN_TYPE)
public class AdmirationFragment extends LikesFragment {

    public static final String UNLOCK_FUCTIONALITY_TYPE = "admirations";
    public static final String SCREEN_TYPE = "Admirations";
    @Nullable
    private ViewFlipper mStubFlipper;

    @Override
    protected String getTitle() {
        return getString(R.string.general_sympathies);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mEmptyFeedView != null) {
            if (mBalanceData.premium) {
                initEmptyFeedView(mEmptyFeedView, ErrorCodes.RESULT_OK);
            } else {
                onLockedFeed(ErrorCodes.RESULT_OK);
            }
        }
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void initLockedFeed(View inflated, int errorCode) {
        initFlipper(inflated);
        setEmptyFeedView(inflated);
        chooseFlipperView(SECOND_CHILD, inflated);
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        initFlipper(inflated);
        setEmptyFeedView(inflated);
        chooseFlipperView(FIRST_CHILD, inflated);
    }

    private void chooseFlipperView(@FlipperChild final int child, View inflated) {
        if (mStubFlipper != null && child == FIRST_CHILD) {
            if (mCountersData.getAdmirations() > 0) {
                if (mBalanceData.premium) {
                    mStubFlipper.setVisibility(View.GONE);
                }
                return;
            }
            mStubFlipper.setVisibility(View.VISIBLE);
        }
        View.OnClickListener buttonClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (child) {
                    case FIRST_CHILD:
                        startActivity(PurchasesActivity.createBuyingIntent("EmptyAdmirations", App.get().getOptions().topfaceOfferwallRedirect));
                        break;
                    case SECOND_CHILD:
                        startActivityForResult(PurchasesActivity.createVipBuyIntent(null, SCREEN_TYPE), PurchasesActivity.INTENT_BUY_VIP);
                        break;
                }
            }
        };
        setupFlipperView(child, inflated, buttonClick);
    }

    private void setupFlipperView(@FlipperChild int child, @NotNull View inflated, @NotNull View.OnClickListener buttonClick) {
        switch (child) {
            case FIRST_CHILD:
                if (mStubFlipper != null) {
                    getUnlockButtonView(FIRST_CHILD).setVisibility(View.GONE);
                    mStubFlipper.setDisplayedChild(FIRST_CHILD);
                }
                inflated.findViewById(R.id.btnStartRate).setOnClickListener(buttonClick);
                break;
            case SECOND_CHILD:
                if (mStubFlipper != null) {
                    mStubFlipper.setDisplayedChild(SECOND_CHILD);
                }
                setUnlockButtonView(getUnlockButtonView(SECOND_CHILD));
                int admirations = mCountersData.getAdmirations();
                Options.PromoPopupEntity premiumAdmirations = App.get().getOptions().premiumAdmirations;
                int curCounter = admirations != 0 ? admirations : premiumAdmirations != null ? premiumAdmirations.getCount() : 0;
                if (curCounter != 0) {
                    ((TextView) inflated.findViewById(R.id.tvTitle)).setText(Utils.getQuantityString(R.plurals.popup_vip_admirations, curCounter, curCounter));
                }
                inflated.findViewById(R.id.btnBuyVip).setOnClickListener(buttonClick);
                Profile profile = App.get().getProfile();
                ((ImageViewRemote) inflated.findViewById(R.id.ivOne))
                        .setResourceSrc(profile.dating.sex == Profile.GIRL ? R.drawable.likes_female_one : R.drawable.likes_male_one);
                ((ImageViewRemote) inflated.findViewById(R.id.ivTwo))
                        .setResourceSrc(profile.dating.sex == Profile.GIRL ? R.drawable.likes_female_two : R.drawable.likes_male_two);
                ((ImageViewRemote) inflated.findViewById(R.id.ivThree))
                        .setResourceSrc(profile.dating.sex == Profile.GIRL ? R.drawable.likes_female_three : R.drawable.likes_male_three);
                break;
        }
    }

    private void initFlipper(View view) {
        if (mStubFlipper == null) {
            mStubFlipper = ((ViewFlipper) view.findViewById(R.id.vfEmptyViews));
        }

    }

    private void setEmptyFeedView(View emptyFeedView) {
        if (mEmptyFeedView == null) {
            mEmptyFeedView = emptyFeedView;
        }
    }

    private Button getUnlockButtonView(@FlipperChild int child) {
        initFlipper(mEmptyFeedView);
        return (Button) (mStubFlipper != null ? mStubFlipper.getChildAt(child).findViewWithTag("btnUnlock") : null);
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
        return mCountersData.getAdmirations();
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

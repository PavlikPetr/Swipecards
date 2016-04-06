package com.topface.topface.ui.fragments.feed;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.CountersData;
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

    @Override
    protected String getTitle() {
        return getString(R.string.general_sympathies);
    }

    @Override
    protected void updateCounters(CountersData countersData) {
        super.updateCounters(countersData);
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
        initFlipper(inflated);
        setEmptyFeedView(inflated);
        chooseFlipperView(inflated);
    }

    @Override
    protected void initEmptyFeedView(View inflated, int errorCode) {
        setEmptyFeedView(inflated);
        chooseFlipperView(inflated);
    }

    private void chooseFlipperView(View inflated) {
        @FlipperChild int pos = FIRST_CHILD;
        View.OnClickListener buttonClick;
        //Vip, есть восхищения - показываем восхищения
        if (mCountersData.admirations > 0 && mBalanceData.premium) {
            mStubFlipper.setVisibility(View.GONE);
            return;
        }
        //Есть Vip, но нет восхищений. Отправляем на покупку симпатий
        if (mBalanceData.premium) {
            buttonClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(PurchasesActivity.createBuyingIntent("EmptyAdmirations", App.get().getOptions().topfaceOfferwallRedirect));
                }
            };
        } else {
            //нет Vip нельзя смотреть восхищения. заглушка с просьбой купить Vip
            pos = SECOND_CHILD;
            buttonClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(PurchasesActivity.createVipBuyIntent(null, SCREEN_TYPE), PurchasesActivity.INTENT_BUY_VIP);
                }
            };
        }
        setupFlipperView(pos, inflated, buttonClick);
    }

    private void setupFlipperView(@FlipperChild int child, @NotNull View inflated, @NotNull View.OnClickListener buttonClick) {
        switch (child) {
            case FIRST_CHILD:
                getUnlockButtonView(FIRST_CHILD).setVisibility(View.GONE);
                mStubFlipper.setDisplayedChild(FIRST_CHILD);
                inflated.findViewById(R.id.btnStartRate).setOnClickListener(buttonClick);
                break;
            case SECOND_CHILD:
                mStubFlipper.setDisplayedChild(SECOND_CHILD);
                setUnlockButtonView(getUnlockButtonView(SECOND_CHILD));
                int curCounter = mCountersData.admirations != 0 ? mCountersData.admirations
                        : App.get().getOptions().premiumAdmirations.getCount();
                if (curCounter != 0) {
                    ((TextView) inflated.findViewById(R.id.tvTitle)).setText(Utils.getQuantityString(R.plurals.popup_vip_admirations, curCounter, curCounter));
                }
                inflated.findViewById(R.id.btnBuyVip).setOnClickListener(buttonClick);
                Profile profile = App.get().getProfile();
                ((ImageViewRemote) inflated.findViewById(R.id.ivOne))
                        .setResourceSrc(profile.dating.sex == Profile.GIRL ? R.drawable.likes_male_one : R.drawable.likes_female_one);
                ((ImageViewRemote) inflated.findViewById(R.id.ivTwo))
                        .setResourceSrc(profile.dating.sex == Profile.GIRL ? R.drawable.likes_male_two : R.drawable.likes_female_two);
                ((ImageViewRemote) inflated.findViewById(R.id.ivThree))
                        .setResourceSrc(profile.dating.sex == Profile.GIRL ? R.drawable.likes_male_three : R.drawable.likes_female_three);
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
        return (Button) mStubFlipper.getChildAt(child).findViewWithTag("btnUnlock");
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

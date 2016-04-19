package com.topface.topface.ui.fragments.feed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.gson.reflect.TypeToken;
import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.data.UnlockFunctionalityOption;
import com.topface.topface.requests.BuyLikesAccessRequest;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteLikesRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ReadLikeRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.LikesListAdapter;
import com.topface.topface.ui.adapters.LikesListAdapter.OnMutualListener;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.FlurryManager;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.ads.AdmobInterstitialUtils;
import com.topface.topface.utils.config.FeedsCache;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

import static com.topface.topface.utils.FlurryManager.LIKES_UNLOCK;

public class LikesFragment extends FeedFragment<FeedLike> {

    public static final String PREFERENCES_PAID_LIKES_COUNT = "paid_likes_count";
    public static final String UNLOCK_FUCTIONALITY_TYPE = "likes";


    @IntDef({FIRST_CHILD, SECOND_CHILD, THIRD_CHILD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FlipperChild {
    }

    public static final int FIRST_CHILD = 0;
    public static final int SECOND_CHILD = 1;
    public static final int THIRD_CHILD = 2;

    private static final String PAGE_NAME = "Likes";

    @Inject
    TopfaceAppState mAppState;
    protected View mEmptyFeedView;
    private RateController mRateController;
    private TextView mTitleWithCounter;
    protected BalanceData mBalanceData;
    private Action1<BalanceData> mBalanceAction = new Action1<BalanceData>() {
        @Override
        public void call(BalanceData balanceData) {
            mBalanceData = balanceData;
        }
    };
    private Subscription mBalanceSubscription;

    @Override
    protected boolean isReadFeedItems() {
        return true;
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(getActivity()).inject(this);
        mBalanceSubscription = mAppState.getObservable(BalanceData.class).subscribe(mBalanceAction);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected void updateCounters(CountersData countersData) {
        super.updateCounters(countersData);
        updateTitleWithCounter(countersData);
    }

    @Override
    protected Type getFeedListDataType() {
        return new TypeToken<FeedList<FeedLike>>() {
        }.getType();
    }

    @Override
    protected Class getFeedListItemClass() {
        return FeedLike.class;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBalanceSubscription.unsubscribe();
    }

    @Override
    protected void init() {
        mRateController = new RateController(getActivity(), SendLikeRequest.FROM_FEED);
    }

    @Override
    protected void makeAllItemsRead() {
        // likes are read by one
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_sympathies);
    }

    @Override
    protected LikesListAdapter createNewAdapter() {
        LikesListAdapter adapter = new LikesListAdapter(getActivity(), getUpdaterCallback());
        adapter.setOnMutualListener(new OnMutualListener() {

            @Override
            public void onMutual(FeedList<FeedLike> items) {
                if (items.size() > 0) {
                    for (FeedItem feedItem : items) {
                        feedItem.unread = false;
                    }
                    LikesFragment.this.onMutual(items);
                }
            }
        });
        return adapter;
    }

    @Override
    protected int[] getTypesForGCM() {
        return new int[]{GCMUtils.GCM_TYPE_LIKE};
    }

    @Override
    protected int getFeedType() {
        return CountersManager.LIKES;
    }

    @NotNull
    @Override
    protected FeedsCache.FEEDS_TYPE getFeedsType() {
        return FeedsCache.FEEDS_TYPE.DATA_LIKES_FEEDS;
    }

    private void onMutual(@NotNull FeedList<FeedLike> items) {
        if (items.size() > 0) {
            FeedLike item = items.getFirst();
            if (!(item.user.deleted || item.user.banned)) {
                for (FeedLike feedLike : items) {
                    if (!feedLike.mutualed) {
                        feedLike.mutualed = true;
                        getListAdapter().notifyDataSetChanged();
                    }
                }
                mRateController.onLike(item.user.id, 0, null, App.from(getActivity()).getOptions().blockUnconfirmed);
                Utils.showToastNotification(R.string.general_mutual, Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.LIKES;
    }

    private void setEmptyFeedView(View emptyFeedView) {
        if (mEmptyFeedView == null) {
            mEmptyFeedView = emptyFeedView;
        }
    }

    @Override
    protected void initLockedFeed(View inflated, int errorCode) {
        setEmptyFeedView(inflated);
        ViewFlipper viewFlipper = (ViewFlipper) inflated.findViewById(R.id.vfEmptyViews);
        switch (errorCode) {
            case ErrorCodes.PREMIUM_ACCESS_ONLY:
                setUnlockButtonView(getUnlockButtonView(inflated, SECOND_CHILD));
                initEmptyScreenOnLikesNeedVip(viewFlipper);
                break;
            case ErrorCodes.BLOCKED_SYMPATHIES:
                Button unlockButton = getUnlockButtonView(inflated, THIRD_CHILD);
                if (!App.get().getOptions().unlockAllForPremium) {
                    unlockButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.unlock_likes_by_coins_button_text_size));
                }
                setUnlockButtonView(unlockButton);
                initEmptyScreenOnBlockedLikes(inflated, viewFlipper);
                break;
        }
    }

    @Override
    protected void initEmptyFeedView(final View inflated, int errorCode) {
        setEmptyFeedView(inflated);
        initEmptyScreenWithoutLikes((ViewFlipper) inflated.findViewById(R.id.vfEmptyViews));
    }

    private Button getUnlockButtonView(View view, @FlipperChild int child) {
        return (Button) ((ViewFlipper) view.findViewById(R.id.vfEmptyViews)).getChildAt(child).findViewWithTag("btnUnlock");
    }

    @Override
    protected String getUnlockFunctionalityType() {
        return UNLOCK_FUCTIONALITY_TYPE;
    }

    @Override
    protected UnlockFunctionalityOption.UnlockScreenCondition getUnlockScreenCondition(UnlockFunctionalityOption data) {
        return data.getUnlockLikesCondition();
    }

    private void updateTitleWithCounter(CountersData countersData) {
        if (mTitleWithCounter != null) {
            String title = Utils.getQuantityString(
                    R.plurals.you_were_liked,
                    countersData.likes,
                    countersData.likes
            );
            mTitleWithCounter.setText(title);
        }
    }

    private void initEmptyScreenOnLikesNeedVip(ViewFlipper viewFlipper) {
        viewFlipper.setDisplayedChild(SECOND_CHILD);
        View currentView = viewFlipper.getChildAt(SECOND_CHILD);
        if (currentView != null) {
            mTitleWithCounter = (TextView) currentView.findViewById(R.id.tvTitle);
            updateTitleWithCounter(null);
            currentView.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = PurchasesActivity.createVipBuyIntent(null, "Likes");
                    startActivityForResult(intent, PurchasesActivity.INTENT_BUY_VIP);
                }
            });
            initAvatarImagesToEmptyView(currentView);
        }
    }

    private void initEmptyScreenWithoutLikes(ViewFlipper viewFlipper) {
        viewFlipper.setDisplayedChild(FIRST_CHILD);
        View currentView = viewFlipper.getChildAt(FIRST_CHILD);
        if (currentView != null) {
            currentView.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(PurchasesActivity.createBuyingIntent("EmptyLikes", App.from(getActivity()).getOptions().topfaceOfferwallRedirect));
                }
            });
        }
    }

    private void initEmptyScreenOnBlockedLikes(final View inflated, ViewFlipper viewFlipper) {
        final Options.BlockSympathy blockSympathyOptions = App.from(getActivity()).getOptions().blockSympathy;
        // send stat to google analytics
        sendBlockSympathyStatistics(blockSympathyOptions);
        // set paid likes view
        viewFlipper.setDisplayedChild(THIRD_CHILD);
        View currentView = viewFlipper.getChildAt(THIRD_CHILD);
        initAvatarImagesToEmptyView(currentView, blockSympathyOptions.showPhotos);
        if (currentView != null) {
            ((TextView) currentView.findViewById(R.id.blocked_likes_text)).setText(blockSympathyOptions.text);
            initBuyCoinsButton(inflated, blockSympathyOptions, currentView);
            initBuyVipButton(currentView, blockSympathyOptions);
        }
    }

    private void initBuyVipButton(View currentView, Options.BlockSympathy blockSympathyOptions) {
        Button btnBuy = (Button) currentView.findViewById(R.id.buy_vip_button);
        TextView buyText = (TextView) currentView.findViewById(R.id.buy_vip_text);
        if (App.from(getActivity()).getOptions().unlockAllForPremium) {
            initButtonForBlockedScreen(
                    buyText, blockSympathyOptions.textPremium,
                    btnBuy, blockSympathyOptions.buttonTextPremium,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(
                                    PurchasesActivity.createVipBuyIntent(null, "Likes"),
                                    PurchasesActivity.INTENT_BUY_VIP
                            );
                        }
                    }
            );
        } else {
            btnBuy.setVisibility(View.GONE);
            buyText.setVisibility(View.GONE);
        }
    }

    private void initBuyCoinsButton(final View inflated, final Options.BlockSympathy blockSympathyOptions, View currentView) {
        final Button btnBuy = (Button) currentView.findViewById(R.id.buy_coins_button);
        final ProgressBar progress = (ProgressBar) currentView.findViewById(R.id.prsLoading);
        initButtonForBlockedScreen(btnBuy, blockSympathyOptions.buttonText, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBalanceData.money >= blockSympathyOptions.price) {
                    btnBuy.setVisibility(View.INVISIBLE);
                    progress.setVisibility(View.VISIBLE);
                    EasyTracker.sendEvent(
                            getTrackName(), "VipPaidSympathies." + blockSympathyOptions.group,
                            "Buying", 1l
                    );
                    BuyLikesAccessRequest request = new BuyLikesAccessRequest(getActivity());
                    request.callback(new SimpleApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            FlurryManager.getInstance().sendSpendCoinsEvent(blockSympathyOptions.price, LIKES_UNLOCK);
                            inflated.setVisibility(View.GONE);
                            updateData(false, true);
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                            super.fail(codeError, response);
                            if (codeError == ErrorCodes.PAYMENT) {
                                openBuyScreenOnBlockedLikes(blockSympathyOptions);
                            }
                        }

                        @Override
                        public void always(IApiResponse response) {
                            super.always(response);
                            btnBuy.setVisibility(View.VISIBLE);
                            progress.setVisibility(View.GONE);
                        }
                    }).exec();
                } else {
                    openBuyScreenOnBlockedLikes(blockSympathyOptions);
                }
            }
        });
    }

    private void openBuyScreenOnBlockedLikes(Options.BlockSympathy blockSympathyOptions) {
        String group = blockSympathyOptions.group;
        EasyTracker.sendEvent(
                getTrackName(), "VipPaidSympathies." + group,
                "OpenBuyingScreen", 1l
        );
        startActivity(
                PurchasesActivity.createBuyingIntent(
                        "VipPaidSympathies." + group,
                        PurchasesFragment.TYPE_UNLOCK_SYMPATHIES,
                        blockSympathyOptions.price,
                        App.from(getActivity()).getOptions().topfaceOfferwallRedirect
                )
        );
    }

    private void sendBlockSympathyStatistics(final Options.BlockSympathy blockSympathyOptions) {
        new BackgroundThread() {
            @Override
            public void execute() {
                SharedPreferences prefs = getActivity()
                        .getSharedPreferences(App.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                final long showsCount = prefs.getLong(PREFERENCES_PAID_LIKES_COUNT, 1l);
                if (showsCount > 1l) {
                    EasyTracker.sendEvent(
                            getTrackName(), "VipPaidSympathies." + blockSympathyOptions.group,
                            "ShownOnce", 1l
                    );
                } else {
                    EasyTracker.sendEvent(
                            getTrackName(), "VipPaidSympathies." + blockSympathyOptions.group,
                            "ShownMoreThanOnce", showsCount
                    );
                }
                prefs.edit().putLong(PREFERENCES_PAID_LIKES_COUNT, showsCount + 1l)
                        .apply();
            }
        };
    }

    private void initAvatarImagesToEmptyView(View currentView) {
        initAvatarImagesToEmptyView(currentView, true);
    }

    private void initAvatarImagesToEmptyView(View currentView, boolean visible) {
        ImageViewRemote ivOne = (ImageViewRemote) currentView.findViewById(R.id.ivOne);
        ImageViewRemote ivTwo = (ImageViewRemote) currentView.findViewById(R.id.ivTwo);
        ImageViewRemote ivThree = (ImageViewRemote) currentView.findViewById(R.id.ivThree);
        Profile profile = App.from(getActivity()).getProfile();
        // if profile still not cached - show girls by default
        if (profile.dating != null && profile.dating.sex == Profile.GIRL) {
            ivOne.setResourceSrc(R.drawable.likes_male_one);
            ivTwo.setResourceSrc(R.drawable.likes_male_two);
            ivThree.setResourceSrc(R.drawable.likes_male_three);
        } else {
            ivOne.setResourceSrc(R.drawable.likes_female_one);
            ivTwo.setResourceSrc(R.drawable.likes_female_two);
            ivThree.setResourceSrc(R.drawable.likes_female_three);
        }
        int visibility = visible ? View.VISIBLE : View.GONE;
        ivOne.setVisibility(visibility);
        ivTwo.setVisibility(visibility);
        ivThree.setVisibility(visibility);
    }

    @Override
    public void onAvatarClick(FeedLike item, View view) {
        super.onAvatarClick(item, view);
        sendLikeReadRequest(getSenderId(item));
        showInterstitial();
    }

    @Override
    protected void onFeedItemClick(FeedItem item) {
        super.onFeedItemClick(item);
        sendLikeReadRequest(getSenderId(item));
        showInterstitial();
    }

    private int getSenderId(FeedItem item) {
        return item != null && !item.isAd() && item.user != null ? item.user.id : 0;
    }

    private void sendLikeReadRequest(int senderId) {
        new ReadLikeRequest(getActivity(), senderId, AdmobInterstitialUtils.canShowInterstitialAds()).exec();
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_likes;
    }

    @Override
    protected boolean isForPremium() {
        return true;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        return new DeleteLikesRequest(ids, getActivity());
    }

    @Override
    protected int getUnreadCounter() {
        return mCountersData.likes;
    }

    @Override
    protected String getGcmUpdateAction() {
        return GCMUtils.GCM_LIKE_UPDATE;
    }

    private void showInterstitial() {
        if (getFeedType() == CountersManager.LIKES) {
            AdmobInterstitialUtils.requestPreloadedInterstitial(getActivity(), App.from(getActivity()).getOptions().interstitial);
        }
    }
}

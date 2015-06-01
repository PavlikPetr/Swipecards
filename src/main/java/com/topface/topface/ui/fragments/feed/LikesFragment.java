package com.topface.topface.ui.fragments.feed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Options;
import com.topface.topface.data.experiments.SixCoinsSubscribeExperiment;
import com.topface.topface.requests.BuyLikesAccessRequest;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteLikesRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ReadLikeRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.LikesListAdapter;
import com.topface.topface.ui.adapters.LikesListAdapter.OnMutualListener;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.TransparentMarketFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.ads.AdmobInterstitialUtils;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.json.JSONObject;

import java.util.List;

public class LikesFragment extends FeedFragment<FeedLike> {

    protected View mEmptyFeedView;
    private RateController mRateController;
    private TextView mTitleWithCounter;
    private BroadcastReceiver mCountersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTitleWithCounter();
        }
    };

    @Override
    protected boolean isReadFeedItems() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mCountersReceiver, new IntentFilter(CountersManager.UPDATE_COUNTERS));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity())
                .unregisterReceiver(mCountersReceiver);
    }

    @Override
    protected void init() {
        mRateController = new RateController(getActivity(), SendLikeRequest.Place.FROM_FEED);
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
            public void onMutual(FeedItem item) {
                item.unread = false;
                LikesFragment.this.onMutual(item);
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

    private void onMutual(FeedItem item) {
        if (!(item.user.deleted || item.user.banned)) {
            if (item instanceof FeedLike) {
                if (!((FeedLike) item).mutualed) {
                    mRateController.onLike(item.user.id, 0, null);
                    ((FeedLike) item).mutualed = true;
                    getListAdapter().notifyDataSetChanged();
                    Utils.showToastNotification(R.string.general_mutual, Toast.LENGTH_SHORT);
                }
            }
        }
    }

    @Override
    protected FeedListData<FeedLike> getFeedList(JSONObject response) {
        return new FeedListData<>(response, FeedLike.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.LIKES;
    }

    @Override
    protected void initEmptyFeedView(final View inflated, int errorCode) {
        if (mEmptyFeedView == null) {
            mEmptyFeedView = inflated;
        }
        ViewFlipper viewFlipper = (ViewFlipper) inflated.findViewById(R.id.vfEmptyViews);
        switch (errorCode) {
            case ErrorCodes.PREMIUM_ACCESS_ONLY:
                initEmptyScreenOnLikesNeedVip(viewFlipper);
                break;
            case ErrorCodes.BLOCKED_SYMPATHIES:
                initEmptyScreenOnBlockedLikes(inflated, viewFlipper);
                break;
            default:
                initEmptyScreenWithoutLikes(viewFlipper);
        }
    }

    private void updateTitleWithCounter() {
        if (mTitleWithCounter != null) {
            String title = Utils.getQuantityString(
                    R.plurals.you_were_liked,
                    CacheProfile.unread_likes,
                    CacheProfile.unread_likes
            );
            mTitleWithCounter.setText(title);
        }
    }

    private void initEmptyScreenOnLikesNeedVip(ViewFlipper viewFlipper) {
        viewFlipper.setDisplayedChild(1);
        View currentView = viewFlipper.getChildAt(1);
        if (currentView != null) {
            mTitleWithCounter = (TextView) currentView.findViewById(R.id.tvTitle);
            updateTitleWithCounter();
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
        viewFlipper.setDisplayedChild(0);
        View currentView = viewFlipper.getChildAt(0);
        if (currentView != null) {
            currentView.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(PurchasesActivity.createBuyingIntent("EmptyLikes"));
                }
            });
        }
    }

    private void initEmptyScreenOnBlockedLikes(final View inflated, ViewFlipper viewFlipper) {
        final Options.BlockSympathy blockSympathyOptions = CacheProfile.getOptions().blockSympathy;
        // send stat to google analytics
        sendBlockSympathyStatistics(blockSympathyOptions);
        // set paid likes view
        viewFlipper.setDisplayedChild(2);
        View currentView = viewFlipper.getChildAt(2);
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
        if (CacheProfile.getOptions().unlockAllForPremium) {
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
        final SixCoinsSubscribeExperiment experiment = CacheProfile.getOptions().sixCoinsSubscribeExperiment;
        initButtonForBlockedScreen(btnBuy, experiment.isEnabled ? experiment.buttonText : blockSympathyOptions.buttonText,
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (experiment.isEnabled) {
                    Fragment f = getChildFragmentManager().findFragmentByTag(TransparentMarketFragment.class.getSimpleName());
                    final TransparentMarketFragment fragment = f == null ? new TransparentMarketFragment() : (TransparentMarketFragment) f;
                    fragment.setOnPurchaseCompleteAction(new TransparentMarketFragment.onPurchaseCompleteAction() {
                        @Override
                        public void onPurchaseAction() {
                            if (isAdded()) {
                                updateData(false, true);
                                getActivity().getSupportFragmentManager().
                                        beginTransaction().remove(fragment).commit();
                            }
                        }
                    });
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .add(fragment, TransparentMarketFragment.class.getSimpleName()).commit();
                    return;
                }
                if (CacheProfile.money >= blockSympathyOptions.price) {
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
                        blockSympathyOptions.price
                )
        );
    }

    private void sendBlockSympathyStatistics(final Options.BlockSympathy blockSympathyOptions) {
        new BackgroundThread() {
            @Override
            public void execute() {
                SharedPreferences prefs = getActivity()
                        .getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                final long showsCount = prefs.getLong(Static.PREFERENCES_PAID_LIKES_COUNT, 1l);
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
                prefs.edit().putLong(Static.PREFERENCES_PAID_LIKES_COUNT, showsCount + 1l)
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

        // if profile still not cached - show girls by default
        if (CacheProfile.dating != null && CacheProfile.dating.sex == Static.GIRL) {
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
        sendLikeReadRequest(item.id);
        showInterstitial();
    }

    @Override
    protected void onFeedItemClick(FeedItem item) {
        super.onFeedItemClick(item);
        sendLikeReadRequest(item.id);
        showInterstitial();
    }

    private void sendLikeReadRequest(String id) {
        if (!TextUtils.isEmpty(id)) {
            ReadLikeRequest request = new ReadLikeRequest(getActivity(), Integer.valueOf(id), AdmobInterstitialUtils.canShowInterstitialAds());
            request.exec();
        }
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
        return CacheProfile.unread_likes;
    }

    @Override
    protected String getGcmUpdateAction() {
        return GCMUtils.GCM_LIKE_UPDATE;
    }

    private void showInterstitial() {
        if (getFeedType() == CountersManager.LIKES) {
            AdmobInterstitialUtils.requestPreloadedInterstitial(getActivity());
        }
    }
}

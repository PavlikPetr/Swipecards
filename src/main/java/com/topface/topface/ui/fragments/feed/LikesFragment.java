package com.topface.topface.ui.fragments.feed;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Options;
import com.topface.topface.requests.BuyLikesAccessRequest;
import com.topface.topface.requests.DeleteFeedsRequest;
import com.topface.topface.requests.DeleteLikesRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.adapters.LikesListAdapter;
import com.topface.topface.ui.adapters.LikesListAdapter.OnMutualListener;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.BackgroundThread;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;

import org.json.JSONObject;

import java.util.List;

public class LikesFragment extends FeedFragment<FeedLike> {

    private RateController mRateController;
    protected View mEmptyFeedView;

    @Override
    protected void init() {
        mRateController = new RateController(getActivity(), SendLikeRequest.Place.FROM_FEED);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_likes);
    }

    @Override
    protected LikesListAdapter getNewAdapter() {
        LikesListAdapter adapter = new LikesListAdapter(getActivity(), getUpdaterCallback());
        adapter.setOnMutualListener(new OnMutualListener() {

            @Override
            public void onMutual(FeedItem item) {
                LikesFragment.this.onMutual(item);
            }
        });
        return adapter;
    }

    @Override
    protected int getTypeForGCM() {
        return GCMUtils.GCM_TYPE_LIKE;
    }

    @Override
    protected int getTypeForCounters() {
        return CountersManager.LIKES;
    }

    private void onMutual(FeedItem item) {
        if (!(item.user.deleted || item.user.banned)) {
            if (item instanceof FeedLike) {
                if (!((FeedLike) item).mutualed) {
                    mRateController.onLike(item.user.id, 0, null);
                    ((FeedLike) item).mutualed = true;
                    getListAdapter().notifyDataSetChanged();
                    Toast.makeText(getActivity(), R.string.general_mutual, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected FeedListData<FeedLike> getFeedList(JSONObject response) {
        return new FeedListData<FeedLike>(response, FeedLike.class);
    }

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.likes_back_icon);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.LIKES;
    }

    @Override
    protected void initEmptyFeedView(final View inflated, int errorCode) {
        if (mEmptyFeedView == null) mEmptyFeedView = inflated;
        ViewFlipper viewFlipper = (ViewFlipper) inflated.findViewById(R.id.vfEmptyViews);
        if (CacheProfile.premium) {
            final Options.BlockSympathy blockSympathyOptions = CacheProfile.getOptions().blockSympathy;
            if (blockSympathyOptions.enabled && errorCode == ErrorCodes.BLOCKED_SYMPATHIES) {
                initEmptyScreenOnBlockedLikes(inflated, viewFlipper, blockSympathyOptions);
            } else {
                initEmptyScreenWithoutLikes(viewFlipper);
            }
        } else {
            if (CacheProfile.unread_likes > 0) {
                if (errorCode == ErrorCodes.PREMIUM_ACCESS_ONLY) {
                    initEmptyScreenOnLikesNeedVip(viewFlipper);
                } else {
                    initEmptyScreenWithoutLikes(viewFlipper);
                }
            } else {
                initEmptyScreenWithoutLikes(viewFlipper);
            }
        }
    }

    private void initEmptyScreenOnLikesNeedVip(ViewFlipper viewFlipper) {
        viewFlipper.setDisplayedChild(1);
        View currentView = viewFlipper.getChildAt(1);
        if (currentView != null) {
            String title = Utils.getQuantityString(R.plurals.you_was_liked, CacheProfile.unread_likes, CacheProfile.unread_likes);
            ((TextView) currentView.findViewById(R.id.tvTitle)).setText(title);
            currentView.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = ContainerActivity.getVipBuyIntent(null, "Likes");
                    startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                }
            });
            currentView.findViewById(R.id.btnRate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MenuFragment.selectFragment(FragmentId.F_DATING);
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
                    startActivity(ContainerActivity.getBuyingIntent("EmptyLikes"));
                }
            });
        }
    }

    private void initEmptyScreenOnBlockedLikes(final View inflated, ViewFlipper viewFlipper, final Options.BlockSympathy blockSympathyOptions) {
        // send stat to google analytics
        sendBlockSympathyStatictics(blockSympathyOptions);
        // set paid likes view
        viewFlipper.setDisplayedChild(2);
        View currentView = viewFlipper.getChildAt(2);
        initAvatarImagesToEmptyView(currentView, blockSympathyOptions.showPhotos);
        if (currentView != null) {
            ((TextView) currentView.findViewById(R.id.tvText)).setText(blockSympathyOptions.text);
            final Button btnBuy = (Button) currentView.findViewById(R.id.btnVipBuyCoins);
            final ProgressBar progress = (ProgressBar) currentView.findViewById(R.id.prsLoading);
            btnBuy.setText(blockSympathyOptions.buttonText);
            btnBuy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (CacheProfile.money >= blockSympathyOptions.price) {
                        btnBuy.setVisibility(View.INVISIBLE);
                        progress.setVisibility(View.VISIBLE);
                        EasyTracker.getTracker().sendEvent(
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
                                    Toast.makeText(getActivity(), R.string.not_enough_coins, Toast.LENGTH_LONG).show();
                                    openBuyScreenOnBlockedLikes(blockSympathyOptions.group);
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
                        openBuyScreenOnBlockedLikes(blockSympathyOptions.group);
                    }
                }
            });
        }
    }

    private void openBuyScreenOnBlockedLikes(String group) {
        EasyTracker.getTracker().sendEvent(
                getTrackName(), "VipPaidSympathies." + group,
                "OpenBuyingScreen", 1l
        );
        startActivity(ContainerActivity.getBuyingIntent("VipPaidSympathies." + group));
    }

    private void sendBlockSympathyStatictics(final Options.BlockSympathy blockSympathyOptions) {
        new BackgroundThread() {
            @Override
            public void execute() {
                SharedPreferences prefs = getActivity()
                        .getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                final long showsCount = prefs.getLong(Static.PREFERENCES_PAID_LIKES_COUNT, 1l);
                if (showsCount > 1l) {
                    EasyTracker.getTracker().sendEvent(
                            getTrackName(), "VipPaidSympathies." + blockSympathyOptions.group,
                            "ShownOnce", 1l
                    );
                } else {
                    EasyTracker.getTracker().sendEvent(
                            getTrackName(), "VipPaidSympathies." + blockSympathyOptions.group,
                            "ShownMoreThanOnce", showsCount
                    );
                }
                prefs.edit().putLong(Static.PREFERENCES_PAID_LIKES_COUNT, showsCount + 1l)
                        .commit();
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
        ivOne.setResourceSrc(CacheProfile.dating.sex == Static.GIRL ?
                R.drawable.likes_male_one : R.drawable.likes_female_one);
        ivTwo.setResourceSrc(CacheProfile.dating.sex == Static.GIRL ?
                R.drawable.likes_male_two : R.drawable.likes_female_two);
        ivThree.setResourceSrc(CacheProfile.dating.sex == Static.GIRL ?
                R.drawable.likes_male_three : R.drawable.likes_female_three);
        int visibility = visible ? View.VISIBLE : View.GONE;
        ivOne.setVisibility(visibility);
        ivTwo.setVisibility(visibility);
        ivThree.setVisibility(visibility);
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
    protected void onCountersUpdated() {
        super.onCountersUpdated();
        if (mEmptyFeedView != null) {
            initEmptyFeedView(mEmptyFeedView);
        }
    }

    @Override
    protected int getContextMenuLayoutRes() {
        return R.menu.feed_context_menu;
    }

    @Override
    protected DeleteFeedsRequest getDeleteRequest(List<String> ids, Context context) {
        return new DeleteLikesRequest(ids, context);
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_feed_filtered;
    }
}

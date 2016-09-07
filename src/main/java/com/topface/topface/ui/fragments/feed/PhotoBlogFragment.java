package com.topface.topface.ui.fragments.feed;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.FeedPhotoBlog;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.Profile;
import com.topface.topface.databinding.HeaderPhotoBlogBinding;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteLikesRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.ui.AddToPhotoBlogActivity;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.statistics.TakePhotoStatistics;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.OwnProfileActivity;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.PhotoBlogListAdapter;
import com.topface.topface.ui.dialogs.TakePhotoPopup;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.RxUtils;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.adapter_utils.IInjectViewFactory;
import com.topface.topface.utils.adapter_utils.IViewInjectRule;
import com.topface.topface.utils.adapter_utils.InjectViewBucket;
import com.topface.topface.ui.fragments.feed.photoblog.HeaderPhotoBlogViewModel;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

public class PhotoBlogFragment extends FeedFragment<FeedPhotoBlog> {

    public final static int ADD_TO_PHOTO_BLOG_ACTIVITY_ID = 1;

    private static final String PAGE_NAME = "PhotoFeed";
    private static final int UPDATE_DELAY = 20;

    @Inject
    TopfaceAppState mAppState;
    private CompositeSubscription mSubscription = new CompositeSubscription();

    private RateController mRateController;
    private CountDownTimer mTimerUpdate;
    private PhotoBlogListAdapter mAdapter;
    private AddPhotoHelper mAddPhotoHelper;

    private HeaderPhotoBlogViewModel mViewModel;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AddPhotoHelper.handlePhotoMessage(msg);
        }
    };

    @Override
    protected Type getFeedListDataType() {
        return new TypeToken<FeedList<FeedPhotoBlog>>() {
        }.getType();
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    protected Class getFeedListItemClass() {
        return FeedPhotoBlog.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(getActivity()).inject(this);
        mSubscription.add(mAppState.getObservable(Profile.class)
                .subscribe(new Action1<Profile>() {
                    @Override
                    public void call(Profile profile) {
                        if (mViewModel != null) {
                            mViewModel.setUrlAvatar(profile);
                        }
                    }
                }));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FeedAdapter adapter = getListAdapter();
        if (adapter != null) {
            InjectViewBucket bucket = new InjectViewBucket(new IInjectViewFactory() {
                @Override
                public View construct(ViewGroup group) {
                    HeaderPhotoBlogBinding binding = DataBindingUtil.inflate((LayoutInflater) App.getContext()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE), R.layout.header_photo_blog, null, true);
                    /*binding.setClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startAddToLeaderActivity();
                        }
                    });

                    mViewModel = new HeaderPhotoBlogViewModel();*/
                    binding.setViewModel(mViewModel);
                    binding.executePendingBindings();
                    return binding.getRoot();
                }
            });
            bucket.addFilter(new IViewInjectRule() {
                @Override
                public boolean isNeedInject(int pos) {
                    return pos == 0;
                }
            });
            adapter.registerViewBucket(bucket);
        }
    }

    private void startAddToLeaderActivity() {
        if (!App.getConfig().getUserConfig().isUserAvatarAvailable() && App.get().getProfile().photo == null) {
            takePhoto();
        } else {
            startActivityForResult(new Intent(getActivity(), AddToPhotoBlogActivity.class), ADD_TO_PHOTO_BLOG_ACTIVITY_ID);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initTimer();
        setDeletable(false);
        if (mAdapter != null) {
            mAdapter.setSympathySentArray(App.getUserConfig().getSympathySentArray());
        }
    }

    @Override
    protected boolean isSwipeRefreshEnable() {
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimer();
        if (mAdapter != null) {
            App.getUserConfig().setSympathySentArray(mAdapter.getSympathySentArray());
            App.getUserConfig().saveConfig();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.safeUnsubscribe(mSubscription);
        mAdapter = null;

        if (mRateController != null) {
            mRateController.destroyController();
        }

        if (mAddPhotoHelper != null) {
            mAddPhotoHelper.releaseHelper();
        }
    }

    @Override
    protected void init() {
        mRateController = new RateController(this, SendLikeRequest.FROM_FEED);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_photoblog);
    }

    @Override
    protected PhotoBlogListAdapter createNewAdapter() {
        mAdapter = new PhotoBlogListAdapter(getActivity(), getUpdaterCallback());
        mAdapter.setOnSympathyListener(new PhotoBlogListAdapter.OnSympathySent() {

            @Override
            public void onSympathy(final FeedItem item) {
                mRateController.onLike(
                        item.user.id,
                        SendLikeRequest.DEFAULT_NO_MUTUAL,
                        new RateController.OnRateRequestListener() {
                            @SuppressWarnings("ConstantConditions")
                            @Override
                            public void onRateCompleted(int mutualId, int ratedUserId) {
                                if (getActivity() != null) {
                                    Utils.showToastNotification(R.string.sympathy_sended, Toast.LENGTH_SHORT);
                                }
                            }

                            @SuppressWarnings("ConstantConditions")
                            @Override
                            public void onRateFailed(int userId, int mutualId) {
                                if (mAdapter != null) {
                                    mAdapter.removeSympathySentId(item.user.id);
                                }
                                if (getActivity() != null) {
                                    Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT);
                                }
                            }
                        }, App.from(getActivity()).getOptions().blockUnconfirmed
                );
            }
        });
        mAdapter.setOnAvatarClickListener(this);
        return mAdapter;
    }

    public void onAvatarClick(FeedPhotoBlog item, View view) {
        if (isAdded()) {
            FeedAdapter<FeedPhotoBlog> adapter = getListAdapter();
            if (adapter.isMultiSelectionMode()) {
                adapter.onSelection(item);
            } else {
                if (isNotYourOwnId(item.user.id)) {
                    startActivity(UserProfileActivity.createIntent(null, item.user.photo, item.user.id, item.id, false, true, Utils.getNameAndAge(item.user.firstName, item.user.age), item.user.city.getName()));
                } else {
                    openOwnProfile();
                }
            }
        }
    }

    @Override
    protected void onFeedItemClick(FeedItem item) {
        if (isNotYourOwnId(item.user.id)) {
            if (!item.user.isEmpty()) {
                FeedUser user = item.user;
                Intent intent = ChatActivity.createIntent(user.id, user.sex, user.getNameAndAge(), user.city.name,
                        null, user.photo, false, null, user.banned);
                getActivity().startActivityForResult(intent, ChatActivity.REQUEST_CHAT);
            }
        } else {
            openOwnProfile();
        }
    }

    private void takePhoto() {
        if (mAddPhotoHelper == null) {
            mAddPhotoHelper = new AddPhotoHelper(PhotoBlogFragment.this, null);
            mAddPhotoHelper.setOnResultHandler(mHandler);
        }
        if (getActivity() != null) {
            TakePhotoPopup.newInstance(TakePhotoStatistics.PLC_ADD_TO_LEADER).show(getActivity().getSupportFragmentManager(), TakePhotoPopup.TAG);
        }
    }

    private boolean isNotYourOwnId(int id) {
        return App.from(getActivity()).getProfile().uid != id;
    }

    private void openOwnProfile() {
        getActivity().startActivity(new Intent(getActivity(), OwnProfileActivity.class));
    }


    @Override
    protected int[] getTypesForGCM() {
        return new int[]{-1};
    }

    @Override
    protected int getFeedType() {
        return CountersManager.UNKNOWN_TYPE;
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.PHOTOBLOG;
    }

    @Override
    protected void initLockedFeed(View inflated, int errorCode) {
    }

    @Override
    protected void initEmptyFeedView(final View inflated, int errorCode) {
        initEmptyScreenWithoutLikes((ViewFlipper) inflated.findViewById(R.id.vfEmptyViews));
    }

    private void initEmptyScreenWithoutLikes(ViewFlipper viewFlipper) {
        viewFlipper.setDisplayedChild(0);
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_photoblog;
    }

    @Override
    protected boolean isForPremium() {
        return false;
    }

    @Override
    protected DeleteAbstractRequest getDeleteRequest(List<String> ids) {
        return new DeleteLikesRequest(ids, getActivity());
    }

    @Override
    protected int getUnreadCounter() {
        return 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TO_PHOTO_BLOG_ACTIVITY_ID) {
            updatePhotoblogList();
        }
        if (mAddPhotoHelper != null) {
            mAddPhotoHelper.processActivityResult(requestCode, resultCode, data);
        }
    }

    private void initTimer() {
        stopTimer();
        mTimerUpdate = new CountDownTimer(UPDATE_DELAY * 1000, UPDATE_DELAY * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                updatePhotoblogList();
            }
        }.start();
    }

    private void stopTimer() {
        if (mTimerUpdate != null) {
            mTimerUpdate.cancel();
        }
    }

    @Override
    protected void processSuccessUpdate(FeedListData<FeedPhotoBlog> data, boolean isHistoryLoad, boolean isPullToRefreshUpdating, boolean makeItemsRead, int limit) {
        RetryViewCreator retryView = getRetryView();
        if (retryView.isVisible()) {
            retryView.setVisibility(View.GONE);
        }
        super.processSuccessUpdate(data, isHistoryLoad, isPullToRefreshUpdating, makeItemsRead, limit);
        initTimer();
    }

    private void updatePhotoblogList() {
        updateData(true, false);
    }

}

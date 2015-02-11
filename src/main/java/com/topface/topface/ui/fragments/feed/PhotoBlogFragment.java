package com.topface.topface.ui.fragments.feed;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.topface.PullToRefreshBase;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.FeedPhotoBlog;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.DeleteLikesRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.ui.AddToLeaderActivity;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.PhotoBlogListAdapter;
import com.topface.topface.ui.fragments.MenuFragment;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.RateController;

import org.json.JSONObject;

import java.util.List;

public class PhotoBlogFragment extends FeedFragment<FeedPhotoBlog> {

    private static final int UPDATE_DELAY = 20;

    protected View mEmptyFeedView;
    private RateController mRateController;
    private MenuItem mBarActions;
    private CountDownTimer mTimerUpdate;
    private PhotoBlogListAdapter mAdapter;

    @Override
    public void onResume() {
        super.onResume();
        initTimer();
        getListView().setMode(PullToRefreshBase.Mode.DISABLED);
        setDeletable(false);
        if (mAdapter != null) {
            mAdapter.setSympathySentArray(App.getUserConfig().getSympathySentArray());
        }
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
    protected void init() {
        mRateController = new RateController(getActivity(), SendLikeRequest.Place.FROM_FEED);
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
                            public void onRateCompleted(int mutualId) {
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), R.string.sympathy_sended, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @SuppressWarnings("ConstantConditions")
                            @Override
                            public void onRateFailed(int userId, int mutualId) {
                                if (mAdapter != null) {
                                    mAdapter.removeSympathySentId(item.user.id);
                                }
                                if (getActivity() != null) {
                                    Toast.makeText(getActivity(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
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
                    startActivity(CacheProfile.getOptions().autoOpenGallery.createIntent(item.user.id, item.user.photosCount, item.id, item.user.photo, getActivity()));
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
                Intent intent = ChatActivity.createIntent(getActivity(), item.user, item.id);
                getActivity().startActivityForResult(intent, ChatActivity.INTENT_CHAT);
            }
        } else {
            openOwnProfile();
        }
    }

    private boolean isNotYourOwnId(int id) {
        return CacheProfile.getProfile().uid != id;
    }

    private void openOwnProfile() {
        MenuFragment.selectFragment(FragmentId.PROFILE);
    }


    @Override
    protected int[] getTypesForGCM() {
        return new int[]{-1};
    }

    @Override
    protected int getTypeForCounters() {
        return -1;
    }

    @Override
    protected FeedListData<FeedPhotoBlog> getFeedList(JSONObject response) {
        return new FeedListData<>(response, FeedPhotoBlog.class);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.PHOTOBLOG;
    }

    @Override
    protected void initEmptyFeedView(final View inflated, int errorCode) {
        if (mEmptyFeedView == null) {
            mEmptyFeedView = inflated;
        }
        ViewFlipper viewFlipper = (ViewFlipper) inflated.findViewById(R.id.vfEmptyViews);
        initEmptyScreenWithoutLikes(viewFlipper);
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

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.action_add_leader;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_new_leader_photo:
                startActivityForResult(new Intent(getActivity(), AddToLeaderActivity.class), AddToLeaderActivity.ADD_TO_LEADER_ACTIVITY_ID);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem barActionsItem = menu.findItem(R.id.action_add_new_leader_photo);
        if (barActionsItem != null && mBarActions != null) {
            barActionsItem.setChecked(mBarActions.isChecked());
        }
        mBarActions = barActionsItem;
    }

    @Override
    protected int getEmptyFeedLayout() {
        return R.layout.layout_empty_likes;
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
        if (requestCode == AddToLeaderActivity.ADD_TO_LEADER_ACTIVITY_ID && resultCode == Activity.RESULT_OK) {
            updatePhotoblogList();
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

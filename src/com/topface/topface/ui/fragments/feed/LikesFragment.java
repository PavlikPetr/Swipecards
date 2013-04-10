package com.topface.topface.ui.fragments.feed;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.RateRequest;
import com.topface.topface.ui.adapters.LikesListAdapter;
import com.topface.topface.ui.adapters.LikesListAdapter.OnMutualListener;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.SwipeGestureListener;
import org.json.JSONObject;

public class LikesFragment extends FeedFragment<FeedLike> {

    private RateController mRateController;


    @Override
    protected void init() {
        mRateController = new RateController(getActivity());
    }

    @Override
    protected int getTitle() {
        return R.string.general_likes;
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
    protected OnItemClickListener getOnItemClickListener() {
        return null;
    }

    @Override
    protected int getTypeForGCM() {
        return GCMUtils.GCM_TYPE_LIKE;
    }

    @Override
    protected int getTypeForCounters() {
        return CountersManager.LIKES;
    }

    @Override
    protected OnTouchListener getListViewOnTouchListener() {
        final GestureDetector gd = new GestureDetector(getActivity().getApplicationContext(),
                new SwipeGestureListener(getActivity().getApplicationContext(), mListView.getRefreshableView(),
                        new SwipeGestureListener.SwipeListener() {

                            @Override
                            public void onSwipeR2L(int position) {
                                ((LikesListAdapter) mListAdapter).setSelectedForMutual(position);
                            }

                            @Override
                            public void onSwipeL2R(int position) {
                                ((LikesListAdapter) mListAdapter).setSelectedForMutual(-1);
                            }

                            @Override
                            public void onTap(int position) {
                                FeedItem item = (FeedItem) mListView.getRefreshableView().getItemAtPosition(position);
                                if (item != null) {
                                    if (!mIsUpdating && item.isRetrier()) {
                                        updateUI(new Runnable() {
                                            public void run() {
                                                mListAdapter.showLoaderItem();
                                            }
                                        });
                                        updateData(false, true);
                                    } else {
                                        try {
                                            onFeedItemClick(item);
                                        } catch (Exception e) {
                                            Debug.error("FeedItem click error:", e);
                                        }
                                    }
                                }
                            }
                        })
        );

        return new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gd.onTouchEvent(event);
            }
        };
    }

    protected DialogInterface.OnClickListener getLongTapActionsListener(final int position) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DELETE_BUTTON:
                        mLockView.setVisibility(View.VISIBLE);
                        onDeleteItem(position);
                        break;
                    case BLACK_LIST_BUTTON:
                        onAddToBlackList(position);
                        break;
                    case MUTUAL_BUTTON:
                        onMutual(position);
                        break;
                }
            }
        };
    }

    private void onMutual(int position) {
        onMutual(getItem(position));
    }

    private void onMutual(FeedItem item) {
        if(!(item.user.deleted || item.user.banned)) {
            if (item instanceof FeedLike) {
                if(!((FeedLike)item).mutualed) {
                    mRateController.onRate(item.user.id, RateController.MUTUAL_VALUE, 0, null);
                    ((FeedLike)item).mutualed = true;
                    getListAdapter().notifyDataSetChanged();
                    Toast.makeText(getActivity(),R.string.general_mutual,Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    protected String[] getLongTapActions() {
        if (editButtonsNames == null) {
            editButtonsNames = new String[]{getString(R.string.general_delete_title),
                    getString(R.string.black_list_add),getString(R.string.general_mutual)};
        }
        return editButtonsNames;
    }

        @Override
    protected FeedListData<FeedLike> getFeedList(JSONObject response) {
        return new FeedListData<FeedLike>(response, FeedLike.class);
    }

    @Override
    protected int getEmptyFeedText() {
        return R.string.likes_background_text;
    }

    @Override
    protected void decrementCounters() {
        CountersManager.getInstance(App.getContext()).decrementCounter(CountersManager.LIKES);
    }

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.likes_back_icon);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.LIKES;
    }

}

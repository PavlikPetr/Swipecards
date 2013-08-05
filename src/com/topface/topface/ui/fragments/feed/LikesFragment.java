package com.topface.topface.ui.fragments.feed;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.topface.topface.App;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedLike;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.LikesListAdapter;
import com.topface.topface.ui.adapters.LikesListAdapter.OnMutualListener;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.*;
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
        if (!(item.user.deleted || item.user.banned)) {
            if (item instanceof FeedLike) {
                if (!((FeedLike) item).mutualed) {
                    mRateController.onRate(item.user.id, RateController.MUTUAL_VALUE, 0, null);
                    ((FeedLike) item).mutualed = true;
                    getListAdapter().notifyDataSetChanged();
                    Toast.makeText(getActivity(), R.string.general_mutual, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    protected String[] getLongTapActions() {
        if (editButtonsNames == null) {
            editButtonsNames = new String[]{getString(R.string.general_delete_title),
                    getString(R.string.black_list_add), getString(R.string.general_mutual)};
        }
        return editButtonsNames;
    }

    @Override
    protected FeedListData<FeedLike> getFeedList(JSONObject response) {
        return new FeedListData<FeedLike>(response, FeedLike.class);
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

    @Override
    protected void initEmptyFeedView(View inflated) {
        if (CacheProfile.premium) {
            ((ViewFlipper) inflated.findViewById(R.id.vfEmptyViews)).setDisplayedChild(0);
            inflated.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(ContainerActivity.getBuyingIntent());
                }
            });
        } else {
            if (CacheProfile.unread_likes > 0) {
                ((ViewFlipper) inflated.findViewById(R.id.vfEmptyViews)).setDisplayedChild(1);
                String title = Utils.getQuantityString(R.plurals.you_was_liked, CacheProfile.unread_likes, CacheProfile.unread_likes);
                ((TextView) inflated.findViewById(R.id.tvTitle)).setText(title);
                inflated.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity().getApplicationContext(), ContainerActivity.class);
                        startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                    }
                });
                inflated.findViewById(R.id.btnRate).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NavigationActivity.selectFragment(F_DATING);
                    }
                });
                ((ImageViewRemote) inflated.findViewById(R.id.ivOne))
                        .setResourceSrc(CacheProfile.dating.sex == Static.GIRL ? R.drawable.likes_male_one : R.drawable.likes_female_one);
                ((ImageViewRemote) inflated.findViewById(R.id.ivTwo))
                        .setResourceSrc(CacheProfile.dating.sex == Static.GIRL ? R.drawable.likes_male_two : R.drawable.likes_female_two);
                ((ImageViewRemote) inflated.findViewById(R.id.ivThree))
                        .setResourceSrc(CacheProfile.dating.sex == Static.GIRL ? R.drawable.likes_male_three : R.drawable.likes_female_three);
            } else {
                ((ViewFlipper) inflated.findViewById(R.id.vfEmptyViews)).setDisplayedChild(0);
                inflated.findViewById(R.id.btnStartRate).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(ContainerActivity.getBuyingIntent());
                    }
                });
            }
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
    protected boolean isBlockOnClosing() {
        return true;
    }
}

package com.topface.topface.ui.fragments.gift;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.topface.topface.data.BasePendingInit;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.FeedGiftsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.IListLoader;

/**
 * Fragment displaying updatable gifts feed
 */
public class UpdatableGiftsFragment extends PlainGiftsFragment {

    private static final int GIFTS_LOAD_COUNT = 30;
    private static final String PROFILE_ID = "profile_id";

    private int mProfileId;
    private boolean mIsUpdating = false;
    private BasePendingInit<Profile> mPendingProfileInit = new BasePendingInit<>();

    @Override
    protected void postGiftsLoadInfoUpdate(Profile.Gifts gifts) {
        if (gifts != null && gifts.more) {
            addItem(new FeedGift(IListLoader.ItemType.LOADER));
        }
    }

    @Override
    protected FeedAdapter.Updater getUpdaterCallback() {
        return new FeedAdapter.Updater() {
            @Override
            public void onUpdate() {
                if (!mIsUpdating) {
                    onNewFeeds();
                }
            }
        };
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mGridAdapter != null) {
            outState.putInt(PROFILE_ID, mProfileId);
        }

    }

    @Override
    protected void restoreInstanceState(Bundle savedState) {
        super.restoreInstanceState(savedState);
        mProfileId = savedState.getInt(PROFILE_ID);
        if (!mIsUpdating) {
            onNewFeeds(mProfileId);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPendingProfileInit.setCanSet(true);
        if (mPendingProfileInit.getCanSet()) {
            setProfilePending(mPendingProfileInit.getData());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPendingProfileInit.setCanSet(false);
    }

    public void setProfile(Profile profile) {
        mPendingProfileInit.setData(profile);
        if (mPendingProfileInit.getCanSet()) {
            setProfilePending(mPendingProfileInit.getData());
        }

    }

    private void setProfilePending(Profile profile) {
        if (mProfileId != profile.uid || mGridAdapter.isEmpty()) {
            mProfileId = profile.uid;
            setGifts(profile.gifts);
        }
    }

    public int getProfileId() {
        return mProfileId;
    }

    public FeedList<FeedGift> getGifts() {
        return mGridAdapter.getData();
    }

    private void onNewFeeds() {
        onNewFeeds(mProfileId);
    }

    private void onNewFeeds(int userId) {
        mIsUpdating = true;
        final FeedGiftsRequest request = new FeedGiftsRequest(getActivity());
        request.limit = GIFTS_LOAD_COUNT;
        request.uid = userId;
        final FeedList<FeedGift> data = mGridAdapter.getData();
        if (!data.isEmpty()) {
            int updateShift = data.getLast().isLoader() || data.getLast().isRetrier() ? data.size() - 2 : data.size() - 1;
            request.from = data.get(updateShift).gift.feedId;
        }
        request.callback(new DataApiHandler<FeedListData<FeedGift>>() {

            @Override
            protected void success(FeedListData<FeedGift> gifts, IApiResponse response) {
                removeLoaderItem();
                if (request.from == 0) {
                    FeedList<FeedGift> noFeedIdGifts = new FeedList<>();
                    for (int i = getMinItemsCount(); i < data.size(); i++) {
                        FeedGift gift = data.get(i);
                        if (gift.gift.feedId == 0) {
                            noFeedIdGifts.add(gift);
                        }
                    }
                    data.removeAll(noFeedIdGifts);
                }
                data.addAll(gifts.items);
                if (!gifts.items.isEmpty()) {
                    mGroupInfo.setVisibility(View.GONE);
                    mTextInfo.setVisibility(View.GONE);
                    mTitle.setVisibility(View.GONE);
                }
                if (gifts.more) {
                    data.add(new FeedGift(IListLoader.ItemType.LOADER));
                }
                mGridAdapter.notifyDataSetChanged();
                mIsUpdating = false;
            }

            @Override
            protected FeedListData<FeedGift> parseResponse(ApiResponse response) {
                return new FeedListData<>(response.jsonResult, FeedGift.class);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                removeLoaderItem();
                data.add(new FeedGift(IListLoader.ItemType.RETRY));
                mGridAdapter.notifyDataSetChanged();
                mIsUpdating = false;
            }
        }).exec();
    }

    private void removeLoaderItem() {
        if (mGridAdapter.getData().size() > 0) {
            FeedGift last = mGridAdapter.getData().getLast();
            if (last.isLoader() || last.isRetrier()) {
                mGridAdapter.getData().remove(mGridAdapter.getData().size() - 1);
            }
        }
    }
}

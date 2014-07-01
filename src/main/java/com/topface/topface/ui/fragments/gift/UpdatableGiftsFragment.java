package com.topface.topface.ui.fragments.gift;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;

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

import java.util.ArrayList;

/**
 * Fragment displaying updatable gifts feed
 */
public class UpdatableGiftsFragment extends PlainGiftsFragment<Profile.Gifts> {

    private static final int GIFTS_LOAD_COUNT = 30;
    private static final String PROFILE_ID = "profile_id";
    private static final String DATA = "data";

    private Profile mProfile;
    private boolean mIsUpdating = false;

    @Override
    protected void postGiftsLoadInfoUpdate(Profile.Gifts gifts) {
        if (gifts != null && gifts.more) {
            addItem(new FeedGift(IListLoader.ItemType.LOADER));
        }
    }

    @Override
    protected void onGiftClick(AdapterView<?> parent, View view, int position, long id) {
        updateIfRetrier(position);
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
        if (mProfile != null) {
            FeedList<FeedGift> data = mGridAdapter.getData();
            outState.putParcelableArray(DATA, data.toArray(new FeedGift[data.size()]));
            outState.putInt(PROFILE_ID, mProfile.uid);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            Parcelable[] gfts = savedInstanceState.getParcelableArray(DATA);
            ArrayList<FeedGift> g = new ArrayList<>(gfts.length);
            for (Parcelable p : gfts) {
                g.add((FeedGift) p);
            }
            mGridAdapter.setData(g, false);
            postGiftsLoadInfoUpdate(null);
            mGridAdapter.notifyDataSetChanged();
            initViews();

            if (!mIsUpdating) {
                onNewFeeds(savedInstanceState.getInt(PROFILE_ID));
            }
        }
    }

    public void setProfile(Profile profile) {
        mProfile = profile;
        setGifts(profile.gifts);
    }

    public Profile getProfile() {
        return mProfile;
    }

    private void onNewFeeds() {
        onNewFeeds(mProfile.uid);
    }

    private void onNewFeeds(int userId) {
        mIsUpdating = true;
        FeedGiftsRequest request = new FeedGiftsRequest(getActivity());
        request.limit = GIFTS_LOAD_COUNT;
        request.uid = userId;
        final FeedList<FeedGift> data = mGridAdapter.getData();
        if (!data.isEmpty()) {
            if (data.getLast().isLoader() || data.getLast().isRetrier()) {
                request.from = data.get(data.size() - 2).gift.feedId;
            } else {
                request.from = data.get(data.size() - 1).gift.feedId;
            }
        }
        request.callback(new DataApiHandler<FeedListData<FeedGift>>() {

            @Override
            protected void success(FeedListData<FeedGift> gifts, IApiResponse response) {

                removeLoaderItem();
                data.addAll(gifts.items);
                if (!gifts.items.isEmpty()) {
                    mGroupInfo.setVisibility(View.GONE);
                    mTextInfo.setVisibility(View.GONE);
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
            if (mGridAdapter.getData().getLast().isLoader() || mGridAdapter.getData().getLast().isRetrier()) {
                mGridAdapter.getData().remove(mGridAdapter.getData().size() - 1);
            }
        }
    }

    protected void updateIfRetrier(int position) {
        if (mGridAdapter.getData().get(position).isRetrier()) {
            updateUI(new Runnable() {
                public void run() {
                    removeLoaderItem();
                    mGridAdapter.getData().add(new FeedGift(IListLoader.ItemType.LOADER));
                    mGridAdapter.notifyDataSetChanged();
                    onNewFeeds();
                }
            });
        }
    }
}

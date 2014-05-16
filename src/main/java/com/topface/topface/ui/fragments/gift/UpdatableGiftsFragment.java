package com.topface.topface.ui.fragments.gift;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.topface.topface.data.FeedGift;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.FeedGiftsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.IListLoader;

import java.util.List;

/**
 * Fragment displaying updatable gifts feed
 */
public class UpdatableGiftsFragment extends PlainGiftsFragment<Profile.Gifts> {

    private static final int GIFTS_LOAD_COUNT = 30;

    private Profile mProfile;
    private boolean mIsUpdating = false;

    @Override
    public void setGifts(Profile.Gifts gifts) {
        super.setGifts(gifts);

        if (gifts.more) {
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

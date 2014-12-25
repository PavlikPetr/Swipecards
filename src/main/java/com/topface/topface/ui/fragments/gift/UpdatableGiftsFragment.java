package com.topface.topface.ui.fragments.gift;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;

import com.topface.framework.utils.Debug;
import com.topface.topface.data.BasePendingInit;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.FeedGiftsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;

import java.util.ArrayList;

/**
 * Fragment displaying updatable gifts feed
 */
public class UpdatableGiftsFragment extends PlainGiftsFragment<Profile.Gifts> {

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
        if (mGridAdapter != null) {
            outState.putInt(PROFILE_ID, mProfileId);
        }

    }

    private UserProfileFragment getUserProfileFragment() {
        UserProfileFragment fragment = null;
        try {
            fragment = (UserProfileFragment) getParentFragment();
        } catch (Exception e) {
            Debug.error("Fragment not equals UserProfileFragment ", e);
        }
        return fragment;
    }

    @Override
    protected void restoreInstanceState(Bundle savedState) {
        UserProfileFragment fragment = getUserProfileFragment();
        if (fragment != null) {
            ArrayList<FeedGift> newGifts = fragment.getNewGifts();
            if (newGifts.size() > 0) {
                ArrayList<Parcelable> gfts = savedState.getParcelableArrayList(PlainGiftsFragment.DATA);
                ArrayList<FeedGift> g = new ArrayList<>(gfts.size());
                for (Parcelable p : gfts) {
                    g.add((FeedGift) p);
                }
                // find button SendGift and add new gifts after it
                g.addAll(getSendGiftButtonPosition(g), newGifts);
                // displace list position
                int position = savedState.getInt(PlainGiftsFragment.POSITION, 0) + newGifts.size();
                fragment.clearNewFeedGift();
                savedState.putParcelableArrayList(DATA, g);
                savedState.putInt(POSITION, position);

            }
        }
        super.restoreInstanceState(savedState);
        mProfileId = savedState.getInt(PROFILE_ID);
        if (!mIsUpdating) {
            onNewFeeds(mProfileId);
        }
    }

    private int getSendGiftButtonPosition(ArrayList<FeedGift> gifts) {
        if (gifts.size() > 0 && gifts.get(0).gift.type == Gift.SEND_BTN) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPendingProfileInit.setCanSet(true);
        if (mPendingProfileInit.getCanSet()) {
            setProfilePending(mPendingProfileInit.getData());
            UserProfileFragment fragment = getUserProfileFragment();
            if (fragment != null) {
                fragment.clearNewFeedGift();
            }
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

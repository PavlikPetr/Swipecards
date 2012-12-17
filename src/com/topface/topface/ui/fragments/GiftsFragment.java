package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.*;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedGiftsRequest;
import com.topface.topface.requests.SendGiftRequest;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.ui.adapters.GiftsAdapter.ViewHolder;
import com.topface.topface.ui.adapters.IListLoader.ItemType;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.GiftGalleryManager;

import java.util.LinkedList;

public class GiftsFragment extends BaseFragment {
    // Data
    private String mTag;

    public static final String GIFTS_ALL_TAG = "giftsGridAll";
    public static final String GIFTS_USER_PROFILE_TAG = "giftsGridProfile";

    private GiftsAdapter mGridAdapter;
    private GiftGalleryManager<FeedGift> mGalleryManager;
    private GridView mGridView;

    private Profile mProfile;
    private FeedList<FeedGift> mGifts = new FeedList<FeedGift>();
    private boolean mIsUpdating = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_grid, null);

        mGridView = (GridView) root.findViewById(R.id.fragmentGrid);
        mGridView.setAnimationCacheEnabled(false);
        mGridView.setScrollingCacheEnabled(true);
        mGalleryManager = new GiftGalleryManager<FeedGift>(mGifts, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (!mTag.equals(GIFTS_ALL_TAG) && !mIsUpdating && mGifts.getLast().isLoader()) {
                    onNewFeeds();
                }
            }
        });
        mGridAdapter = new GiftsAdapter(getActivity().getApplicationContext(), mGalleryManager);
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnScrollListener(mGalleryManager);

        if(mProfile != null) {
            setProfile(mProfile);
            initViews(root);
        }

        return root;
    }

    private void initViews(final View root) {
        if (root != null) {
            root.post(new Runnable() {
                @Override
                public void run() {
                    if (mTag.equals(GIFTS_ALL_TAG)) {
                        root.findViewById(R.id.fragmentTitle).setVisibility(View.GONE);
                        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                FeedGift item = (FeedGift) parent.getItemAtPosition(position);
                                Intent intent = getActivity().getIntent();
                                if (view.getTag() instanceof ViewHolder) {
                                    if (item.gift.type != Gift.PROFILE && item.gift.type != Gift.SEND_BTN) {
                                        intent.putExtra(GiftsActivity.INTENT_GIFT_ID, item.gift.id);
                                        intent.putExtra(GiftsActivity.INTENT_GIFT_URL, item.gift.link);
                                        intent.putExtra(GiftsActivity.INTENT_GIFT_PRICE, item.gift.price);

                                        getActivity().setResult(Activity.RESULT_OK, intent);
                                        getActivity().finish();
                                    }
                                }
                            }
                        });
                        if (mProfile != null) {
                            ((TextView) getView().findViewById(R.id.fragmentTitle)).setText(R.string.gifts);
                            root.findViewById(R.id.fragmentTitle).setVisibility(View.VISIBLE);
                            if (mGifts.size() == 0) {
                                onNewFeeds();
                            }
                        }
                    } else if (mTag.equals(GIFTS_USER_PROFILE_TAG)) {
                        ((TextView) getView().findViewById(R.id.fragmentTitle)).setText(R.string.gifts);
                        root.findViewById(R.id.fragmentTitle).setVisibility(View.VISIBLE);
                        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                if (view.getTag() instanceof ViewHolder) {
                                    FeedGift item = (FeedGift) parent.getItemAtPosition(position);
                                    if (item.gift != null) {
                                        if (item.gift.type == Gift.SEND_BTN) {
                                            Intent intent = new Intent(getActivity().getApplicationContext(),
                                                    GiftsActivity.class);
                                            startActivityForResult(intent, GiftsActivity.INTENT_REQUEST_GIFT);
                                        }
                                    }
                                }

                                if (mGifts.get(position).isLoaderRetry()) {
                                    updateUI(new Runnable() {
                                        public void run() {
                                            updateUI(new Runnable() {

                                                @Override
                                                public void run() {
                                                    removeLoaderItem();
                                                    mGifts.add(new FeedGift(ItemType.LOADER));
                                                    mGridAdapter.notifyDataSetChanged();
                                                }
                                            });
                                            onNewFeeds();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
                Bundle extras = data.getExtras();
                final int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);
                final String url = extras.getString(GiftsActivity.INTENT_GIFT_URL);
                final int price = extras.getInt(GiftsActivity.INTENT_GIFT_PRICE);

                if (mProfile != null) {
                    final SendGiftRequest sendGift = new SendGiftRequest(getActivity());
                    registerRequest(sendGift);
                    sendGift.giftId = id;
                    sendGift.userId = mProfile.uid;
                    final FeedGift sendedGift = new FeedGift();
                    sendedGift.gift = new Gift();
                    sendedGift.gift.id = sendGift.giftId;
                    sendedGift.gift.link = url;
                    sendedGift.gift.type = Gift.PROFILE_NEW;
                    sendGift.callback(new ApiHandler() {
                        @Override
                        public void success(ApiResponse response) {
                            SendGiftAnswer answer = SendGiftAnswer.parse(response);
                            CacheProfile.power = answer.power;
                            CacheProfile.money = answer.money;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mGifts.size() > 1) {
                                        mGifts.add(1, sendedGift);
                                    } else {
                                        mGifts.addLast(sendedGift);
                                    }
                                    mGridAdapter.notifyDataSetChanged();
                                }
                            });
                        }

                        @Override
                        public void fail(int codeError, final ApiResponse response) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (response.code == ApiResponse.PAYMENT) {
                                        Intent intent = new Intent(getActivity()
                                                .getApplicationContext(), BuyingActivity.class);
                                        intent.putExtra(BuyingActivity.INTENT_USER_COINS, price
                                                - CacheProfile.money);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    }).exec();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void removeLoaderItem() {
        if (mGifts.size() > 0) {
            if (mGifts.getLast().isLoader() || mGifts.getLast().isLoaderRetry()) {
                mGifts.remove(mGifts.size() - 1);
            }
        }
    }

    private void onNewFeeds() {
        onNewFeeds(mProfile.uid);
    }

    private void onNewFeeds(int userId) {
        mIsUpdating = true;
        FeedGiftsRequest request = new FeedGiftsRequest(getActivity());
        request.limit = UserProfileActivity.GIFTS_LOAD_COUNT;
        request.uid = userId;
        if (!mGifts.isEmpty()) {
            if (mGifts.getLast().isLoader() || mGifts.getLast().isLoaderRetry()) {
                request.from = mGifts.get(mGifts.size() - 2).gift.feedId;
            } else {
                request.from = mGifts.get(mGifts.size() - 1).gift.feedId;
            }
        }

        request.callback(new ApiHandler() {

            @Override
            public void success(ApiResponse response) {
                final FeedListData<FeedGift> feedGifts = new FeedListData<FeedGift>(response.jsonResult, FeedGift.class);

                updateUI(new Runnable() {

                    @Override
                    public void run() {
                        removeLoaderItem();
                        mGifts.addAll(feedGifts.items);

                        if (FeedGift.more) {
                            mGifts.add(new FeedGift(ItemType.LOADER));
                        }
                        mGridAdapter.notifyDataSetChanged();
                    }
                });
                mIsUpdating = false;
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        removeLoaderItem();
                        mGifts.add(new FeedGift(ItemType.RETRY));
                        mGridAdapter.notifyDataSetChanged();
                    }
                });
                mIsUpdating = false;
            }
        }).exec();
    }

    public void setGifts(LinkedList<Gift> gifts) {
        if (mProfile == null) mTag = GIFTS_ALL_TAG;
        mGifts.clear();
        for (Gift gift : gifts) {
            FeedGift item = new FeedGift();
            item.gift = gift;
            mGifts.add(item);
        }
        if (mTag != null && mTag.equals(GIFTS_USER_PROFILE_TAG)) {
            mGifts.add(0, FeedGift.getSendedGiftItem());
            if (mGifts.size() >= UserProfileActivity.GIFTS_LOAD_COUNT)
                mGifts.add(new FeedGift(ItemType.LOADER));
        }

        if (mGridView != null) {
            mGridView.post(new Runnable() {
                @Override
                public void run() {
                    mGridAdapter.notifyDataSetChanged();
                }
            });
        }

        initViews(getView());
    }

    public void setProfile(Profile profile) {
        if (profile == null) mTag = GIFTS_ALL_TAG;
        else {
            if (profile instanceof User) mTag = GIFTS_USER_PROFILE_TAG;
            else mTag = GIFTS_ALL_TAG;
            mProfile = profile;
            setGifts(profile.gifts);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void sendGift() {
        Intent intent = new Intent(getActivity().getApplicationContext(),
                GiftsActivity.class);
        startActivityForResult(intent, GiftsActivity.INTENT_REQUEST_GIFT);
    }
}

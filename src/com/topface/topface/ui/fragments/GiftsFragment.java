package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.*;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.FeedGiftsRequest;
import com.topface.topface.requests.SendGiftRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.ui.adapters.GiftsAdapter.ViewHolder;
import com.topface.topface.ui.adapters.IListLoader.ItemType;
import com.topface.topface.utils.CacheProfile;

import java.util.ArrayList;

public class GiftsFragment extends BaseFragment {
    private static final int GIFTS_LOAD_COUNT = 30;
    // Data
    private String mTag = GIFTS_ALL_TAG;

    public static final String GIFTS_ALL_TAG = "giftsGridAll";
    public static final String GIFTS_USER_PROFILE_TAG = "giftsGridProfile";

    private TextView mTitle;
    private View mGroupInfo;
    private TextView mTextInfo;
    private Button mBtnInfo;
    private GiftsAdapter mGridAdapter;
    private GridView mGridView;


    private Profile mProfile;
    private FeedList<FeedGift> mGifts = new FeedList<FeedGift>();
    private boolean mIsUpdating = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_grid, null);

        mGridView = (GridView) root.findViewById(R.id.usedGrid);
        mGridView.setAnimationCacheEnabled(false);
        mGridView.setScrollingCacheEnabled(true);
        mGridAdapter = new GiftsAdapter(getActivity().getApplicationContext(), mGifts, getUpdaterCallback());
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnScrollListener(mGridAdapter);

        mTitle = (TextView) root.findViewById(R.id.usedTitle);
        mGroupInfo = root.findViewById(R.id.loInfo);
        mTextInfo = (TextView) mGroupInfo.findViewById(R.id.tvInfo);
        mBtnInfo = (Button) mGroupInfo.findViewById(R.id.btnInfo);

        if (mProfile != null) {
            setProfile(mProfile);
        }
        initViews();

        return root;
    }

    private void initViews() {
        updateUI(new Runnable() {
            @Override
            public void run() {
                if (mTag.equals(GIFTS_ALL_TAG)) {
                    mTitle.setVisibility(View.GONE);
                    mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            FragmentActivity activity = getActivity();
                            if (activity != null) {
                                FeedGift item = (FeedGift) parent.getItemAtPosition(position);
                                Intent intent = activity.getIntent();
                                if (view.getTag() instanceof ViewHolder) {
                                    if (item.gift.type != Gift.PROFILE && item.gift.type != Gift.SEND_BTN) {
                                        intent.putExtra(GiftsActivity.INTENT_GIFT_ID, item.gift.id);
                                        intent.putExtra(GiftsActivity.INTENT_GIFT_URL, item.gift.link);
                                        intent.putExtra(GiftsActivity.INTENT_GIFT_PRICE, item.gift.price);

                                        EasyTracker.getTracker().trackEvent("Gifts", "Send", "GiftId=" + item.gift.id, (long) item.gift.price);
                                        activity.setResult(Activity.RESULT_OK, intent);
                                        activity.finish();
                                    }
                                }
                            }
                        }
                    });
                    if (mProfile != null) {
                        mTitle.setText(R.string.gifts);
                        mTitle.setVisibility(View.VISIBLE);
                        if (mGifts.size() == 0) {
                            onNewFeeds();
                        }
                    }
                } else if (mTag.equals(GIFTS_USER_PROFILE_TAG)) {
                    if (mGifts.size() > 1)
                        mTitle.setText(R.string.gifts);
                    else
                        mTitle.setText(R.string.user_does_not_have_gifts);
                    mTitle.setVisibility(View.VISIBLE);
                    mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (view.getTag() instanceof ViewHolder) {
                                FeedGift item = (FeedGift) parent.getItemAtPosition(position);
                                if (item.gift != null) {
                                    if (item.gift.type == Gift.SEND_BTN) {
                                        sendGift();
                                    }
                                }
                            }

                            if (mGifts.get(position).isRetrier()) {
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
                    sendedGift.gift = new Gift(
                            sendGift.giftId,
                            Gift.PROFILE_NEW,
                            url,
                            0
                    );
                    sendGift.callback(new DataApiHandler<SendGiftAnswer>() {

                        @Override
                        protected void success(SendGiftAnswer answer, ApiResponse response) {
                            CacheProfile.likes = answer.likes;
                            CacheProfile.money = answer.money;
                            if (mGifts.size() > 1) {
                                mGifts.add(1, sendedGift);
                            } else {
                                mGifts.add(sendedGift);
                                mTitle.setText(R.string.gifts);
                            }
                            mGridAdapter.notifyDataSetChanged();
                        }

                        @Override
                        protected SendGiftAnswer parseResponse(ApiResponse response) {
                            return SendGiftAnswer.parse(response);
                        }

                        @Override
                        public void fail(int codeError, final ApiResponse response) {
                            if (response.code == ApiResponse.PAYMENT) {
                                FragmentActivity activity = getActivity();
                                if (activity != null) {
                                    Intent intent = new Intent(activity.getApplicationContext(),
                                            ContainerActivity.class);
                                    intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
                                    intent.putExtra(BuyingFragment.ARG_ITEM_TYPE, BuyingFragment.TYPE_GIFT);
                                    intent.putExtra(BuyingFragment.ARG_ITEM_PRICE, price);
                                    startActivity(intent);
                                }
                            }
                        }
                    }).exec();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void removeLoaderItem() {
        if (mGifts.size() > 0) {
            if (mGifts.getLast().isLoader() || mGifts.getLast().isRetrier()) {
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
        request.limit = GIFTS_LOAD_COUNT;
        request.uid = userId;
        if (!mGifts.isEmpty()) {
            if (mGifts.getLast().isLoader() || mGifts.getLast().isRetrier()) {
                request.from = mGifts.get(mGifts.size() - 2).gift.feedId;
            } else {
                request.from = mGifts.get(mGifts.size() - 1).gift.feedId;
            }
        }

        request.callback(new DataApiHandler<FeedListData<FeedGift>>() {

            @Override
            protected void success(FeedListData<FeedGift> gifts, ApiResponse response) {

                removeLoaderItem();
                mGifts.addAll(gifts.items);
                if (mGifts.isEmpty() && !gifts.items.isEmpty()) {
                    mGroupInfo.setVisibility(View.GONE);
                }

                if (gifts.more) {
                    mGifts.add(new FeedGift(ItemType.LOADER));
                }
                mGridAdapter.notifyDataSetChanged();
                mIsUpdating = false;
            }

            @Override
            protected FeedListData<FeedGift> parseResponse(ApiResponse response) {
                return new FeedListData<FeedGift>(response.jsonResult, FeedGift.class);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                removeLoaderItem();
                mGifts.add(new FeedGift(ItemType.RETRY));
                mGridAdapter.notifyDataSetChanged();
                mIsUpdating = false;
            }
        }).exec();
    }

    public void setGifts(ArrayList<Gift> gifts) {
        if (mProfile == null) mTag = GIFTS_ALL_TAG;
        mGifts.clear();

        for (Gift gift : gifts) {
            FeedGift item = new FeedGift();
            item.gift = gift;
            mGifts.add(item);
        }
        if (mTag != null) {
            if (mTag.equals(GIFTS_USER_PROFILE_TAG)) {
                mGifts.add(0, FeedGift.getSendedGiftItem());
                if (mGifts.size() >= GIFTS_LOAD_COUNT)
                    mGifts.add(new FeedGift(ItemType.LOADER));
            } else {
                if (mGifts.isEmpty()) {
                    mGroupInfo.setVisibility(View.VISIBLE);
                    mTextInfo.setText(R.string.you_dont_have_gifts_yet);
                    mBtnInfo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ContainerActivity.class);
                            intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
                            startActivity(intent);
                        }
                    });
                } else {
                    mGroupInfo.setVisibility(View.GONE);
                }
            }
        }
        if (mGridView != null) {
            mGridView.post(new Runnable() {
                @Override
                public void run() {
                    mGridAdapter.notifyDataSetChanged();
                }
            });
        }

        initViews();
    }

    public void setProfile(Profile profile) {
        if (profile == null) mTag = GIFTS_ALL_TAG;
        else {
            if (profile instanceof User) {
                mTag = GIFTS_USER_PROFILE_TAG;
            } else mTag = GIFTS_ALL_TAG;
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
        getParentFragment().startActivityForResult(intent, GiftsActivity.INTENT_REQUEST_GIFT);
    }

    protected FeedAdapter.Updater getUpdaterCallback() {
        return new FeedAdapter.Updater() {
            @Override
            public void onUpdate() {
                if (!mIsUpdating) {
                    if (!mTag.equals(GIFTS_ALL_TAG) && !mIsUpdating && mGifts.getLast().isLoader()) {
                        onNewFeeds();
                    }
                }
            }
        };
    }
}

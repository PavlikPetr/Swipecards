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
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.FeedGiftsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendGiftRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.GiftsAdapter;
import com.topface.topface.ui.adapters.GiftsAdapter.ViewHolder;
import com.topface.topface.ui.adapters.IListLoader.ItemType;
import com.topface.topface.ui.fragments.buy.BuyingFragment;
import com.topface.topface.ui.fragments.profile.ProfileInnerFragment;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;

import java.util.List;

public class GiftsFragment extends ProfileInnerFragment {
    public static final String GIFTS_ALL_TAG = "giftsGridAll";
    // Data
    private String mTag = GIFTS_ALL_TAG;
    public static final String GIFTS_USER_PROFILE_TAG = "giftsGridProfile";
    private static final int GIFTS_LOAD_COUNT = 30;
    private TextView mTitle;
    private View mGroupInfo;
    private TextView mTextInfo;
    private Button mBtnInfo;
    private GiftsAdapter mGridAdapter;
    private GridView mGridView;
    private UserProfileFragment.OnGiftReceivedListener mGiftReceivedListener;

    private Profile mProfile;
    private boolean mIsUpdating = false;
    private boolean needFeedUpdate = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_grid, null);

        mGridView = (GridView) root.findViewById(R.id.usedGrid);
        mGridView.setAnimationCacheEnabled(false);
        mGridView.setScrollingCacheEnabled(true);
        mGridAdapter = new GiftsAdapter(getActivity().getApplicationContext(), new FeedList<FeedGift>(), getUpdaterCallback());
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnScrollListener(mGridAdapter);

        mTitle = (TextView) root.findViewById(R.id.usedTitle);
        mGroupInfo = root.findViewById(R.id.loInfo);
        mTextInfo = (TextView) mGroupInfo.findViewById(R.id.tvInfo);
        mBtnInfo = (Button) mGroupInfo.findViewById(R.id.btnInfo);
        if (mProfile != null) {
            setProfile(mProfile);
        }
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
                                    if (item != null && item.gift.type != Gift.PROFILE && item.gift.type != Gift.SEND_BTN) {
                                        intent.putExtra(GiftsActivity.INTENT_GIFT_ID, item.gift.id);
                                        intent.putExtra(GiftsActivity.INTENT_GIFT_URL, item.gift.link);
                                        intent.putExtra(GiftsActivity.INTENT_GIFT_PRICE, item.gift.price);

                                        EasyTracker.getTracker().sendEvent("Gifts", "Send", "GiftId=" + item.gift.id, (long) item.gift.price);
                                        activity.setResult(Activity.RESULT_OK, intent);
                                        activity.finish();
                                    }
                                }
                            }
                        }
                    });

                } else if (mTag.equals(GIFTS_USER_PROFILE_TAG)) {
                    if (mGridAdapter.getData().size() > 1)
                        mTitle.setText(R.string.gifts);
                    else
                        mTitle.setText(R.string.user_does_not_have_gifts);
                    mTitle.setVisibility(View.VISIBLE);
                    mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (view.getTag() instanceof ViewHolder) {
                                FeedGift item = (FeedGift) parent.getItemAtPosition(position);
                                if (item != null && item.gift != null) {
                                    if (item.gift.type == Gift.SEND_BTN) {
                                        sendGift();
                                    }
                                }
                            }

                            if (mGridAdapter.getData().get(position).isRetrier()) {
                                updateUI(new Runnable() {
                                    public void run() {
                                        removeLoaderItem();
                                        mGridAdapter.getData().add(new FeedGift(ItemType.LOADER));
                                        mGridAdapter.notifyDataSetChanged();
                                        onNewFeeds();
                                    }
                                });
                            }
                        }
                    });
                }
                if (mProfile != null) {
                    mTitle.setText(R.string.gifts);
                    mTitle.setVisibility(View.VISIBLE);
                    if (mGridAdapter.getData().size() <= getMinItemsCount() && needFeedUpdate) {
                        onNewFeeds();
                    }
                }
            }
        });
    }

    private int getMinItemsCount() {
        return mTag.equals(GIFTS_USER_PROFILE_TAG) ? 1 : 0;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
                //Этот флаг нужен для того, чтобы, когда нет подарков,
                //на onResume не кидался запрос на обновление подарков.
                needFeedUpdate = false;
                sendGift(data);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendGift(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
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
                    protected void success(SendGiftAnswer answer, IApiResponse response) {
                        addGift(sendedGift);
                    }

                    @Override
                    protected SendGiftAnswer parseResponse(ApiResponse response) {
                        return SendGiftAnswer.parse(response);
                    }

                    @Override
                    public void fail(int codeError, final IApiResponse response) {
                        if (response.isCodeEqual(ErrorCodes.PAYMENT)) {
                            FragmentActivity activity = getActivity();
                            if (activity != null) {
                                Intent intent = ContainerActivity.getBuyingIntent("Gifts");
                                intent.putExtra(BuyingFragment.ARG_ITEM_TYPE, BuyingFragment.TYPE_GIFT);
                                intent.putExtra(BuyingFragment.ARG_ITEM_PRICE, price);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void always(IApiResponse response) {
                        super.always(response);
                        needFeedUpdate = true;
                        if (mGiftReceivedListener != null) {
                            mGiftReceivedListener.onReceived();
                        }
                    }
                }).exec();
            }
        }
    }

    public void addGift(FeedGift sendedGift) {
        if (mGridAdapter.getData().size() > 1) {
            mGridAdapter.add(1, sendedGift);
        } else {
            mGridAdapter.add(sendedGift);
            mTitle.setText(R.string.gifts);
        }
        if (mProfile.gifts != null) mProfile.gifts.add(0, sendedGift.gift);
        mGridAdapter.notifyDataSetChanged();
        if (getActivity() != null) {
            Toast.makeText(getActivity(), R.string.chat_gift_out, Toast.LENGTH_LONG).show();
        }
    }

    private void removeLoaderItem() {
        if (mGridAdapter.getData().size() > 0) {
            if (mGridAdapter.getData().getLast().isLoader() || mGridAdapter.getData().getLast().isRetrier()) {
                mGridAdapter.getData().remove(mGridAdapter.getData().size() - 1);
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
                } else if (mTag.equals(GIFTS_USER_PROFILE_TAG) && mGridAdapter.getData().size() <= getMinItemsCount()) {
                    mTitle.setText(R.string.user_does_not_have_gifts);
                }

                if (gifts.more) {
                    data.add(new FeedGift(ItemType.LOADER));
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
                data.add(new FeedGift(ItemType.RETRY));
                mGridAdapter.notifyDataSetChanged();
                mIsUpdating = false;
            }
        }).exec();
    }

    public void setGifts(Profile.Gifts gifts) {
        setGifts(gifts, gifts.more);
    }

    public void setGifts(List<Gift> gifts) {
        setGifts(gifts, false);
    }

    public void setGifts(List<Gift> gifts, boolean more) {
        if (mProfile == null) mTag = GIFTS_ALL_TAG;
        if (isAdded()) {
            FeedList<FeedGift> data = mGridAdapter.getData();
            if (data != null) {
                data.clear();
                for (Gift gift : gifts) {
                    FeedGift item = new FeedGift();
                    item.gift = gift;
                    data.add(item);
                }
                if (mTag != null) {
                    if (mTag.equals(GIFTS_USER_PROFILE_TAG)) {
                        data.add(0, FeedGift.getSendedGiftItem());
                        if (more) {
                            data.add(new FeedGift(ItemType.LOADER));
                        }
                    } else {
                        if (data.isEmpty()) {
                            mGroupInfo.setVisibility(View.VISIBLE);
                            mTextInfo.setText(R.string.you_dont_have_gifts_yet);
                            mBtnInfo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(ContainerActivity.getBuyingIntent("ProfileGifts"));
                                }
                            });
                        } else {
                            mGroupInfo.setVisibility(View.GONE);
                            mTextInfo.setVisibility(View.GONE);
                        }
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

    public void sendGift(UserProfileFragment.OnGiftReceivedListener listener) {
        this.mGiftReceivedListener = listener;
        sendGift();
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
                if (!mIsUpdating && !mTag.equals(GIFTS_ALL_TAG) && mGridAdapter.getData().getLast().isLoader()) {
                    onNewFeeds();
                }
            }
        };
    }
}

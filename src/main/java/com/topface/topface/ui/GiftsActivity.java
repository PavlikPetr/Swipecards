package com.topface.topface.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Gift;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.fragments.GiftsFragment;
import com.topface.topface.ui.fragments.buy.BuyingFragment;
import com.topface.topface.ui.views.TripleButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;

public class GiftsActivity extends BaseFragmentActivity implements IGiftSendListener {

    public static final int INTENT_REQUEST_GIFT = 111;
    public static final String INTENT_GIFT_ID = "gift_id";
    public static final String INTENT_GIFT_URL = "gift_url";
    public static final String INTENT_GIFT_PRICE = "gift_price";
    public static final String INTENT_USER_ID_TO_SEND_GIFT = "user_id_to_send_gift";
    private static final String INTENT_SEND_GIFT = "send_gitft_request";
    public static final String GIFTS_LIST = "gifts_list";

    public static ArrayList<Gift> mGiftsList = new ArrayList<>();
    private int mUserIdToSendGift;
    private boolean mNeedToSendGift;

    public GiftsCollection mGiftsCollection;
    private TripleButton mTripleButton;
    private GiftsFragment mGiftFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_gifts);
        getSupportActionBar().setTitle(getString(R.string.gifts_title));

        mUserIdToSendGift = getIntent().getIntExtra(INTENT_USER_ID_TO_SEND_GIFT, 0);
        mNeedToSendGift = getIntent().getBooleanExtra(INTENT_SEND_GIFT, true);
        mGiftFragment = new GiftsFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.giftGrid, mGiftFragment, GiftsFragment.GIFTS_ALL_TAG).commit();

        mGiftsCollection = new GiftsCollection();

        // init triple button
        mTripleButton = (TripleButton) findViewById(R.id.btnTriple);
        mTripleButton.setLeftText(Gift.getTypeNameResId(Gift.ROMANTIC));
        mTripleButton.setMiddleText(Gift.getTypeNameResId(Gift.FRIENDS));
        mTripleButton.setRightText(Gift.getTypeNameResId(Gift.PRESENT));

        mTripleButton.setLeftListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        mGiftsCollection.setCurrentType(Gift.ROMANTIC);
                        mGiftFragment.setGifts(mGiftsCollection.getGifts());
                    }
                });
            }
        });

        mTripleButton.setMiddleListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        mGiftsCollection.setCurrentType(Gift.FRIENDS);
                        mGiftFragment.setGifts(mGiftsCollection.getGifts());
                    }
                });

            }
        });

        mTripleButton.setRightListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        mGiftsCollection.setCurrentType(Gift.PRESENT);
                        mGiftFragment.setGifts(mGiftsCollection.getGifts());
                    }
                });
            }
        });

    }

    /**
     * Loading array of gifts from server
     */
    private void update() {
        if (mGiftsList.isEmpty()) {
            mTripleButton.setChecked(TripleButton.LEFT_BUTTON);
            mTripleButton.setEnabled(false);
            setSupportProgressBarIndeterminateVisibility(true);
            GiftsRequest giftRequest = new GiftsRequest(this);
            registerRequest(giftRequest);
            giftRequest.callback(new DataApiHandler<LinkedList<Gift>>() {

                @Override
                protected void success(LinkedList<Gift> data, IApiResponse response) {
                    mGiftsList.addAll(data);
                    mGiftsCollection.add(mGiftsList);
                    mGiftFragment.setGifts(mGiftsCollection.getGifts());
                }

                @Override
                protected LinkedList<Gift> parseResponse(ApiResponse response) {
                    return Gift.parse(response);
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    Toast.makeText(
                            App.getContext(),
                            R.string.general_data_error,
                            Toast.LENGTH_SHORT
                    ).show();
                }

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
                    mTripleButton.setEnabled(true);
                    setSupportProgressBarIndeterminateVisibility(false);
                }
            }).exec();
        } else {
            mGiftsCollection.add(mGiftsList);
            mGiftFragment.setGifts(mGiftsCollection.getGifts());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
        switch (GiftsCollection.currentType) {
            case Gift.ROMANTIC:
                mTripleButton.setChecked(TripleButton.LEFT_BUTTON);
                break;
            case Gift.FRIENDS:
                mTripleButton.setChecked(TripleButton.MIDDLE_BUTTON);
                break;
            case Gift.PRESENT:
                mTripleButton.setChecked(TripleButton.RIGHT_BUTTON);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(GIFTS_LIST, mGiftsList);
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mGiftsList = savedInstanceState.getParcelableArrayList(GIFTS_LIST);
    }

    @Override
    public void onSendGift(final Gift item) {
        EasyTracker.getTracker().sendEvent("Gifts", "Send", "GiftId=" + item.id, (long) item.price);
        if (mNeedToSendGift) {
            final SendGiftRequest sendGiftRequest = new SendGiftRequest(this);
            sendGiftRequest.giftId = item.id;
            sendGiftRequest.userId = mUserIdToSendGift;
            registerRequest(sendGiftRequest);
            setSupportProgressBarIndeterminateVisibility(true);
            sendGiftRequest.callback(new DataApiHandler<SendGiftAnswer>() {

                @Override
                protected void success(SendGiftAnswer answer, IApiResponse response) {
                    processResult(item);
                }

                @Override
                protected SendGiftAnswer parseResponse(ApiResponse response) {
                    return SendGiftAnswer.parse(response);
                }

                @Override
                public void fail(int codeError, final IApiResponse response) {
                    setSupportProgressBarIndeterminateVisibility(false);
                    if (response.isCodeEqual(ErrorCodes.PAYMENT)) {
                        startActivity(ContainerActivity.getBuyingIntent("Gifts", BuyingFragment.TYPE_GIFT, item.price));
                    }
                }
            }).exec();
        } else {
            processResult(item);
        }
    }

    private void processResult(Gift item) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(INTENT_GIFT_ID, item.id);
        resultIntent.putExtra(INTENT_GIFT_URL, item.link);
        resultIntent.putExtra(INTENT_GIFT_PRICE, item.price);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Intent to start GiftsActivity for sending gift item
     * By default send gift request will be executed in GiftActivity and return result item
     * in onActivityResult(...)
     *
     * @param context where activity will be launched
     * @param userId  profile to send gift
     * @return intent
     */
    public static Intent getSendGiftIntent(Context context, int userId) {
        Intent result = new Intent(context, GiftsActivity.class);
        result.putExtra(INTENT_USER_ID_TO_SEND_GIFT, userId);
        result.putExtra(INTENT_SEND_GIFT, true);
        return result;
    }

    /**
     * Intent to start GiftsActivity for sending gift item
     * If you need to process send gift request yourself set sendGift flag to false
     *
     * @param context  lauch context
     * @param userId   profile id to send gift
     * @param sendGift false to send gift request yourself
     * @return intent
     */
    public static Intent getSendGiftIntent(Context context, int userId, boolean sendGift) {
        Intent result = new Intent(context, GiftsActivity.class);
        result.putExtra(INTENT_USER_ID_TO_SEND_GIFT, userId);
        result.putExtra(INTENT_SEND_GIFT, sendGift);
        return result;
    }

    /**
     * Works with array of gifts, categorizes by type
     */
    public static class GiftsCollection {
        public static int currentType = Gift.ROMANTIC;
        private LinkedList<Gift> mAllGifts = new LinkedList<>();

        public void add(ArrayList<Gift> gifts) {
            mAllGifts.addAll(gifts);
        }

        public ArrayList<Gift> getGifts(int type) {
            ArrayList<Gift> result = new ArrayList<>();
            for (Gift gift : mAllGifts) {
                if (gift.type == type) {
                    result.add(gift);
                }
            }

            return result;
        }

        public ArrayList<Gift> getGifts() {
            return getGifts(GiftsCollection.currentType);
        }

        public void setCurrentType(int type) {
            GiftsCollection.currentType = type;
        }
    }
}

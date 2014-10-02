package com.topface.topface.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.topface.topface.R;
import com.topface.topface.data.Gift;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.GiftsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendGiftRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.gift.PlainGiftsFragment;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.ui.views.TripleButton;
import com.topface.topface.utils.EasyTracker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GiftsActivity extends BaseFragmentActivity implements IGiftSendListener {

    public static final int INTENT_REQUEST_GIFT = 111;
    public static final String INTENT_GIFT_PRICE = "gift_price";
    public static final String INTENT_USER_ID_TO_SEND_GIFT = "user_id_to_send_gift";
    public static final String GIFTS_LIST = "gifts_list";
    public static final String INTENT_SEND_GIFT_ANSWER = "send_gift_answer";

    public static ArrayList<Gift> mGiftsList = new ArrayList<>();
    private int mUserIdToSendGift;
    private boolean mRequestingGifts;

    public GiftsCollection mGiftsCollection;
    private TripleButton mTripleButton;
    private PlainGiftsFragment<List<Gift>> mGiftFragment;
    private RelativeLayout mLockScreen;
    private RetryViewCreator mRetryView;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_gifts);
        getTitleSetter().setActionBarTitles(getString(R.string.gifts_title), null);

        mUserIdToSendGift = getIntent().getIntExtra(INTENT_USER_ID_TO_SEND_GIFT, 0);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.giftGrid);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (fragment == null) {
            mGiftFragment = new PlainGiftsFragment<>();
            transaction.add(R.id.giftGrid, mGiftFragment);
        } else {
            mGiftFragment = (PlainGiftsFragment<List<Gift>>) fragment;
            transaction.replace(R.id.giftGrid, mGiftFragment);
        }
        transaction.commit();

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

        mTripleButton.setChecked(TripleButton.LEFT_BUTTON);

        mLockScreen = (RelativeLayout) findViewById(R.id.lockScreen);
        mRetryView = RetryViewCreator.createDefaultRetryView(this, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadGifts();
            }
        });
        mLockScreen.addView(mRetryView.getView());

        loadGifts();
    }

    /**
     * Loading array of gifts from server
     */
    private void loadGifts() {
        if (mGiftsList.isEmpty()) {
            mTripleButton.setEnabled(false);
            setSupportProgressBarIndeterminateVisibility(true);
            GiftsRequest giftRequest = new GiftsRequest(this);
            registerRequest(giftRequest);
            mRequestingGifts = true;
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
                    mLockScreen.setVisibility(View.VISIBLE);
                    mRetryView.setText(getText(R.string.general_error_try_again_later).toString());
                    mRetryView.showRetryButton(true);
                }

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
                    mTripleButton.setEnabled(true);
                    setSupportProgressBarIndeterminateVisibility(false);
                    mRequestingGifts = false;
                }
            }).exec();
            mLockScreen.setVisibility(View.GONE);
        } else {
            mGiftsCollection.add(mGiftsList);
            mGiftFragment.setGifts(mGiftsCollection.getGifts());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGiftsList.isEmpty() && !mRequestingGifts) {
            loadGifts();
        }
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
    protected void onPostResume() {
        super.onPostResume();
        /**
         * setSupportProgressBarIndeterminateVisibility() doesn't work right after onCreate (first
         * time loadGifts() is called). We need to show progress bar here.
         */
        if (mRequestingGifts) {
            setSupportProgressBarIndeterminateVisibility(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRequestingGifts = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(GIFTS_LIST, mGiftsList);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mGiftsList = savedInstanceState.getParcelableArrayList(GIFTS_LIST);
    }

    @Override
    public void onSendGift(final Gift item) {
        EasyTracker.sendEvent("Gifts", "Send", "GiftId=" + item.id, (long) item.price);
        final SendGiftRequest sendGiftRequest = new SendGiftRequest(this);
        sendGiftRequest.giftId = item.id;
        sendGiftRequest.userId = mUserIdToSendGift;
        registerRequest(sendGiftRequest);
        setSupportProgressBarIndeterminateVisibility(true);
        sendGiftRequest.callback(new DataApiHandler<SendGiftAnswer>() {

            @Override
            protected void success(SendGiftAnswer answer, IApiResponse response) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(INTENT_SEND_GIFT_ANSWER, answer);
                resultIntent.putExtra(INTENT_GIFT_PRICE, item.price);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }

            @Override
            protected SendGiftAnswer parseResponse(ApiResponse response) {
                return SendGiftAnswer.parse(response);
            }

            @Override
            public void fail(int codeError, final IApiResponse response) {
                setSupportProgressBarIndeterminateVisibility(false);
                if (response.isCodeEqual(ErrorCodes.PAYMENT)) {
                    startActivity(PurchasesActivity.createBuyingIntent("Gifts", PurchasesFragment.TYPE_GIFT, item.price));
                }
            }
        }).exec();
    }

    /**
     * Intent to start GiftsActivity for sending gift item
     * If you need to process send gift request yourself set sendGift flag to false
     *
     * @param context  lauch context
     * @param userId   profile id to send gift
     * @return intent
     */
    public static Intent getSendGiftIntent(Context context, int userId) {
        Intent result = new Intent(context, GiftsActivity.class);
        result.putExtra(INTENT_USER_ID_TO_SEND_GIFT, userId);
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

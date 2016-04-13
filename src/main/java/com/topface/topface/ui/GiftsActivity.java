package com.topface.topface.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Options;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.GiftsRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendGiftRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.gift.GiftsListFragment;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.FlurryManager;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedList;

import static com.topface.topface.utils.FlurryManager.BUY_GIFT;

public class GiftsActivity extends BaseFragmentActivity implements IGiftSendListener {

    public static final int INTENT_REQUEST_GIFT = 111;
    public static final String INTENT_GIFT_PRICE = "gift_price";
    public static final String INTENT_USER_ID_TO_SEND_GIFT = "user_id_to_send_gift";
    public static final String INTENT_IS_SUCCESS_TOAST_AVAILABLE = "is_success_toast_available";
    public static final String GIFTS_LIST = "gifts_list";
    public static final String SUCCESS_TOAST_AVAILABLE = "success_toast_available";
    public static final String INTENT_SEND_GIFT_ANSWER = "send_gift_answer";

    private ArrayList<Gift> mAllGifts = new ArrayList<>();
    private int mUserIdToSendGift;
    private boolean mRequestingGifts;

    private boolean mIsSuccessToastAvailable;

    private GiftsListFragment mGiftListFragment;
    private RelativeLayout mLockScreen;
    private RetryViewCreator mRetryView;

    /**
     * Intent to start GiftsActivity for sending gift item
     * If you need to process send gift request yourself set sendGift flag to false
     *
     * @param context lauch context
     * @param userId  profile id to send gift
     * @return intent
     */
    public static Intent getSendGiftIntent(Context context, int userId) {
        Intent result = new Intent(context, GiftsActivity.class);
        result.putExtra(INTENT_USER_ID_TO_SEND_GIFT, userId);
        return result;
    }

    /**
     * Intent to start GiftsActivity for sending gift item
     * If you need to process send gift request yourself set sendGift flag to false
     * Ig you don't need to show toast on success gift send use isSuccessToastAvailable = false
     *
     * @param context                 lauch context
     * @param userId                  profile id to send gift
     * @param isSuccessToastAvailable show or not toast on success gift send
     * @return intent
     */
    public static Intent getSendGiftIntent(Context context, int userId, boolean isSuccessToastAvailable) {
        Intent result = new Intent(context, GiftsActivity.class);
        result.putExtra(INTENT_USER_ID_TO_SEND_GIFT, userId);
        result.putExtra(INTENT_IS_SUCCESS_TOAST_AVAILABLE, isSuccessToastAvailable);
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBarView.setArrowUpView(getResources().getString(R.string.profile_gifts));
        mUserIdToSendGift = getIntent().getIntExtra(INTENT_USER_ID_TO_SEND_GIFT, 0);
        mIsSuccessToastAvailable = getIntent().getBooleanExtra(INTENT_IS_SUCCESS_TOAST_AVAILABLE, true);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.giftGrid);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (fragment == null) {
            mGiftListFragment = new GiftsListFragment();
            transaction.add(R.id.giftGrid, mGiftListFragment);
        } else {
            mGiftListFragment = (GiftsListFragment) fragment;
            transaction.replace(R.id.giftGrid, mGiftListFragment);
        }
        transaction.commit();

        mLockScreen = (RelativeLayout) findViewById(R.id.lockScreen);
        mRetryView = new RetryViewCreator.Builder(App.getContext(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadGifts();
            }
        }).build();
        mLockScreen.addView(mRetryView.getView());
    }

    @Override
    protected int getContentLayout() {
        return R.layout.ac_gifts;
    }

    /**
     * Loading array of gifts from server
     */
    private void loadGifts() {
        if (mAllGifts.isEmpty()) {
            setSupportProgressBarIndeterminateVisibility(true);
            GiftsRequest giftRequest = new GiftsRequest(App.getContext());
            registerRequest(giftRequest);
            mRequestingGifts = true;
            giftRequest.callback(new DataApiHandler<LinkedList<Gift>>() {

                @Override
                protected void success(LinkedList<Gift> data, IApiResponse response) {
                    mAllGifts.addAll(data);
                    mGiftListFragment.setGifts(mAllGifts);
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
                    setSupportProgressBarIndeterminateVisibility(false);
                    mRequestingGifts = false;
                }
            }).exec();
            mLockScreen.setVisibility(View.GONE);
        } else {
            mGiftListFragment.setGifts(mAllGifts);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAllGifts.isEmpty() && !mRequestingGifts) {
            loadGifts();
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(GIFTS_LIST, mAllGifts);
        outState.putBoolean(SUCCESS_TOAST_AVAILABLE, mIsSuccessToastAvailable);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAllGifts = savedInstanceState.getParcelableArrayList(GIFTS_LIST);
        mIsSuccessToastAvailable = savedInstanceState.getBoolean(SUCCESS_TOAST_AVAILABLE);
    }

    @Override
    public void onSendGift(final Gift item) {
        EasyTracker.sendEvent("Gifts", "Send", "GiftId=" + item.id, (long) item.price);
        final Options options = App.from(this).getOptions();
        final SendGiftRequest sendGiftRequest = new SendGiftRequest(this, options.blockUnconfirmed);
        sendGiftRequest.giftId = item.id;
        sendGiftRequest.userId = mUserIdToSendGift;
        registerRequest(sendGiftRequest);
        setSupportProgressBarIndeterminateVisibility(true);
        sendGiftRequest.callback(new DataApiHandler<SendGiftAnswer>() {

            @Override
            protected void success(SendGiftAnswer answer, IApiResponse response) {
                FlurryManager.getInstance().sendSpendCoinsEvent(item.price, BUY_GIFT);
                Intent resultIntent = new Intent();
                resultIntent.putExtra(INTENT_SEND_GIFT_ANSWER, answer);
                resultIntent.putExtra(INTENT_GIFT_PRICE, item.price);
                setResult(Activity.RESULT_OK, resultIntent);
                if (mIsSuccessToastAvailable) {
                    Utils.showToastNotification(R.string.chat_gift_out, Toast.LENGTH_SHORT);
                }
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
                    startActivity(PurchasesActivity.createBuyingIntent("Gifts"
                            , PurchasesFragment.TYPE_GIFT, item.price, options.topfaceOfferwallRedirect));
                } else {
                    Utils.showErrorMessage();
                }
            }
        }).exec();
    }
}

package com.topface.topface.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Rate;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.requests.RateRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;

public class RateController {
    /**
     * Для теста отключаем диалог восхищения
     */

    public static final int MUTUAL_VALUE = 9;

    private Activity mContext;
    private OnRateControllerListener mOnRateControllerListener;

    public interface OnRateControllerListener {
        public void successRate();

        public void failRate();
    }

    public RateController(final Activity context) {
        mContext = context;
    }

    private void sendRate(final int userid, final int rate) {
        if (userid == 0) {
            return;
        }
        RateRequest doRate = new RateRequest(mContext);
        doRate.userid = userid;
        doRate.rate = rate;
        doRate.callback(new DataApiHandler<Rate>() {

            @Override
            protected void success(Rate rate, ApiResponse response) {
                CacheProfile.likes = rate.likes;
                CacheProfile.money = rate.money;
                CacheProfile.average_rate = rate.average;
            }

            @Override
            protected Rate parseResponse(ApiResponse response) {
                return Rate.parse(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                if (mOnRateControllerListener != null) {
                    mOnRateControllerListener.failRate();
                }
            }

        }).exec();

        mOnRateControllerListener.successRate();
    }

    public void onRate(final int userId, final int rate, final int mutualId, OnRateListener listener) {
        if (rate == 10 && CacheProfile.money <= 0) {
            Intent intent = new Intent(mContext, ContainerActivity.class);
            intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
            mContext.startActivity(intent);
            mOnRateControllerListener.failRate();
            return;
        }

            sendRate(userId, rate, mutualId, listener);
    }

    private void sendRate(final int userid, final int rate, final int mutualId, final OnRateListener listener) {
        RateRequest doRate = new RateRequest(mContext);
        doRate.userid = userid;
        doRate.rate = rate;
        doRate.mutualid = mutualId;
        doRate.callback(new DataApiHandler<Rate>() {

            @Override
            protected void success(Rate rate, ApiResponse response) {
                CacheProfile.likes = rate.likes;
                CacheProfile.money = rate.money;
                CacheProfile.average_rate = rate.average;
                if (listener != null) {
                    listener.onRateCompleted();
                }
            }

            @Override
            protected Rate parseResponse(ApiResponse response) {
                return Rate.parse(response);
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                if (listener != null) {
                    listener.onRateFailed();
                }
            }

        }).exec();

        if (mOnRateControllerListener != null) {
            mOnRateControllerListener.successRate();
        }
    }

    public void setOnRateControllerListener(OnRateControllerListener onRateControllerListener) {
        mOnRateControllerListener = onRateControllerListener;
    }

    public interface OnRateListener {
        public void onRateCompleted();

        public void onRateFailed();
    }
}

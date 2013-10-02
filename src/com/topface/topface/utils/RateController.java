package com.topface.topface.utils;

import android.app.Activity;
import com.topface.topface.data.Rate;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.RateRequest;
import com.topface.topface.ui.ContainerActivity;

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

    public void onRate(final int userId, final int rate, final int mutualId, OnRateListener listener) {
        if (rate == 10 && CacheProfile.money < CacheProfile.getOptions().price_highrate) {
            mContext.startActivity(ContainerActivity.getBuyingIntent("RateAdmiration"));

            if (mOnRateControllerListener != null) {
                mOnRateControllerListener.failRate();
            }
            if (listener != null) {
                listener.onRateFailed();
            }
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

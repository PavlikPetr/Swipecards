package com.topface.topface.utils;

import android.app.Activity;
import com.topface.topface.data.Rate;
import com.topface.topface.requests.*;
import com.topface.topface.ui.ContainerActivity;

public class RateController {
    /**
     * Для теста отключаем диалог восхищения
     */

    public static final int MUTUAL_VALUE = 9;

    private Activity mContext;
    private OnRateControllerListener mOnRateControllerUiListener;

    public RateController(final Activity context) {
        mContext = context;
    }

    public void onLike(final int userId, final int mutualId, final OnRateRequestListener requestListener) {
        sendRate(new SendLikeRequest(mContext), userId, mutualId, requestListener);
    }

    public void onAdmiration(final int userId, final int mutualId, final OnRateRequestListener requestListener) {
        if (CacheProfile.money <= 0) {
            mContext.startActivity(ContainerActivity.getBuyingIntent("RateAdmiration"));
            if (mOnRateControllerUiListener != null) {
                mOnRateControllerUiListener.failRate();
            }
            if (requestListener != null) {
                requestListener.onRateFailed();
            }
            return;
        }
        sendRate(new SendAdmirationRequest(mContext), userId, mutualId, requestListener);
    }

    public void onRate(final int userId, final int rate, final int mutualId, OnRateRequestListener requestListener) {
        if (rate == 10) {
            onAdmiration(userId,mutualId,requestListener);
        } else {
            onLike(userId,mutualId,requestListener);
        }
    }

    private void sendRate(SendLikeRequest sendLike, final int userid, final int mutualId, final OnRateRequestListener listener) {
        sendLike.userid = userid;
        sendLike.mutualid = mutualId;
        sendLike.callback(new DataApiHandler<Rate>() {

            @Override
            protected void success(Rate rate, IApiResponse response) {
                CacheProfile.likes = rate.likes;
                CacheProfile.money = rate.money;
                if (listener != null) {
                    listener.onRateCompleted();
                }
            }

            @Override
            protected Rate parseResponse(ApiResponse response) {
                return Rate.parse(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (listener != null) {
                    listener.onRateFailed();
                }
            }

        }).exec();

        if (mOnRateControllerUiListener != null) {
            mOnRateControllerUiListener.successRate();
        }
    }

    public void setOnRateControllerUiListener(OnRateControllerListener onRateControllerUiListener) {
        mOnRateControllerUiListener = onRateControllerUiListener;
    }

    /**
     * Interface for UI callbacks
     */
    public interface OnRateControllerListener {
        public void successRate();
        public void failRate();
    }

    /**
     * Interface for api request callbacks
     */
    public interface OnRateRequestListener {
        public void onRateCompleted();
        public void onRateFailed();
    }
}

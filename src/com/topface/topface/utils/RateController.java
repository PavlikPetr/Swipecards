package com.topface.topface.utils;

import android.app.Activity;

import com.topface.topface.data.Rate;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendAdmirationRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.ui.ContainerActivity;

public class RateController {

    private final SendLikeRequest.Place mPlace;
    private Activity mContext;
    private OnRateControllerListener mOnRateControllerUiListener;

    public RateController(final Activity context, SendLikeRequest.Place place) {
        mContext = context;
        mPlace = place;
    }

    public void onLike(final int userId, final int mutualId, final OnRateRequestListener requestListener) {
        sendRate(new SendLikeRequest(mContext, userId, mutualId, mPlace), requestListener);
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
        sendRate(new SendAdmirationRequest(mContext, userId, mutualId, mPlace), requestListener);
    }

    public void onRate(final int userId, final int rate, final int mutualId, OnRateRequestListener requestListener) {
        if (rate == 10) {
            onAdmiration(userId,mutualId,requestListener);
        } else {
            onLike(userId,mutualId,requestListener);
        }
    }

    private void sendRate(SendLikeRequest sendLike, final OnRateRequestListener listener) {
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

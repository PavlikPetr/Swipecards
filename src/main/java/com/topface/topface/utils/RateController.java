package com.topface.topface.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.data.Rate;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendAdmirationRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.utils.cache.SearchCacheManager;

public class RateController {

    public static final String USER_RATED = "com.topface.topface.USER_RATED";
    public static final String USER_ID_EXTRA = "user_id";

    private final SendLikeRequest.Place mPlace;
    private Context mContext;
    private OnRateControllerListener mOnRateControllerUiListener;

    public RateController(final Context context, SendLikeRequest.Place place) {
        mContext = context;
        mPlace = place;
    }

    public void onLike(final int userId, final int mutualId, final OnRateRequestListener requestListener) {
        sendRate(new SendLikeRequest(mContext, userId, mutualId, mPlace), requestListener);
    }

    public boolean onAdmiration(final int userId, final int mutualId, final OnRateRequestListener requestListener) {
        if (CacheProfile.money < CacheProfile.getOptions().priceAdmiration) {
            mContext.startActivity(ContainerActivity.getBuyingIntent("RateAdmiration"));
            if (mOnRateControllerUiListener != null) {
                mOnRateControllerUiListener.failRate();
            }
            if (requestListener != null) {
                requestListener.onRateFailed(userId, mutualId);
            }
            return false;
        }
        sendRate(new SendAdmirationRequest(mContext, userId, mutualId, mPlace), requestListener);
        return true;
    }

    private void sendRate(final SendLikeRequest sendLike, final OnRateRequestListener listener) {
        sendLike.callback(new DataApiHandler<Rate>() {

            @Override
            protected void success(Rate rate, IApiResponse response) {
                if (listener != null) {
                    listener.onRateCompleted(sendLike.getMutualid());
                }
                // Broadcast to disable dating rate buttons for current user
                userRateBroadcast(sendLike.getUserid());

                /* Update dating search cache for situations when it's fragment is destroyed
                   and it will be restored from cache
                 */
                SearchCacheManager mCache = new SearchCacheManager();
                @SuppressWarnings("unchecked") UsersList<SearchUser> searchUsers = mCache.getCache();
                int currentPosition = mCache.getPosition();

                if (searchUsers != null) {
                    boolean cacheUpdated = false;
                    for (SearchUser user : searchUsers) {
                        if (user.id == sendLike.getUserid()) {
                            if (searchUsers.indexOf(user) > currentPosition) {
                                searchUsers.remove(user);
                            } else {
                                user.rated = true;
                            }
                            cacheUpdated = true;
                            break;
                        }
                    }
                    if (cacheUpdated) {
                        mCache.setCache(searchUsers);
                    }
                }
            }

            @Override
            protected Rate parseResponse(ApiResponse response) {
                return Rate.parse(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (listener != null) {
                    listener.onRateFailed(sendLike.getUserid(), sendLike.getMutualid());
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

    public void userRateBroadcast(int userId) {
        Intent intent = new Intent(USER_RATED);
        intent.putExtra(USER_ID_EXTRA, userId);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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
        public void onRateCompleted(int mutualId);

        public void onRateFailed(int userId, int mutualId);
    }
}

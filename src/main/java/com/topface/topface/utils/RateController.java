package com.topface.topface.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.topface.topface.App;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.Options;
import com.topface.topface.data.Rate;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendAdmirationRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.utils.cache.SearchCacheManager;

import static com.topface.topface.utils.FlurryManager.SEND_ADMIRATION;

public class RateController {

    public static final String USER_RATED = "com.topface.topface.USER_RATED";
    public static final String USER_ID_EXTRA = "user_id";

    @SendLikeRequest.Place
    private final int mPlace;
    private Context mContext;
    private OnRateControllerListener mOnRateControllerUiListener;

    public RateController(final Context context, @SendLikeRequest.Place int place) {
        mContext = context;
        mPlace = place;
    }

    public void onLike(final int userId, final int mutualId
            , final OnRateRequestListener requestListener, boolean blockUnconfirmed) {
        sendRate(new SendLikeRequest(mContext, userId, mutualId, mPlace, blockUnconfirmed), requestListener);
    }

    public boolean onAdmiration(BalanceData balanceData, final int userId, final int mutualId
            , final OnRateRequestListener requestListener, Options options) {
        if (balanceData.money < options.priceAdmiration) {
            mContext.startActivity(PurchasesActivity.createBuyingIntent("RateAdmiration"
                    , PurchasesFragment.TYPE_ADMIRATION, options.priceAdmiration, options.topfaceOfferwallRedirect));
            if (mOnRateControllerUiListener != null) {
                mOnRateControllerUiListener.failRate();
            }
            if (requestListener != null) {
                requestListener.onRateFailed(userId, mutualId);
            }
            return false;
        }
        sendRate(new SendAdmirationRequest(mContext, userId, mutualId, mPlace, options.blockUnconfirmed), requestListener);
        return true;
    }

    private void sendRate(final SendLikeRequest sendLike, final OnRateRequestListener listener) {
        sendLike.callback(new DataApiHandler<Rate>() {

            @Override
            protected void success(Rate rate, IApiResponse response) {
                FlurryManager.getInstance().sendSpendCoinsEvent(App.get().getOptions().priceAdmiration, SEND_ADMIRATION);
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
        void successRate();

        void failRate();
    }

    /**
     * Interface for api request callbacks
     */
    public interface OnRateRequestListener {
        void onRateCompleted(int mutualId);

        void onRateFailed(int userId, int mutualId);
    }
}

package com.topface.topface.utils;

import android.support.v4.app.FragmentActivity;

import com.topface.topface.App;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.Options;
import com.topface.topface.data.Rate;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ReadLikeRequest;
import com.topface.topface.requests.SendAdmirationRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.utils.cache.SearchCacheManager;

import static com.topface.topface.requests.SendLikeRequest.FROM_FEED;
import static com.topface.topface.utils.FlurryManager.SEND_ADMIRATION;

public class RateController {

    @SendLikeRequest.Place
    private final int mPlace;
    private FragmentActivity mFragmentActivity;
    private OnRateControllerListener mOnRateControllerUiListener;

    public RateController(FragmentActivity fragmentActivity, @SendLikeRequest.Place int place) {
        mFragmentActivity = fragmentActivity;
        mPlace = place;
    }

    public void onLike(final int userId, final int mutualId
            , final OnRateRequestListener requestListener, boolean blockUnconfirmed) {
        sendRate(new SendLikeRequest(mFragmentActivity, userId, mutualId, mPlace, blockUnconfirmed), requestListener);
    }

    public boolean onAdmiration(BalanceData balanceData, final int userId, final int mutualId
            , final OnRateRequestListener requestListener, Options options) {
        if (balanceData.money < options.priceAdmiration) {
            mFragmentActivity.startActivity(PurchasesActivity.createBuyingIntent("RateAdmiration"
                    , PurchasesFragment.TYPE_ADMIRATION, options.priceAdmiration, options.topfaceOfferwallRedirect));
            if (mOnRateControllerUiListener != null) {
                mOnRateControllerUiListener.failRate();
            }
            if (requestListener != null) {
                requestListener.onRateFailed(userId, mutualId);
            }
            return false;
        }
        sendRate(new SendAdmirationRequest(mFragmentActivity, userId, mutualId, mPlace, options.blockUnconfirmed), requestListener);
        return true;
    }

    private void sendRate(final SendLikeRequest sendLike, final OnRateRequestListener listener) {
        sendLike.callback(new DataApiHandler<Rate>() {

            @Override
            protected void success(Rate rate, IApiResponse response) {
                if (sendLike.getServiceName().equals(SendAdmirationRequest.service)) {
                    FlurryManager.getInstance().sendSpendCoinsEvent(App.get().getOptions().priceAdmiration, SEND_ADMIRATION);
                }
                if (mPlace == FROM_FEED) {
                    new ReadLikeRequest(sendLike.getContext(), sendLike.getUserid()).exec();
                }
                if (listener != null) {
                    listener.onRateCompleted(sendLike.getMutualid(), sendLike.getUserid());
                }

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

    public void destroyController() {
        mOnRateControllerUiListener = null;
        mFragmentActivity = null;
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
        void onRateCompleted(int mutualId, int ratedUserId);

        void onRateFailed(int userId, int mutualId);
    }
}

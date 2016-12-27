package com.topface.topface.ui.fragments.feed.feed_base

import android.content.Intent
import android.databinding.ViewDataBinding
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedLike
import com.topface.topface.data.Rate
import com.topface.topface.requests.ApiRequest
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.handlers.ApiHandler
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.ads.AdmobInterstitialUtils
import com.topface.topface.utils.cache.SearchCacheManager
import rx.Subscriber
import rx.Subscription

/**
 * Created by ppavlik on 11.10.16.
 * Base viewModel for a common symphaties logic
 */
abstract class BaseSymphatiesItemViewModel
<T : ViewDataBinding>(binding: T, item: FeedLike, navigator: IFeedNavigator,
                      private val mApi: FeedApi, private val mHandleDuplicates: (Boolean, Int) -> Unit,
                      isActionModeEnabled: () -> Boolean) :
        BaseFeedItemViewModel<T, FeedLike>(binding, item, navigator, isActionModeEnabled) {

    abstract fun getReadItemRequest(): ApiRequest

    private var mSendLikeSubscription: Subscription? = null
    override val text: String?
        get() = item.user?.city?.name

    fun isMutualed() = item.mutualed

    fun onHeartClick() {
        val userId = item.getUserId()
        mHandleDuplicates(true, userId)
        mSendLikeSubscription = mApi.callSendLike(userId, App.get().options.blockUnconfirmed).subscribe(object : Subscriber<Rate>() {
            override fun onError(e: Throwable?) {
                item.mutualed = false
                if (e != null) {
                    e.message?.let {
                        if (it.equals(ErrorCodes.UNCONFIRMED_LOGIN)) {
                            Utils.showToastNotification(R.string.confirm_email_for_dating, Toast.LENGTH_SHORT)
                        } else {
                            Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_SHORT)
                        }
                    }
                }
            }

            override fun onCompleted() {
                Debug.log("Likes Feed Item Send like OK")
                RxUtils.safeUnsubscribe(mSendLikeSubscription)
            }

            override fun onNext(t: Rate?) {
                item.mutualed = true
                Utils.showToastNotification(R.string.general_mutual, Toast.LENGTH_SHORT)
                sendReadItemRequest(userId)
                SearchCacheManager.markUserAsRatedInCache(userId)
            }
        })
    }

    private fun sendReadItemRequest(userId: Int) = getReadItemRequest()
            .callback(object : ApiHandler() {
                override fun success(response: IApiResponse?) {
                    Debug.log("Likes Feed Item ReadLikeRequest OK")
                    val intent = Intent(ChatFragment.MAKE_ITEM_READ)
                    intent.putExtra(ChatFragment.INTENT_USER_ID, item.getUserId())
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                }

                override fun fail(codeError: Int, response: IApiResponse?) {
                    mHandleDuplicates(false, userId)
                    Utils.showErrorMessage()
                }
            })
            .exec()


    override fun onAvatarClickActionModeDisabled() {
        super.onAvatarClickActionModeDisabled()
        getReadItemRequest().exec()
        AdmobInterstitialUtils.
                requestPreloadedInterstitial(context, App.get().options.interstitial)
    }

    override fun release() {
        super.release()
        RxUtils.safeUnsubscribe(mSendLikeSubscription)
    }
}
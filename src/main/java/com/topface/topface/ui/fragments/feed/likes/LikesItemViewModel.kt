package com.topface.topface.ui.fragments.feed.likes

import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedLike
import com.topface.topface.data.Rate
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.ReadLikeRequest
import com.topface.topface.requests.handlers.ApiHandler
import com.topface.topface.ui.fragments.ChatFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedItemViewModel
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.ads.AdmobInterstitialUtils
import com.topface.topface.utils.cache.SearchCacheManager
import rx.Subscriber
import rx.Subscription

/**
 * VM для итема лайков
 * Created by tiberal on 15.08.16.
 */
class LikesItemViewModel(binding: FeedItemHeartBinding, item: FeedLike, navigator: IFeedNavigator,
                         private val mApi: FeedApi, isActionModeEnabled: () -> Boolean) :
        BaseFeedItemViewModel<FeedItemHeartBinding>(binding, item, navigator, isActionModeEnabled) {

    var city: String = Utils.EMPTY
    private var mSendLikeSubscription: Subscription? = null

    init {
        item.user?.let {
            city = it.city.name
        }
    }

    fun onHeartClick() {
        binding.heart.isActivated = true
        val userId = item.getUserId()
        mSendLikeSubscription = mApi.callSendLike(item.getUserId(), App.get().options.blockUnconfirmed).subscribe(object : Subscriber<Rate>() {
            override fun onError(e: Throwable?) {
                Utils.showToastNotification(R.string.confirm_email_for_dating, Toast.LENGTH_SHORT)
            }

            override fun onCompleted() {
                Debug.log("Likes Feed Item Send like OK")
            }

            override fun onNext(t: Rate?) {
                item.mutualed = true
                Utils.showToastNotification(R.string.general_mutual, Toast.LENGTH_SHORT)
                sendReadLikeRequest(userId)
                SearchCacheManager.markUserAsRatedInCache(userId)
            }
        })
    }

    private fun sendReadLikeRequest(userId: Int) {
        ReadLikeRequest(context, userId)
                .callback(object : ApiHandler() {
                    override fun success(response: IApiResponse?) {
                        Debug.log("Likes Feed Item ReadLikeRequest OK")
                        val intent = Intent(ChatFragment.MAKE_ITEM_READ)
                        intent.putExtra(ChatFragment.INTENT_ITEM_ID, item.id)
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    }

                    override fun fail(codeError: Int, response: IApiResponse?) {
                        binding.heart.isActivated = false
                        Utils.showErrorMessage()
                    }
                })
                .exec()
    }

    override fun getClickListenerForMultiselectHandle() = arrayOf<View.OnClickListener>(binding.clickListener)


    override fun onAvatarClickActionModeDisabled() {
        ReadLikeRequest(context, item.getUserId()).exec()
        AdmobInterstitialUtils.
                requestPreloadedInterstitial(context, App.get().options.interstitial)
    }

    override fun release() {
        super.release()
        RxUtils.safeUnsubscribe(mSendLikeSubscription)
    }

}
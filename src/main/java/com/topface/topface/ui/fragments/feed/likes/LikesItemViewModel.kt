package com.topface.topface.ui.fragments.feed.likes

import android.widget.Toast
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedLike
import com.topface.topface.data.Photo
import com.topface.topface.data.Rate
import com.topface.topface.databinding.FeedItemHeartBinding
import com.topface.topface.requests.ReadLikeRequest
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_utils.AgeAndNameData
import com.topface.topface.ui.fragments.feed.feed_utils.getUserId
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.ads.AdmobInterstitialUtils
import com.topface.topface.utils.cache.SearchCacheManager
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscriber
import rx.Subscription

/**
 * VM для итема лайков
 * Created by tiberal on 15.08.16.
 */
class LikesItemViewModel(binding: FeedItemHeartBinding, val item: FeedLike, private val mNavigator: IFeedNavigator,
                         private val mApi: FeedApi, private val mIsActionModeEnabled: () -> Boolean) : BaseViewModel<FeedItemHeartBinding>(binding) {

    var photo: Photo? = null
    var city: String = Utils.EMPTY
    var nameAndAge: AgeAndNameData? = null
    private var mSendLikeSubscription: Subscription? = null

    init {
        item.user?.let {
            photo = it.photo
            city = it.city.name
            nameAndAge = AgeAndNameData(it.nameAndAge, "", getOnlineRes())
        }
    }

    private fun getOnlineRes() =
            if (!(item.user.deleted || item.user.banned) && item.user.online)
                R.drawable.im_list_online
            else
                0


    fun onHeartClick() {
        Utils.showToastNotification("HEART", Toast.LENGTH_LONG)
        val userId = item.getUserId()
        mSendLikeSubscription = mApi.callSendLike(item.getUserId(), App.get().options.blockUnconfirmed).subscribe(object : Subscriber<Rate>() {
            override fun onError(e: Throwable?) {
                Utils.showToastNotification(R.string.confirm_email_for_dating, Toast.LENGTH_SHORT)
            }

            override fun onCompleted() = Debug.log("Send like OK")
            override fun onNext(t: Rate?) {
                ReadLikeRequest(context, userId).exec()
                SearchCacheManager.markUserAsRatedInCache(userId)
            }
        })
    }

    fun onAvatarClick() {
        if (mIsActionModeEnabled.invoke()) {
            binding.clickListener.onClick(binding.root)
        } else {
            mNavigator.showProfile(item)
            ReadLikeRequest(context, item.getUserId()).exec()
            AdmobInterstitialUtils.
                    requestPreloadedInterstitial(context, App.get().options.interstitial)
        }
    }

    override fun release() {
        super.release()
        RxUtils.safeUnsubscribe(mSendLikeSubscription)
    }

}
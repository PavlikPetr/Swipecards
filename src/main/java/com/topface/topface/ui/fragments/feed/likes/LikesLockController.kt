package com.topface.topface.ui.fragments.feed.likes

import android.databinding.ViewStubProxy
import com.topface.statistics.generated.NewProductsKeysGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.databinding.LayoutEmptyLikesBinding
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscription

/**
 * Расширение контроллера заглушек для фрагмента лайков
 * Created by tiberal on 12.08.16.
 */
class LikesLockController(stub: ViewStubProxy) : BaseFeedLockerController<LayoutEmptyLikesBinding, LikesLockScreenViewModel>(stub) {

    private var mLikesBlokedSubscription: Subscription? = null

    override fun initLockedFeedStub(errorCode: Int) {
        when (errorCode) {
            ErrorCodes.BLOCKED_SYMPATHIES, ErrorCodes.PREMIUM_ACCESS_ONLY -> {
                mLikesBlokedSubscription = NewProductsKeysGeneratedStatistics.sendPost_LIKES_BLOCKED_OPEN(App.getContext())
                mStubModel?.currentChildPod?.set(1)
            }
        }
    }

    override fun initEmptyFeedStub() {
        mStubModel?.currentChildPod?.set(0)
    }

    override fun release() {
        super.release()
        mLikesBlokedSubscription.safeUnsubscribe()
    }

}
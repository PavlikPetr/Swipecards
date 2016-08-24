package com.topface.topface.ui.fragments.feed.likes

import android.databinding.ViewStubProxy
import com.topface.topface.databinding.LayoutEmptyLikesBinding
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

/**
 * Расширение контроллера заглушек для фрагмента лайков
 * Created by tiberal on 12.08.16.
 */
class LikesLockController(stub: ViewStubProxy) : BaseFeedLockerController<LayoutEmptyLikesBinding, LikesLockScreenViewModel>(stub) {

    override fun initLockedFeedStub(errorCode: Int) {
        when (errorCode) {
            ErrorCodes.BLOCKED_SYMPATHIES, ErrorCodes.PREMIUM_ACCESS_ONLY -> {
                mStubModel?.currentChildPod?.set(1)
            }
        }
    }

    override fun initEmptyFeedStub() {
        mStubModel?.currentChildPod?.set(0)
    }

}
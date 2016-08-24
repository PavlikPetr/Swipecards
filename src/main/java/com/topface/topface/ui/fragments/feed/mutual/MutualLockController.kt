package com.topface.topface.ui.fragments.feed.mutual

import android.databinding.ViewStubProxy
import com.topface.framework.utils.Debug
import com.topface.topface.databinding.LayoutEmptyMutualBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

/**
 * Контроллер заглушек взаимных. На данный моммент 1 заглушка, контроллер нужен только для типа и логирования
 * Created by tiberal on 22.08.16.
 */
class MutualLockController(stub: ViewStubProxy) : BaseFeedLockerController<LayoutEmptyMutualBinding, MutualLockScreenViewModel>(stub) {

    override fun initLockedFeedStub(errorCode: Int) {
        Debug.log("MUTUAL: error $errorCode")
    }

    override fun initEmptyFeedStub() {
        Debug.log("MUTUAL: Shown default empty scree")
    }
}
package com.topface.topface.ui.fragments.feed.dialogs

import android.databinding.ViewStubProxy
import com.topface.topface.databinding.LayoutEmptyDialogsBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

/**
 * Created by tiberal on 18.09.16.
 */
class DialogsLockController(stub: ViewStubProxy) : BaseFeedLockerController<LayoutEmptyDialogsBinding, DialogsLockScreenViewModel>(stub) {

    override fun initLockedFeedStub(errorCode: Int) {

    }

    override fun initEmptyFeedStub() {
    }
}
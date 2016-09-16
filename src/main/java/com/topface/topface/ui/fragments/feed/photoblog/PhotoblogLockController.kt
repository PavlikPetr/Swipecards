package com.topface.topface.ui.fragments.feed.photoblog

import android.databinding.ViewStubProxy
import com.topface.topface.databinding.LayoutEmptyPhotoblogBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

/**
 * Created by tiberal on 05.09.16.
 */
class PhotoblogLockController(viewStubProxy: ViewStubProxy) :
        BaseFeedLockerController<LayoutEmptyPhotoblogBinding, PhotoblogLockScreenViewModel>(viewStubProxy) {

    override fun initEmptyFeedStub() {
        /*
         * в старом коде должен был показываться 0 child фдиппера, которы в layout_empty_photoblog
         * но детей у флиппера нет так что непонятно что показывать
         */

    }

    override fun initLockedFeedStub(errorCode: Int) {

    }
}
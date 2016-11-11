package com.topface.topface.ui.fragments.feed.dating

import android.widget.Toast
import com.topface.topface.R
import com.topface.topface.data.DatingFilter
import com.topface.topface.databinding.LayoutEmptyDatingBinding
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.safeUnsubscribe
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscriber
import rx.Subscription


/**
 * Created by mbulgakov on 07.11.16.
 */
open class DatingEmptyFragmentViewModel(binding: LayoutEmptyDatingBinding,
                                        private val mApi: FeedApi,
                                        private val mNavigator: FeedNavigator,
                                        private val iDialogCloser: IDialogCloser) : BaseViewModel<LayoutEmptyDatingBinding>(binding) {

    private var mClearDatingFilterSubscriber: Subscription? = null

    fun onCleanDatingFilter() = cleanDatingFilter()

    fun onChangeDatingFilter() = mNavigator.showFilter()

    private fun cleanDatingFilter() {
        mClearDatingFilterSubscriber = mApi.callResetFilterRequest().subscribe(object : Subscriber<DatingFilter>() {
            override fun onCompleted() = mClearDatingFilterSubscriber.safeUnsubscribe()

            override fun onError(e: Throwable?) = Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_LONG)

            override fun onNext(t: DatingFilter?) = iDialogCloser.closeIt()

        })
    }

    override fun release() {
        mClearDatingFilterSubscriber.safeUnsubscribe()
        super.release()
    }

}
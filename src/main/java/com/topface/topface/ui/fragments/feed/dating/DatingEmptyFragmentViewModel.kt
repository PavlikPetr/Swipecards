package com.topface.topface.ui.fragments.feed.dating

import android.app.Dialog
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
open class DatingEmptyFragmentViewModel(val dialog: Dialog,
                                        binding: LayoutEmptyDatingBinding,
                                        private val mApi: FeedApi,
                                        private val mNavigator: FeedNavigator) : BaseViewModel<LayoutEmptyDatingBinding>(binding) {

    private var mClearDatingFilterSubscriber: Subscription? = null

    fun onCleanDatingFilter() {
        cleanDatingFilter()

    }

    fun onChangeDatingFilter() {
        startDatingFilterActivity()
    }

    private fun cleanDatingFilter() {
        mClearDatingFilterSubscriber = mApi.callResetFilterRequest().subscribe(object : Subscriber<DatingFilter>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_LONG);
            }

            override fun onNext(t: DatingFilter?) {
                dialog.cancel()
            }

        })
    }

    private fun startDatingFilterActivity() {
        mNavigator.showFilter()
    }

    override fun release() {
        mClearDatingFilterSubscriber.safeUnsubscribe()
        super.release()
    }

}
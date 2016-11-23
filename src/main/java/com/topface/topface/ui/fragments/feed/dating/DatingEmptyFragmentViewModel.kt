package com.topface.topface.ui.fragments.feed.dating

import android.widget.Toast
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.DatingFilter
import com.topface.topface.databinding.LayoutEmptyDatingBinding
import com.topface.topface.state.AppState
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.safeUnsubscribe
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscriber
import rx.Subscription
import javax.inject.Inject

open class DatingEmptyFragmentViewModel(binding: LayoutEmptyDatingBinding,
                                        private val mApi: FeedApi,
                                        private val mNavigator: FeedNavigator,
                                        private val iDialogCloser: IDialogCloser) : BaseViewModel<LayoutEmptyDatingBinding>(binding) {

    @Inject lateinit var state: TopfaceAppState

    init {
        App.get().inject(this)
    }

    private var mClearDatingFilterSubscriber: Subscription? = null

    fun onCleanDatingFilter() = cleanDatingFilter()

    fun onChangeDatingFilter() = mNavigator.showFilter()

    private fun cleanDatingFilter() {
        mClearDatingFilterSubscriber = mApi.callResetFilterRequest().subscribe(object : Subscriber<DatingFilter>() {
            override fun onCompleted() = mClearDatingFilterSubscriber.safeUnsubscribe()

            override fun onError(e: Throwable?) = Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_LONG)

            override fun onNext(filter: DatingFilter?) {
                if (filter != null) {
                    val profile = App.get().profile
                    profile.dating = filter
                    state.setData(profile)
                }
                iDialogCloser.closeIt()
            }
        })
    }


    override fun release() {
        mClearDatingFilterSubscriber.safeUnsubscribe()
        super.release()
    }

}
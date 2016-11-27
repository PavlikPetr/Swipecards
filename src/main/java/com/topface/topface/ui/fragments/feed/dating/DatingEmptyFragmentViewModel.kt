package com.topface.topface.ui.fragments.feed.dating

import com.topface.topface.App
import com.topface.topface.databinding.LayoutEmptyDatingBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.extensions.safeUnsubscribe
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription
import javax.inject.Inject

open class DatingEmptyFragmentViewModel(binding: LayoutEmptyDatingBinding,
                                        private val mNavigator: FeedNavigator,
                                        private val iDialogCloser: IDialogCloser) : BaseViewModel<LayoutEmptyDatingBinding>(binding) {

    @Inject lateinit var state: TopfaceAppState

    init {
        App.get().inject(this)
    }

    private var mClearDatingFilterSubscriber: Subscription? = null

    fun onCleanDatingFilter() = cleanDatingFilter()

    fun onChangeDatingFilter() = mNavigator.showFilter()

    //как будет новый скраффи. отсюда слать апдейт.ловить в модели фрагмента. а на onNext запроса закрывать попап
    private fun cleanDatingFilter() = iDialogCloser.closeIt()

    override fun release() {
        mClearDatingFilterSubscriber.safeUnsubscribe()
        super.release()
    }

}
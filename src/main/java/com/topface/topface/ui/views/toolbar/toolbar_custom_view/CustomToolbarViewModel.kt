package com.topface.topface.ui.views.toolbar.toolbar_custom_view

import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.databinding.CustomTitleAndSubtitleToolbarAdditionalViewBinding
import com.topface.topface.utils.rx.RxFieldObservable
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.viewModels.BaseViewModel
import rx.subscriptions.CompositeSubscription

/**
 * Created by ppavlik on 07.11.16.
 * Вьюмодель для кастомной вью, которая содержит title, subtitle и может online
 */

class CustomToolbarViewModel(binding: CustomTitleAndSubtitleToolbarAdditionalViewBinding) : BaseViewModel<CustomTitleAndSubtitleToolbarAdditionalViewBinding>(binding) {
    val title = RxFieldObservable<String>()
    val subTitle = RxFieldObservable<String>()
    val titleVisibility = ObservableInt(View.VISIBLE)
    val subTitleVisibility = ObservableInt(View.VISIBLE)
    val isOnline = ObservableBoolean()

    private val subscriptions = CompositeSubscription()

    init {
        subscriptions.add(title.filedObservable.subscribe { Debug.error("CUSTOM_VIEW_TEST title = $it") })
        subscriptions.add(subTitle.filedObservable.subscribe { Debug.error("CUSTOM_VIEW_TEST subTitle = $it") })
    }

    override fun release() {
        super.release()
        subscriptions.safeUnsubscribe()
    }
}
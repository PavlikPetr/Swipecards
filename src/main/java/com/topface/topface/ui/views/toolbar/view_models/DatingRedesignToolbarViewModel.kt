package com.topface.topface.ui.views.toolbar.view_models

import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.CountersData
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.utils.Utils
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription
import javax.inject.Inject

/**
 *  toolbar для редизайна знакомств
 */
class DatingRedesignToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding, mNavigation: IToolbarNavigation? = null)
    : BaseToolbarViewModel(binding, mNavigation) {

    @Inject lateinit var mState: TopfaceAppState
    private val mNotificationSubscription: Subscription

    init {
        App.get().inject(this)
        background.set(R.color.transparent)
        upIcon.set(R.drawable.menu_white)
        updateTopPadding()

        shadowVisibility.set(View.GONE)
        title.set(Utils.EMPTY)
        subTitle.set(Utils.EMPTY)

        mNotificationSubscription = mState.getObservable(CountersData::class.java).subscribe(shortSubscription {
            upIcon.set(if (it.dialogs > 0 || it.mutual > 0 || it.likes > 0) R.drawable.menu_white_notification else R.drawable.menu_white)
        })
    }

    override fun release() {
        super.release()
        mNotificationSubscription.safeUnsubscribe()
    }
}
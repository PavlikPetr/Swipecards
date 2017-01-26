package com.topface.topface.ui.views.toolbar.view_models

import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.CountersData
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.utils.Utils
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.rx.safeUnsubscribe
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
        background.set(R.drawable.newdating_toolbar_background)
        upIcon.set(R.drawable.menu_white)
        updateTopPadding()

        shadowVisibility.set(View.GONE)
        title.set(Utils.EMPTY)
        subTitle.set(Utils.EMPTY)
        shadowVisibility.set(View.INVISIBLE)

        mNotificationSubscription = mState.getObservable(CountersData::class.java).subscribe(object : RxUtils.ShortSubscription<CountersData>() {
            override fun onNext(type: CountersData?) {
                type?.let {
                    if (it.dialogs > 0 || it.mutual > 0 || it.likes > 0) upIcon.set(R.drawable.menu_white_notification)
                }
                super.onNext(type)
            }
        })
    }

    override fun release() {
        super.release()
        mNotificationSubscription.safeUnsubscribe()
    }
}
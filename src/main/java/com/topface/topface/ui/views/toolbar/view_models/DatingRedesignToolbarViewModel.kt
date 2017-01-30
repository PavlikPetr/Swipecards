package com.topface.topface.ui.views.toolbar.view_models

import android.databinding.ObservableField
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.CountersData
import com.topface.topface.data.FragmentLifreCycleData
import com.topface.topface.data.FragmentLifreCycleData.*
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.state.LifeCycleState
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.dating.dating_redesign.DatingFragment
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.appContext
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.Subscription
import javax.inject.Inject
import kotlin.properties.Delegates

/**
 *  toolbar для редизайна знакомств
 */
class DatingRedesignToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding, mNavigation: IToolbarNavigation? = null)
    : BaseToolbarViewModel(binding, mNavigation) {

    @Inject lateinit var mState: TopfaceAppState
    @Inject lateinit var mLifeCycleReporter: LifeCycleState
    val contentMarginTop = ObservableField(0)
    private val mNotificationSubscription: Subscription
    private val mFragmentLifecycleSubscription: Subscription
    private var mIsDating by Delegates.observable(false) { prop, old, new ->
        contentMarginTop.set(if (new) 0 else binding.root.measuredHeight)
        redrawUpIcon()
        shadowVisibility.set(if (new) View.GONE else View.VISIBLE)
    }
    private var mHasNotification by Delegates.observable(false) { prop, old, new ->
        redrawUpIcon()
    }

    init {
        App.get().inject(this)
        background.set(R.color.transparent)
        upIcon.set(R.drawable.menu_white)
        updateTopPadding()

        shadowVisibility.set(View.GONE)
        title.set(Utils.EMPTY)
        subTitle.set(Utils.EMPTY)

        mNotificationSubscription = mState.getObservable(CountersData::class.java)
                .distinctUntilChanged()
                .flatMap { Observable.just(it.dialogs > 0 || it.mutual > 0 || it.likes > 0) }
                .filter { mHasNotification != it }
                .subscribe(shortSubscription {
                    mHasNotification = it
                })
        mFragmentLifecycleSubscription = mLifeCycleReporter
                .getObservable(FragmentLifreCycleData::class.java)
                .filter { it.className == DatingFragment::class.java.name }
                .subscribe(shortSubscription {
                    when (it.state) {
                        RESUME, ATTACH, CREATE, CREATE_VIEW, VIEW_CREATED, START -> mIsDating = true
                        PAUSE, DESTROY_VIEW, STOP, DESTROY, DETACH -> mIsDating = false
                    }
                })
    }

    private fun redrawUpIcon() {
        upIcon.set(if (mHasNotification) {
            if (mIsDating) R.drawable.menu_white_notification else R.drawable.menu_gray_notification
        } else {
            if (mIsDating) R.drawable.menu_white else R.drawable.menu_gray
        })
    }


    override fun release() {
        super.release()
        mNotificationSubscription.safeUnsubscribe()
        mFragmentLifecycleSubscription.safeUnsubscribe()
    }
}
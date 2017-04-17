package com.topface.topface.ui.views.toolbar.view_models

import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.CountersData
import com.topface.topface.data.FragmentLifreCycleData
import com.topface.topface.data.FragmentLifreCycleData.*
import com.topface.topface.databinding.CustomTitleAndSubtitleToolbarAdditionalViewBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.fragments.dating.design.v1.DatingFragment
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.ui.views.toolbar.toolbar_custom_view.CustomToolbarViewModel
import com.topface.topface.utils.Utils
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import kotlin.properties.Delegates

/**
 *  toolbar для редизайна знакомств
 */
class DatingRedesignToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding, mNavigation: IToolbarNavigation? = null)
    : BaseToolbarViewModel(binding, mNavigation) {

    private val mState by lazy {
        App.getAppComponent().appState()
    }
    private val mLifeCycleReporter by lazy {
        App.getAppComponent().lifeCycleState()
    }
    var extraViewModel: CustomToolbarViewModel? = null
    val contentMarginTop = ObservableInt(0)
    private val mNotificationSubscription: Subscription
    private val mFragmentLifecycleSubscription: Subscription
    private val mSubscriptions = CompositeSubscription()
    private var isEmptyContentMargin = true
    val contentShadowVisibility = ObservableInt(View.VISIBLE)

    var isDating by Delegates.observable(false) { prop, old, new ->
        isEmptyContentMargin = new
        background.set(if (new) R.color.transparent else R.color.toolbar_background)
        redrawUpIcon()
        shadowVisibility.set(View.GONE)
        contentShadowVisibility.set(if (new) View.GONE else View.VISIBLE)
        extraViewModel?.apply {
            titleVisibility.set(if (!new && !TextUtils.isEmpty(title.get()))
                View.VISIBLE
            else
                View.GONE)
            subTitleVisibility.set(if (!new && !TextUtils.isEmpty(subTitle.get()))
                View.VISIBLE
            else
                View.GONE)
        }
        if (new) {
            title.set(Utils.EMPTY)
            subTitle.set(Utils.EMPTY)
            contentMarginTop.set(0)
        } else {
            with(binding.root) {
                post {
                    if (!isEmptyContentMargin) {
                        contentMarginTop.set(measuredHeight)
                    }
                }
            }
        }
        updateTopPadding()
    }

    private var mHasNotification by Delegates.observable(false) { prop, old, new ->
        redrawUpIcon()
    }

    init {
        title.set(Utils.EMPTY)
        subTitle.set(Utils.EMPTY)
        isDating = false
        with(DataBindingUtil.inflate<CustomTitleAndSubtitleToolbarAdditionalViewBinding>(LayoutInflater.from(context),
                R.layout.custom_title_and_subtitle_toolbar_additional_view, null, false)) {
            binding.toolbarCustomView.addView(root)
            extraViewModel = CustomToolbarViewModel(this)
            viewModel = extraViewModel
        }
        mSubscriptions.add(title.filedObservable
                .subscribe(shortSubscription { it?.let { extraViewModel?.title?.set(it) } }))
        mSubscriptions.add(subTitle.filedObservable
                .subscribe(shortSubscription { it?.let { extraViewModel?.subTitle?.set(it) } }))
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
                    Debug.error("LIFE_CYCLE catch class ${it.className} state ${it.state}")
                    when (it.state) {
                        RESUME, ATTACH, CREATE, CREATE_VIEW, VIEW_CREATED, START -> isDating = true
                        DESTROY_VIEW, STOP, DESTROY, DETACH -> isDating = false
                    }
                })
    }

    private fun redrawUpIcon() {
        upIcon.set(if (mHasNotification) {
            if (isDating) R.drawable.menu_white_notification else R.drawable.menu_gray_notification
        } else {
            if (isDating) R.drawable.menu_white else R.drawable.menu_gray
        })
    }

    override fun release() {
        super.release()
        mNotificationSubscription.safeUnsubscribe()
        mFragmentLifecycleSubscription.safeUnsubscribe()
        mSubscriptions.safeUnsubscribe()
    }
}
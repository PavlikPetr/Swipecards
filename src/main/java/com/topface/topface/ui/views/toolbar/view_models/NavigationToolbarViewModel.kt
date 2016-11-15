package com.topface.topface.ui.views.toolbar.view_models

import android.databinding.DataBindingUtil
import android.databinding.ObservableBoolean
import android.view.LayoutInflater
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.CountersData
import com.topface.topface.data.Profile
import com.topface.topface.databinding.CustomTitleAndSubtitleToolbarAdditionalViewBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.ui.views.toolbar.toolbar_custom_view.CustomToolbarViewModel
import com.topface.topface.utils.RxFieldObservable
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.safeUnsubscribe
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * Created by petrp on 09.10.2016.
 * вью модель тулбара для чата
 */

class NavigationToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding, mNavigation: IToolbarNavigation? = null)
    : BaseToolbarViewModel(binding, mNavigation) {
    @Inject lateinit var mState: TopfaceAppState
    var extraViewModel: CustomToolbarViewModel
    val isCollapsingToolbar = RxFieldObservable<Boolean>(false)

    private var mHasNotification: Boolean? = null
    private val subscriptions = CompositeSubscription()
    private var ownProfile: Profile = App.get().profile

    init {
        App.get().inject(this)
        title.set(Utils.EMPTY)
        subTitle.set(Utils.EMPTY)
        shadowVisibility.set(View.GONE)
        val additionalViewBinding = DataBindingUtil.inflate<CustomTitleAndSubtitleToolbarAdditionalViewBinding>(LayoutInflater.from(context),
                R.layout.custom_title_and_subtitle_toolbar_additional_view, null, false)
        binding.toolbarCustomView.addView(additionalViewBinding.root)
        extraViewModel = CustomToolbarViewModel(additionalViewBinding)
        additionalViewBinding.viewModel = extraViewModel
        subscriptions.add(title.filedObservable.subscribe {
            Debug.error("TOOLBAR title = $it")
            extraViewModel.title.set(it)
        })
        subscriptions.add(subTitle.filedObservable.subscribe { extraViewModel.subTitle.set(it) })
        subscriptions.add(mState.getObservable(CountersData::class.java)
                .map {
                    it.dialogs > 0 || it.mutual > 0
                }
                .filter {
                    mHasNotification == null || mHasNotification != it
                }
                .subscribe(object : RxUtils.ShortSubscription<Boolean>() {
                    override fun onNext(isHasNotif: Boolean?) {
                        super.onNext(isHasNotif)
                        mHasNotification = isHasNotif
                        setUpIconStyle(isCollapsingToolbar.get(), mHasNotification)
                    }
                }))
        subscriptions.add(mState.getObservable(Profile::class.java).subscribe(object : RxUtils.ShortSubscription<Profile>() {
            override fun onNext(profile: Profile?) {
                profile?.let { ownProfile = it }
            }
        }))
        subscriptions.add(isCollapsingToolbar.filedObservable.subscribe { setCorrectStyle(it) })
    }

    private fun setCorrectStyle(isCollapsed: Boolean) {
        setUpIconStyle(isCollapsed, mHasNotification)
        background.set(if (isCollapsed) R.drawable.tool_bar_gradient else R.color.toolbar_background)
        if (isCollapsed) {
            subTitle.set(Utils.EMPTY)
        }
    }

    private fun setUpIconStyle(isCollapsed: Boolean, isHasNotification: Boolean?) =
            upIcon.set(if (isHasNotification ?: false)
                if (isCollapsed) R.drawable.menu_white_notification else R.drawable.menu_gray_notification
            else
                if (isCollapsed) R.drawable.menu_white else R.drawable.menu_gray)

    override fun release() {
        super.release()
        subscriptions.safeUnsubscribe()
        extraViewModel.release()
    }
}

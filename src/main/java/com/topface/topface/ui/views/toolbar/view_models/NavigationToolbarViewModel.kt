package com.topface.topface.ui.views.toolbar.view_models

import android.databinding.DataBindingUtil
import android.databinding.ObservableBoolean
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.CountersData
import com.topface.topface.data.Profile
import com.topface.topface.databinding.CustomTitleAndSubtitleToolbarAdditionalViewBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.fragments.feed.toolbar.IAppBarState
import com.topface.topface.ui.views.toolbar.IToolbarNavigation
import com.topface.topface.ui.views.toolbar.toolbar_custom_view.CustomToolbarViewModel
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.isHasNotification
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.subscriptions.CompositeSubscription

/**
 * Created by petrp on 09.10.2016.
 * вью модель тулбара для чата
 */

class NavigationToolbarViewModel @JvmOverloads constructor(binding: ToolbarBinding, mNavigation: IToolbarNavigation? = null)
    : BaseToolbarViewModel(binding, mNavigation), IAppBarState {

    private val mState by lazy {
        App.getAppComponent().appState()
    }
    var extraViewModel: CustomToolbarViewModel? = null
    val isCollapsStyle = object : ObservableBoolean() {
        override fun set(value: Boolean) {
            super.set(value)
            setCorrectStyle(value)
            isScrimVisible.set(!value)
        }
    }
    val isScrimVisible = object : ObservableBoolean() {
        override fun set(value: Boolean) {
            super.set(value)
            // тулбар с градиентом только до тех пор, пока не началась анимация по переходу между CollapsingToolbar и Toolbar
            background.set(if (value) 0 else R.drawable.tool_bar_gradient)
            // Для того чтобы на белом фоне тулбара была видна кнопка гамбургер-меню необходимо выполнить
            // замену ресурсов в момент перехода между CollapsingToolbar и Toolbar
            with(upIcon) {
                // проверяем текущий ресурс, это надо чтобы заменить серую иконку с индикатором нотификаций
                // на такую же, но белую и наоборот
                if (get().isHasNotification())
                    set(if (value)
                        R.drawable.menu_gray_notification
                    else
                        R.drawable.menu_white_notification)
                else
                    set(if (value)
                        R.drawable.menu_gray
                    else
                        R.drawable.menu_white)
            }
            extraViewModel?.apply {
                titleVisibility.set(if (value && !TextUtils.isEmpty(title.get()))
                    View.VISIBLE
                else
                    View.GONE)
                subTitleVisibility.set(if (value && !TextUtils.isEmpty(subTitle.get()))
                    View.VISIBLE
                else
                    View.GONE)
            }
        }
    }

    private var mHasNotification: Boolean? = null
    private val subscriptions = CompositeSubscription()
    private var ownProfile: Profile = App.get().profile

    init {
        isCollapsStyle.set(false)
        title.set(Utils.EMPTY)
        subTitle.set(Utils.EMPTY)
        shadowVisibility.set(View.GONE)
        val additionalViewBinding = DataBindingUtil.inflate<CustomTitleAndSubtitleToolbarAdditionalViewBinding>(LayoutInflater.from(context),
                R.layout.custom_title_and_subtitle_toolbar_additional_view, null, false)
        binding.toolbarCustomView.addView(additionalViewBinding.root)
        extraViewModel = CustomToolbarViewModel(additionalViewBinding)
        additionalViewBinding.viewModel = extraViewModel
        subscriptions.add(title.filedObservable.subscribe { extraViewModel?.title?.set(it) })
        subscriptions.add(subTitle.filedObservable.subscribe { extraViewModel?.subTitle?.set(it) })
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
                        setUpIconStyle(isCollapsStyle.get(), mHasNotification)
                    }
                }))
        subscriptions.add(mState.getObservable(Profile::class.java).subscribe(object : RxUtils.ShortSubscription<Profile>() {
            override fun onNext(profile: Profile?) {
                profile?.let { ownProfile = it }
            }
        }))
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

    override fun isScrimVisible(isVisible: Boolean) = isScrimVisible.set(isVisible)

    override fun release() {
        super.release()
        subscriptions.safeUnsubscribe()
        extraViewModel?.release()
    }
}

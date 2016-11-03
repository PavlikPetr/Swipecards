package com.topface.topface.ui.views.toolbar

import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.CountersData
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.extensions.getColor
import rx.Subscription
import javax.inject.Inject

/**
 * Created by petrp on 09.10.2016.
 * вьюмодель тулбара для NavigationActivity
 */

open class NavigationToolbarViewModel @JvmOverloads constructor(val toolbar: ToolbarBinding, mNavigation: IToolbarNavigation? = null)
: BaseToolbarViewModel(toolbar, mNavigation) {

    companion object {
        const val EMPTY_TITLE = ""
    }

    @Inject lateinit var mState: TopfaceAppState
    private var mBalanceSubscription: Subscription? = null
    private var mHasNotification: Boolean? = null

    private var isCollapsingToolbar = false

    init {
        App.get().inject(this)
        mBalanceSubscription = mState.getObservable(CountersData::class.java)
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
                        setUpIconStyle(mHasNotification)
                    }
                })
        setCorrectStyle()
    }

    private fun setUpIconStyle(isHasNotification: Boolean?) {
        upIcon.set(if (isHasNotification ?: false)
            if (isCollapsingToolbar) R.drawable.menu_white_notification else R.drawable.menu_gray_notification
        else
            if (isCollapsingToolbar) R.drawable.menu_white else R.drawable.menu_gray)
    }


    private fun setCorrectStyle() {
        setUpIconStyle(mHasNotification)
        shadowVisibility.set(if (isCollapsingToolbar) View.GONE else View.VISIBLE)
        background.set(if (isCollapsingToolbar) R.drawable.tool_bar_gradient else R.color.toolbar_background)
        if (isCollapsingToolbar) {
//            title.set(EMPTY_TITLE)
//            subTitle.set(EMPTY_TITLE)
        }
        titleTextColor.set((if (isCollapsingToolbar) R.color.toolbar_light_title_color else R.color.toolbar_dark_title_color).getColor())
    }

    fun isCollapsingToolbarStyle(isCollapsing: Boolean) = apply {
        isCollapsingToolbar = isCollapsing
        setCorrectStyle()
    }

    override fun release() {
        super.release()
        RxUtils.safeUnsubscribe(mBalanceSubscription)
    }
}

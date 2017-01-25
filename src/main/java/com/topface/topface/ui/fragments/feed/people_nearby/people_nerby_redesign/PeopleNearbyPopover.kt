package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.content.Context
import android.databinding.DataBindingUtil
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.leftMenu.DrawerLayoutStateData
import com.topface.topface.databinding.PeopleNearbyPopoverBinding
import com.topface.topface.state.DrawerLayoutState
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.jetbrains.anko.layoutInflater
import rx.Subscription
import java.util.*
import javax.inject.Inject

/**
 * Поповер для экрана Люди Рядом
 * Created by petrp on 20.01.2017.
 */
class PeopleNearbyPopover(private val mContext: Context, private val mNavigator: FeedNavigator,
                          private val anchorView: () -> View) : IPopoverControl {
    @Inject lateinit var mDrawerLayoutState: DrawerLayoutState

    companion object {
        private const val POPOVER_SHOW_DELAY = 24 * 60 * 60 * 1000
    }

    private val mBinding: PeopleNearbyPopoverBinding by lazy {
        DataBindingUtil.bind<PeopleNearbyPopoverBinding>(mContext.layoutInflater.inflate(R.layout.people_nearby_popover, null))
    }
    private val mViewModel: PeopleNearbyPopoverViewModel by lazy {
        PeopleNearbyPopoverViewModel(mNavigator) { closeByUser() }
    }
    private val mPopupVindow: PopupWindow by lazy {
        PopupWindow(mBinding.apply { viewModel = mViewModel }.root, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private var mDrawerStateSubscription: Subscription
    private var mDrawerClosedSubscription: Subscription? = null
    private var mDrawerLayoutData: DrawerLayoutStateData? = null

    init {
        App.get().inject(this)
        mDrawerStateSubscription = mDrawerLayoutState
                .observable
                .subscribe(shortSubscription {
                    mDrawerLayoutData = it
                    if (it.state == DrawerLayoutStateData.SLIDE) {
                        closeProgrammatically()
                    }
                })
    }

    override fun closeByUser() {
        if (mPopupVindow.isShowing) {
            App.getUserConfig().apply {
                peopleNearbyPopoverClose = Calendar.getInstance().timeInMillis
                saveConfig()
            }
            mDrawerClosedSubscription.safeUnsubscribe()
            mPopupVindow.dismiss()
        }
    }

    override fun closeProgrammatically() = mPopupVindow.dismiss()

    override fun show() {
        if (!mPopupVindow.isShowing) {
            if (Calendar.getInstance().timeInMillis >=
                    App.getUserConfig().peopleNearbyPopoverClose + POPOVER_SHOW_DELAY) {
                if (mDrawerLayoutData?.state == DrawerLayoutStateData.CLOSED) {
                    showPopupImmediately()
                } else {
                    mDrawerClosedSubscription = mDrawerLayoutState
                            .observable
                            .filter { it.state == DrawerLayoutStateData.CLOSED }
                            .first()
                            .subscribe(shortSubscription {
                                showPopupImmediately()
                            })
                }
            }
        }
    }

    private fun showPopupImmediately() = mPopupVindow.showAsDropDown(anchorView.invoke())

    fun release() = mDrawerStateSubscription.safeUnsubscribe()
}

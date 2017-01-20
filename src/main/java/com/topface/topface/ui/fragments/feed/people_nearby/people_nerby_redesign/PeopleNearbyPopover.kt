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
        private const val POPOVER_SHOW_DELAY = 24 * 60 * 60
    }

    private val mBinding: PeopleNearbyPopoverBinding by lazy {
        DataBindingUtil.bind<PeopleNearbyPopoverBinding>(mContext.layoutInflater.inflate(R.layout.people_nearby_popover, null))
    }
    private val mViewModel: PeopleNearbyPopoverViewModel by lazy {
        PeopleNearbyPopoverViewModel(mNavigator) { closeByUser() }
    }
    private val mPopupVindow: PopupWindow by lazy {
        PopupWindow(mBinding.apply { viewModel = mViewModel }.getRoot(), LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    private val mDrawerStateSubscription: Subscription
    private var mDrawerLayoutData: DrawerLayoutStateData? = null

    init {
        App.get().inject(this)
        mDrawerStateSubscription = mDrawerLayoutState
                .observable
                .filter { it.state != DrawerLayoutStateData.STATE_CHANGED }
                .subscribe(shortSubscription {
                    mDrawerLayoutData = it
                    if (it.state == DrawerLayoutStateData.SLIDE) {
                        closeProgrammatically()
                    }
                })
    }

    override fun closeByUser() {
        App.getUserConfig().apply {
            peopleNearbyPopoverClose = Calendar.getInstance().timeInMillis
            saveConfig()
        }
        mPopupVindow.dismiss()
    }

    override fun closeProgrammatically() = mPopupVindow.dismiss()

    override fun show() {
        if (!mPopupVindow.isShowing && mDrawerLayoutData?.state != DrawerLayoutStateData.SLIDE &&
                mDrawerLayoutData?.state != DrawerLayoutStateData.OPENED) {
            if (Calendar.getInstance().timeInMillis >=
                    App.getUserConfig().peopleNearbyPopoverClose + POPOVER_SHOW_DELAY) {
                mPopupVindow.showAsDropDown(anchorView.invoke())
            }
        }
    }

    fun release() = mDrawerStateSubscription.safeUnsubscribe()
}

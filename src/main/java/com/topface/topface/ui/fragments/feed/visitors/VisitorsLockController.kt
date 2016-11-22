package com.topface.topface.ui.fragments.feed.visitors

import android.databinding.ViewStubProxy
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.LayoutEmptyVisitorsBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

/**
 * Created by tiberal on 09.09.16.
 */
class VisitorsLockController(stub: ViewStubProxy,private val mShower: ITrialShower) :
        BaseFeedLockerController<LayoutEmptyVisitorsBinding, VisitorsLockScreenViewModel>(stub) {

    override fun initLockedFeedStub(errorCode: Int) {
        mStubModel?.let {
            with(it) {
                mShower.showTrial()
                buttonText.set(App.getContext().getString(R.string.buying_vip_status))
                title.set(App.getContext().getString(R.string.with_vip_find_your_visitors))
                setOnButtonClickListener(View.OnClickListener {
                    mNavigator.showPurchaseVip()
                })
            }
        }
    }

    override fun initEmptyFeedStub() {
        mStubModel?.let {
            with(it) {
                buttonText.set(App.getContext().getString(R.string.general_get_dating))
                title.set(App.getContext().getString(R.string.go_dating_message))
                setOnButtonClickListener(View.OnClickListener {
                    mNavigator.showDating()
                })
            }
        }
    }
}
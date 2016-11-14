package com.topface.topface.ui.dialogs

import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.ui.fragments.buy.TransparentMarketFragment
import com.topface.topface.ui.views.ITransparentMarketFragmentRunner

/**
 *
 * Created by siberia87 on 14.11.16.
 */
abstract class BaseTrialVipPopup : AbstractDialogFragment(), View.OnClickListener, IOnFragmentActionsListener {

    override fun onSubscribeClick() {
        val f = activity.supportFragmentManager.findFragmentByTag(TransparentMarketFragment::class.java.simpleName)
        val fragment = f ?: TransparentMarketFragment.newInstance(App.get().options.trialVipExperiment.subscriptionSku, true, TrialVipPopup.TAG)
        fragment.retainInstance = true
        if (fragment is ITransparentMarketFragmentRunner) {
            fragment.setOnPurchaseCompleteAction(object : TransparentMarketFragment.onPurchaseActions {
                override fun onPurchaseSuccess() {
                    dismiss()
                }

                override fun onPopupClosed() {
                }
            })
            val transaction = this.activity.supportFragmentManager.beginTransaction()
            if (!fragment.isAdded) {
                transaction.add(R.id.fragment_content, fragment, TransparentMarketFragment::class.java.simpleName).commit()
            } else {
                transaction.remove(fragment)
                        .add(R.id.fragment_content, fragment, TransparentMarketFragment::class.java.simpleName).commit()
            }
        }
    }
}
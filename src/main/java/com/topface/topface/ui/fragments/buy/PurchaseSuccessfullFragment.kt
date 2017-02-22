package com.topface.topface.ui.fragments.buy

import android.os.Bundle
import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.BasePurchaseSuccessfullBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.IActivityDelegate
import kotlin.properties.Delegates

/**
 * Диалогфрагмент успешной покупки
 */
class PurchaseSuccessfullFragment : AbstractDialogFragment(), IDialogCloser {

    companion object{
        const val TAG = "purchase_successfull_ragment"
        const val GO_TO = "go_to"
        const val SKU = "product_type"
        fun getInstance(goTo: Int, sku: String) = PurchaseSuccessfullFragment().apply {
                        arguments = Bundle().apply {
                                putInt(GO_TO, goTo)
                                putString(SKU, sku)
                            }
                    }
    }


    private val mNavigator by lazy { FeedNavigator (activity as IActivityDelegate) }

    private var mBinding by Delegates.notNull<BasePurchaseSuccessfullBinding>()

    private val mViewModel by lazy {
                PurchaseSuccessfullViewModel(mNavigator, arguments.getInt(GO_TO), arguments.getString(SKU), this)
            }
    override fun initViews(root: View?) {
        mBinding = BasePurchaseSuccessfullBinding.bind(root)
        mBinding.setViewModel(mViewModel)
    }

    override fun isModalDialog() = false

    override fun getDialogLayoutRes() = R.layout.base_purchase_successfull

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.unsubscribe()
    }

    override fun closeIt() = dialog.cancel()

}
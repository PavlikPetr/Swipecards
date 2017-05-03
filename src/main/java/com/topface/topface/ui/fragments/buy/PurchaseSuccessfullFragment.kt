package com.topface.topface.ui.fragments.buy

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.BasePurchaseSuccessfullBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.registerLifeCycleDelegate
import kotlin.properties.Delegates

/**
 * Диалогфрагмент успешной покупки
 */
class PurchaseSuccessfullFragment : AbstractDialogFragment(), IDialogCloser {

    companion object {
        const val TAG = "purchase_successfull_ragment"
        const val TYPE = "product_type"
        const val FINISH_BUNDLE = "finish_bundle"
        fun getInstance(type: String, finishBundle: Bundle) = PurchaseSuccessfullFragment().apply {
            arguments = Bundle().apply {
                putString(TYPE, type)
                putBundle(FINISH_BUNDLE, finishBundle)
            }
        }
    }

    private var mBinding by Delegates.notNull<BasePurchaseSuccessfullBinding>()

    private val mViewModel by lazy {
        PurchaseSuccessfullViewModel(arguments.getString(TYPE), this)
    }

    override fun initViews(root: View?) {
        mBinding = BasePurchaseSuccessfullBinding.bind(root)
        mBinding.viewModel = mViewModel
    }

    override fun isModalDialog() = false

    override fun getDialogLayoutRes() = R.layout.base_purchase_successfull

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        activity?.let {
            (it as IActivityDelegate).apply {
                setResult(Activity.RESULT_OK, Intent().putExtras(arguments.getBundle(FINISH_BUNDLE)))
                finish()
            }
        }
        mViewModel.release()
    }

    override fun closeIt() = dialog.cancel()

}
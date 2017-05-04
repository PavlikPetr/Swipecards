package com.topface.billing.ninja.fragments.three_d_secure

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.billing.ninja.IFinishDelegate
import com.topface.billing.ninja.PurchaseError
import com.topface.topface.R
import com.topface.topface.databinding.LayoutNinja3dSecureFragmentBinding
import com.topface.topface.ui.fragments.BaseFragment
import org.jetbrains.anko.layoutInflater

/**
 * Фрагмент для проведения 3DSecure валидации транзакции PN платежей
 * Created by ppavlik on 25.04.17.
 */
class ThreeDSecureFragment : BaseFragment(), IFinishDelegate {
    companion object {
        const val TAG = "ThreeDSecureFragment.TAG"
        const val EXTRA_SETTINGS = "ThreeDSecureFragment.Extra.Settings"

        fun newInstance(settings: PurchaseError) = ThreeDSecureFragment().apply {
            arguments = Bundle().apply { putParcelable(EXTRA_SETTINGS, settings) }
        }

        fun newInstance(bundle: Bundle) = newInstance(bundle.getParcelable<PurchaseError>(EXTRA_SETTINGS))
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<LayoutNinja3dSecureFragmentBinding>(context.layoutInflater, R.layout.layout_ninja_3d_secure_fragment, null, false)
    }

    private val mViewModel by lazy {
        ThreeDSecureViewModel(arguments.getParcelable<PurchaseError>(EXTRA_SETTINGS), this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            mBinding.apply { viewModel = mViewModel }.root

    override fun finishWithResult(resultCode: Int, data: Intent) {
        with(activity) {
            setResult(resultCode, data)
            finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.release()
    }
}
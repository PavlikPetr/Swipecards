package com.topface.billing.ninja.fragments

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.topface.billing.ninja.ThreeDSecureParams
import com.topface.topface.R
import com.topface.topface.databinding.LayoutNinja3dSecureFragmentBinding
import com.topface.topface.ui.fragments.BaseFragment
import org.jetbrains.anko.layoutInflater

/**
 * Фрагмент для проведения 3DSecure валидации транзакции PN платежей
 * Created by ppavlik on 25.04.17.
 */
class ThreeDSecureFragment : BaseFragment() {
    companion object {
        const val TAG = "ThreeDSecureFragment.TAG"
        const val EXTRA_SETTINGS = "ThreeDSecureFragment.Extra.Settings"

        fun newInstance(settings: ThreeDSecureParams) = ThreeDSecureFragment().apply {
            arguments = Bundle().apply { putParcelable(EXTRA_SETTINGS, settings) }
        }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<LayoutNinja3dSecureFragmentBinding>(context.layoutInflater, R.layout.layout_ninja_3d_secure_fragment, null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) =
            mBinding.apply {
                with(arguments.getParcelable<ThreeDSecureParams>(EXTRA_SETTINGS)) {

                }
            }.root
}
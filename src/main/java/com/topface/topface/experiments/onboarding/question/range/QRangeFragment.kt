package com.topface.topface.experiments.onboarding.question.range

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.OnboardingQRangeBinding
import com.topface.topface.ui.fragments.BaseFragment
import org.jetbrains.anko.layoutInflater

class QRangeFragment: BaseFragment() {
    override fun needOptionsMenu() = false

    private val mViewModel by lazy {
        QRangeFragmentViewModel()
    }

    private val mLayoutResId = R.layout.onboarding_q_range

    private val mBinding by lazy {
        DataBindingUtil.inflate<OnboardingQRangeBinding>(context.layoutInflater, mLayoutResId, null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        mBinding.viewModel = QRangeFragmentViewModel()
        return mBinding.root
    }
}
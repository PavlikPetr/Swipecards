package com.topface.topface.ui.fragments.dating.dating_redesign

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.DatingReredesignBinding
import com.topface.topface.databinding.FragmentDatingLayoutBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.IActivityDelegate
import org.jetbrains.anko.layoutInflater

/**
 * Редизайн знакомств
 * Created by tiberal on 07.10.16.
 */
class DatingFragment : BaseFragment() {

    private val mBinding by lazy {
        DataBindingUtil.inflate<DatingReredesignBinding>(context.layoutInflater, R.layout.dating_reredesign, null, false)
    }

    private val mViewModel by lazy {
        DatingFragmentViewModel(mNavigator)
    }

    private val mNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?) = mBinding.apply { viewModel = mViewModel }.root
}
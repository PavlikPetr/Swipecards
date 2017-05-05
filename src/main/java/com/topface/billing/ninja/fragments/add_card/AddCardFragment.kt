package com.topface.billing.ninja.fragments.add_card

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.billing.ninja.IFinishDelegate
import com.topface.topface.R
import com.topface.topface.databinding.LayoutNinjaAddCardFragmentBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.IActivityDelegate
import org.jetbrains.anko.layoutInflater

/**
 * Фрагмент для добавления карты
 * Created by ppavlik on 25.04.17.
 */

class AddCardFragment : BaseFragment(), IFinishDelegate {

    companion object {
        const val TAG = "AddCardFragment.TAG"

        fun newInstance(bundle: Bundle) = AddCardFragment().apply {
            arguments = bundle
        }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<LayoutNinjaAddCardFragmentBinding>(context.layoutInflater, R.layout.layout_ninja_add_card_fragment, null, false)
    }

    private val mFeedNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private val mViewModel by lazy {
        AddCardViewModel(arguments, mFeedNavigator, this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            mBinding.apply {
                viewModel = mViewModel
            }.root

    override fun finishWithResult(resultCode: Int, data: Intent) {
        activity?.let {
            with(it) {
                setResult(resultCode, data)
                finish()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        mViewModel.release()
    }
}

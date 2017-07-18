package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.LikesCardsFragmentBinding
import com.topface.topface.ui.fragments.BaseFragment
import org.jetbrains.anko.layoutInflater


/**
 * Created by ppavlik on 17.07.17.
 * Фрагмент симпатий в виде карточек, по аналогии с tinder
 */
class LikesFragment : BaseFragment() {
    private val mBinding by lazy {
        DataBindingUtil.inflate<LikesCardsFragmentBinding>(context.layoutInflater,
                R.layout.likes_cards_fragment, null, false)
    }

    private val mAdapter by lazy {
        LikesAdapter()
    }

    private val mViewModel by lazy {
        LikesViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mBinding.apply {
            viewModel = mViewModel
            frame.adapter = mAdapter
        }.root
    }
}
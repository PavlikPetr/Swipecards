package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.LikesCardsFragmentBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.jetbrains.anko.layoutInflater
import rx.Subscription


/**
 * Created by ppavlik on 17.07.17.
 * Фрагмент симпатий в виде карточек, по аналогии с tinder
 */
class LikesFragment : BaseFragment() {

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var mUserСhoiceSubscription: Subscription? = null

    private val mBinding by lazy {
        DataBindingUtil.inflate<LikesCardsFragmentBinding>(context.layoutInflater,
                R.layout.likes_cards_fragment, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUserСhoiceSubscription = mEventBus
                .getObservable(LikesCardUserChoose::class.java)
                .subscribe(shortSubscription {
                    it?.let {
                        try {
                            if (it.isLike) {
                                mBinding.frame.topCardListener.selectRight()
                            } else {
                                mBinding.frame.topCardListener.selectLeft()
                            }
                        } catch (e: NullPointerException) {

                        }
                    }

                })
    }

    override fun onDestroy() {
        super.onDestroy()
        mUserСhoiceSubscription.safeUnsubscribe()
    }

    private val mAdapter by lazy {
        LikesAdapter()
    }

    private val mViewModel by lazy {
        LikesViewModel(App.getAppComponent().api())
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mBinding.apply {
            viewModel = mViewModel
            frame.adapter = mAdapter
            frame.setFlingListener(mViewModel)
            frame.setOnItemClickListener(mViewModel)
        }.root
    }
}
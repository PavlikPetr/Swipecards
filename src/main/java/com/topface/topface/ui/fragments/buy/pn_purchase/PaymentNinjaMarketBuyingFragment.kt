package com.topface.topface.ui.fragments.buy.pn_purchase

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.FragmentPnBuyBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.buy.pn_purchase.components.*
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.Utils
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate
import org.jetbrains.anko.layoutInflater

/**
 * Fragment to buy payment ninja products
 * Created by petrp on 02.03.2017.
 */
class PaymentNinjaMarketBuyingFragment : BaseFragment() {

    companion object {
        private const val FROM = "payment_ninja_market_buying_fragment_from"
        private const val TEXT = "payment_ninja_market_buying_fragment_text"
        private const val IS_PREMIUM_PRODUCTS = "payment_ninja_market_buying_fragment_is_premium_products"
        fun newInstance(isPremiumProducts: Boolean, text: String?, from: String?) =
                PaymentNinjaMarketBuyingFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean(IS_PREMIUM_PRODUCTS, isPremiumProducts)
                        putString(TEXT, text)
                        putString(FROM, from)
                    }
                }
    }

    private var mText: String? = null
    private var mFrom: String? = null
    private var mIsPremiumProducts = false

    private val mBinding by lazy {
        DataBindingUtil.inflate<FragmentPnBuyBinding>(context.layoutInflater,
                R.layout.fragment_pn_buy, null, false)
    }

    private val mViewModel by lazy {
        PaymentNinjaMarketBuyingFragmentViewModel(mFeedNavigator, mIsPremiumProducts, mFrom ?: Utils.EMPTY)
                .apply { activity.registerLifeCycleDelegate(this) }
    }

    private val mPnBuyingTypeProvider by lazy {
        PaymentNinjaBuyingTypeProvider()
    }

    private val mFeedNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(mPnBuyingTypeProvider) { Bundle() }
                .addAdapterComponent(BuyButtonComponent {
                    mViewModel.buyProduct(it)
                })
                .addAdapterComponent(BuyScreenTitleComponent(mText))
                .addAdapterComponent(BuyScreenCoinsSectionComponent())
                .addAdapterComponent(BuyScreenLikesSectionComponent())
                .addAdapterComponent(BuyScreenUnavailableComponent())
                .addAdapterComponent(TestPurchaseComponent())
                .addAdapterComponent(ThreeDSecureComponent())
    }

    private fun initList() = with(mBinding.buttonsRecyclerView) {
        layoutManager = LinearLayoutManager(context)
        adapter = mAdapter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(arguments) {
            mText = getString(TEXT)
            mFrom = getString(FROM)
            mIsPremiumProducts = getBoolean(IS_PREMIUM_PRODUCTS)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initList()
        return mBinding.apply {
            viewModel = mViewModel
        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity.unregisterLifeCycleDelegate(mViewModel)
        mViewModel.release()
    }
}
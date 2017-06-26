package com.topface.topface.ui.fragments.buy.design.v1

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.billing.OpenIabFragment
import com.topface.topface.R
import com.topface.topface.data.Products
import com.topface.topface.databinding.FragmentOpeniabBuyBinding
import com.topface.topface.ui.fragments.buy.design.v1.adapter_components.CoinItemComponent
import com.topface.topface.ui.fragments.buy.design.v1.adapter_components.InAppBillingUnsupportedComponent
import com.topface.topface.ui.fragments.buy.design.v1.adapter_components.LikeItemComponent
import com.topface.topface.ui.fragments.buy.design.v1.adapter_components.TestPurchaseSwitchItemComponent
import com.topface.topface.ui.fragments.buy.design.v1.view_models.CoinsBuyingViewModel
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.CacheProfile
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.extensions.getInt
import org.jetbrains.anko.layoutInflater
import org.onepf.oms.appstore.googleUtils.Purchase

/**
 * Created by ppavlik on 08.06.17.
 * Фрагмент покупки coins && likes в GP
 */
class CoinsBuyingFragment : OpenIabFragment() {

    companion object {
        const val FROM = "CoinsBuyingFragment.Extra.From"
        const val TEXT = "CoinsBuyingFragment.Extra.Text"
        fun newInstance(from: String?, text: String?) =
                CoinsBuyingFragment().apply {
                    arguments = Bundle().apply {
                        putString(TEXT, text)
                        putString(FROM, from)
                    }
                }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<FragmentOpeniabBuyBinding>(context.layoutInflater,
                R.layout.fragment_openiab_buy, null, false)
    }

    private val mViewModel by lazy {
        CoinsBuyingViewModel(arguments, products)
    }

    private val mFeedNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(TypeProvider()) { Bundle() }
                .addAdapterComponent(CoinItemComponent(mFeedNavigator))
                .addAdapterComponent(LikeItemComponent(mFeedNavigator))
                .addAdapterComponent(TestPurchaseSwitchItemComponent())
                .addAdapterComponent(InAppBillingUnsupportedComponent())
    }

    private fun initList() = with(mBinding.products) {
        layoutManager = StaggeredGridLayoutManager(R.integer.purchase_v1_coins_on_row.getInt(),
                StaggeredGridLayoutManager.VERTICAL)
        addItemDecoration(PurchaseItemDecoration())
        adapter = mAdapter
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initList()
        return mBinding.apply {
            viewModel = mViewModel
        }.root
    }

    override fun onSubscriptionSupported() {
        mViewModel.onSubscriptionSupported()
    }

    override fun onSubscriptionUnsupported() {
        mViewModel.onSubscriptionUnsupported()
    }

    override fun onInAppBillingSupported() {
        mViewModel.onInAppBillingSupported()
    }

    override fun onInAppBillingUnsupported() {
        mViewModel.onInAppBillingUnsupported()
    }

    override fun onPurchased(product: Purchase) {
        super.onPurchased(product)
        mViewModel.onPurchased(product)
    }

    override fun getProducts(): Products = CacheProfile.getMarketProducts()
}
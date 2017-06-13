package com.topface.topface.ui.bonus

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.FragmentBonusEnhancedBinding
import com.topface.topface.ui.bonus.components.LoaderComponent
import com.topface.topface.ui.bonus.components.OfferwallButtonComponent
import com.topface.topface.ui.bonus.models.BonusViewModel
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import org.jetbrains.anko.layoutInflater


/**
 * Created by ppavlik on 02.06.17.
 * Фрагмент "разводящего" экрана для оферов
 */
class BonusFragment : BaseFragment() {

    companion object {
        const val PAGE_NAME = "bonus"
        const val NEED_SHOW_TITLE = "need_show_title"
        const val FROM = "from"
        val TAG = BonusFragment::class.java.simpleName

        fun newInstance(isNeedTitle: Boolean, from: String) = BonusFragment().apply {
            arguments = Bundle().apply {
                putBoolean(NEED_SHOW_TITLE, isNeedTitle)
                putString(FROM, from)
            }
        }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<FragmentBonusEnhancedBinding>(context.layoutInflater,
                R.layout.fragment_bonus_enhanced, null, false)
    }

    private val mViewModel by lazy {
        BonusViewModel()
    }

    private val mIronSourceManager by lazy {
        App.getAppComponent().ironSourceManager()
    }

    private val mProvider by lazy {
        BonusProvider()
    }

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(mProvider) { Bundle() }
                .addAdapterComponent(LoaderComponent())
                .addAdapterComponent(OfferwallButtonComponent(arguments.getString(FROM)))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mIronSourceManager.initSdk(activity)
        initList()
        return mBinding.apply {
            viewModel = mViewModel
        }.root
    }

    override fun onResume() {
        super.onResume()
        if (arguments?.getBoolean(NEED_SHOW_TITLE) ?: false) {
            ToolbarManager.setToolbarSettings(ToolbarSettingsData(getString(R.string.general_bonus)))
        }
    }

    private fun initList() = with(mBinding.list) {
        layoutManager = LinearLayoutManager(context)
        adapter = mAdapter
    }
}
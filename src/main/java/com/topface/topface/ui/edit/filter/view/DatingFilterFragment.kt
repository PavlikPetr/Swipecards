package com.topface.topface.ui.edit.filter.view

import android.app.Activity
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.view.*
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.DatingFilter
import com.topface.topface.databinding.DatingFilterBinding
import com.topface.topface.ui.edit.AbstractEditFragment
import com.topface.topface.ui.edit.filter.model.FilterData
import com.topface.topface.ui.edit.filter.viewModel.DatingFilterViewModel
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate
import org.jetbrains.anko.layoutInflater

class DatingFilterFragment : AbstractEditFragment() {

    companion object {
        const val TAG = "DATING_filter_fragment_tag"
        const val INTENT_DATING_FILTER = "topface_dating_filter"
        const val CURRENT_FILTER_VALUE = "current_filter_value"
        const val PAGE_NAME = "Filter"
    }

    private val mFeedNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private var mFilter: FilterData? = null

    private val preFilter by lazy {
        FilterData(App.get().profile.dating)
    }
    private val mBinding by lazy {
        DataBindingUtil.inflate<DatingFilterBinding>(context.layoutInflater, R.layout.dating_filter, null, false)
    }

    private val mViewModel by lazy { DatingFilterViewModel(mFeedNavigator, mFilter ?: initFilter()) }

    override fun getScreenName(): String = PAGE_NAME

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        if (savedInstanceState != null && savedInstanceState.containsKey(CURRENT_FILTER_VALUE)) {
            mFilter = savedInstanceState.getParcelable<FilterData>(CURRENT_FILTER_VALUE)
        }
        (activity as IActivityDelegate).registerLifeCycleDelegate(mViewModel)
        mBinding.viewModel = mViewModel
        return mBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.actions_dating_filter, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        activity.finish()
        return true
    }

    private fun initFilter() = FilterData(App.get().profile.dating?.clone() ?: DatingFilter()).apply { mFilter = this }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(CURRENT_FILTER_VALUE, FilterData(mViewModel))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as IActivityDelegate).unregisterLifeCycleDelegate(mViewModel)
    }

    override fun hasChanges() = mFilter != null && FilterData(mViewModel) != preFilter

    override fun saveChanges(handler: Handler) {
        if (hasChanges()) {
            val intent = Intent()
            val data = FilterData(mViewModel)
            intent.putExtra(INTENT_DATING_FILTER, data)
            DatingFilter.setOnlyOnlineField(data.isOnlineOnly)
            activity.setResult(Activity.RESULT_OK, intent)
        } else {
            activity.setResult(Activity.RESULT_CANCELED)
        }
        handler.sendEmptyMessage(0)
    }

    override fun lockUi() = mViewModel.isEnabled.set(false)

    override fun unlockUi() = mViewModel.isEnabled.set(true)

    override fun onResume() {
        super.onResume()
        ToolbarManager.setToolbarSettings(ToolbarSettingsData(getString(R.string.filter_screen_title)))
    }
}

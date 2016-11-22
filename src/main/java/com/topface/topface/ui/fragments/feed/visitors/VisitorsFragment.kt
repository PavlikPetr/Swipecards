package com.topface.topface.ui.fragments.feed.visitors

import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Visitor
import com.topface.topface.databinding.LayoutEmptyVisitorsBinding
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.dialogs.trial_vip_experiment.IOnFragmentFinishDelegate
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentBoilerplateFragment
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

@FlurryOpenEvent(name = VisitorsFragment.SCREEN_TYPE)
class VisitorsFragment : BaseFeedFragment<Visitor, LayoutEmptyVisitorsBinding>(), ITrialShower, IOnFragmentFinishDelegate {

    companion object {
        const val SCREEN_TYPE = "Visitors"
    }

    override val mViewModel by lazy {
        VisitorsFragmentViewModel(mBinding, mNavigator, mApi)
    }
    override val mLockerControllerBase by lazy {
        VisitorsLockController(mBinding.emptyFeedStub as ViewStubProxy, this)
    }
    override val mAdapter by lazy {
        VisitorsAdapter(mNavigator)
    }

    override fun createLockerFactory() = object : BaseFeedLockerController.ILockScreenVMFactory<LayoutEmptyVisitorsBinding> {
        override fun construct(binding: ViewDataBinding) = VisitorsLockScreenViewModel(binding as LayoutEmptyVisitorsBinding, mNavigator, this@VisitorsFragment)
    }

    override fun getEmptyFeedLayout() = R.layout.layout_empty_visitors

    override fun showTrial() {
        if (App.getUserConfig().canShowInVisitors() && isAdded) {
            val popup = ExperimentBoilerplateFragment
                    .newInstance(ExperimentsType.EXPERIMENT_1, true)
            popup.onFragmentFinishDelegate = this
            popup.show(activity.supportFragmentManager, ExperimentBoilerplateFragment.TAG)
        }
    }

    override fun closeFragmentByForm() = onFeedUnlocked()

}
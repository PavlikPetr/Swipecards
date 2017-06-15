package com.topface.topface.ui.fragments.feed.enhanced.visitors

import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.api.responses.Visitor
import com.topface.topface.banners.PageInfo
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.feed.base.BaseFeedModule
import com.topface.topface.di.feed.visitors.DaggerVisitorsModelsComponent
import com.topface.topface.di.feed.visitors.VisitorsComponent
import com.topface.topface.di.feed.visitors.VisitorsModelsComponent
import com.topface.topface.di.feed.visitors.VisitorsModule
import com.topface.topface.di.navigation_activity.NavigationActivityComponent
import com.topface.topface.di.navigation_activity.NavigationActivityModule
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.dialogs.trial_vip_experiment.IOnFragmentFinishDelegate
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragment
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter

@FlurryOpenEvent(name = VisitorsFragment.SCREEN_TYPE)
class VisitorsFragment : BaseFeedFragment<Visitor>(), IOnFragmentFinishDelegate {

    companion object {
        const val SCREEN_TYPE = "Visitors"
    }

    override val mViewModel by lazy {
        ComponentManager.obtainComponent(VisitorsModelsComponent::class.java) {
            DaggerVisitorsModelsComponent.builder().appComponent(App.getAppComponent()).build()
        }.visitorsViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ComponentManager.releaseComponent(VisitorsComponent::class.java)
        ComponentManager.obtainComponent(VisitorsComponent::class.java) {
            ComponentManager.obtainComponent(NavigationActivityComponent::class.java) {
                App.getAppComponent().add(NavigationActivityModule(activity as NavigationActivity));
            }
                    .add(VisitorsModule(this@VisitorsFragment), BaseFeedModule(this@VisitorsFragment))
        }.inject(this@VisitorsFragment)
        super.onCreate(savedInstanceState)
    }

    override fun attachAdapterComponents(compositeAdapter: CompositeAdapter) {
        compositeAdapter.addAdapterComponent(
                VisitorAdapterComponent({ itemClick(it) }, { itemLongClick(it) }))
    }

    override fun terminateImmortalComponent() {
        super.terminateImmortalComponent()
        ComponentManager.releaseComponent(VisitorsModelsComponent::class.java)
    }

    override fun onDestroyView() {
        ComponentManager.releaseComponent(VisitorsComponent::class.java)
        super.onDestroyView()
    }

    override fun closeFragmentByForm() = onFeedUnlocked()

    override fun getPageName() = PageInfo.PageName.UNKNOWN_PAGE
}
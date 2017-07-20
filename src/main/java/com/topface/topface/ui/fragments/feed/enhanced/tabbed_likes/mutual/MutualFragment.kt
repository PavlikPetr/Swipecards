package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.mutual

import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.feed.base.BaseFeedModule
import com.topface.topface.di.feed.mutual.DaggerMutualViewModelsComponent
import com.topface.topface.di.feed.mutual.MutualComponent
import com.topface.topface.di.feed.mutual.MutualModule
import com.topface.topface.di.feed.mutual.MutualViewModelsComponent
import com.topface.topface.di.navigation_activity.NavigationActivityComponent
import com.topface.topface.di.navigation_activity.NavigationActivityModule
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.BaseLikesFeedFragment
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter

@FlurryOpenEvent(name = MutualFragment.SCREEN_TYPE)
class MutualFragment : BaseLikesFeedFragment() {
    companion object {
        const val SCREEN_TYPE = "NewMutual"
    }

    override val mViewModel by lazy {
        ComponentManager.obtainComponent(MutualViewModelsComponent::class.java) {
            DaggerMutualViewModelsComponent.builder().appComponent(App.getAppComponent()).build()
        }.mutualViewModel()
    }

    override fun attachAdapterComponents(compositeAdapter: CompositeAdapter) {
        compositeAdapter.addAdapterComponent(
                MutualAdapterComponent({ itemClick(it, MutualFragment.SCREEN_TYPE) }, { itemLongClick(it) }))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ComponentManager.releaseComponent(MutualComponent::class.java)
        ComponentManager.obtainComponent(MutualComponent::class.java) {
            ComponentManager.obtainComponent(NavigationActivityComponent::class.java) {
                App.getAppComponent().add(NavigationActivityModule(activity as NavigationActivity))
            }
                    .add(MutualModule(this@MutualFragment), BaseFeedModule(this@MutualFragment))
        }.inject(this@MutualFragment)
        super.onCreate(savedInstanceState)
    }

    override fun terminateImmortalComponent() {
        ComponentManager.releaseComponent(MutualViewModelsComponent::class.java)
    }

    override fun onDestroyView() {
        ComponentManager.releaseComponent(MutualComponent::class.java)
        super.onDestroyView()
    }
}
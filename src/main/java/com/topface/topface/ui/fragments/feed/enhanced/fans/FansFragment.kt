package com.topface.topface.ui.fragments.feed.enhanced.fans

import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.data.FeedBookmark
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.feed.base.BaseFeedModule
import com.topface.topface.di.feed.fans.DaggerFansViewModelsComponent
import com.topface.topface.di.feed.fans.FansComponent
import com.topface.topface.di.feed.fans.FansModule
import com.topface.topface.di.feed.fans.FansViewModelsComponent
import com.topface.topface.di.navigation_activity.NavigationActivityComponent
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragment
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter

@FlurryOpenEvent(name = FansFragment.SCREEN_TYPE)
class FansFragment : BaseFeedFragment<FeedBookmark>() {

    companion object {
        const val SCREEN_TYPE = "Fans"
    }

    override val mViewModel by lazy {
        ComponentManager.obtainComponent(FansViewModelsComponent::class.java) {
            DaggerFansViewModelsComponent.builder().appComponent(App.getAppComponent()).build()
        }.fansViewModel()
    }

    override fun attachAdapterComponents(compositeAdapter: CompositeAdapter) {
        compositeAdapter.addAdapterComponent(
                FansAdapterComponent({ itemClick(it) }, { itemLongClick(it) }))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ComponentManager.obtainComponent(FansComponent::class.java) {
            ComponentManager.obtainComponent(NavigationActivityComponent::class.java)
                    .add(FansModule(this@FansFragment), BaseFeedModule(this@FansFragment)).apply {
                inject(this@FansFragment)
            }
        }
    }

    override fun terminateImmortalComponent() {
        ComponentManager.releaseComponent(FansViewModelsComponent::class.java)
    }

    override fun onDestroyView() {
        ComponentManager.releaseComponent(FansComponent::class.java)
        super.onDestroyView()
    }

}
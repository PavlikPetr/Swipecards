package com.topface.topface.ui.fragments.feed.enhanced.fans

import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.feed.base.BaseFeedModule
import com.topface.topface.di.feed.base.DefaultFeedModule
import com.topface.topface.di.feed.fans.DaggerFansViewModelsComponent
import com.topface.topface.di.feed.fans.FansComponent
import com.topface.topface.di.feed.fans.FansModule
import com.topface.topface.di.feed.fans.FansViewModelsComponent
import com.topface.topface.di.navigation_activity.NavigationActivityComponent
import com.topface.topface.di.navigation_activity.NavigationActivityModule
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.fragments.feed.enhanced.base.BaseFeedFragmentWithComponentAdapter
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter

@FlurryOpenEvent(name = FansFragment.SCREEN_TYPE)
class FansFragment : BaseFeedFragmentWithComponentAdapter<FeedBookmark>() {

    companion object {
        const val SCREEN_TYPE = "Fans"
    }

    override val actionModeMenu: Int
        get() = R.menu.feed_context_menu_fans

    override val mViewModel by lazy {
        ComponentManager.obtainComponent(FansViewModelsComponent::class.java) {
            DaggerFansViewModelsComponent.builder().appComponent(App.getAppComponent()).build()
        }.fansViewModel()
    }

    override fun attachAdapterComponents(adapter: CompositeAdapter) {
        adapter.addAdapterComponent(
                FansAdapterComponent({ itemClick(it, SCREEN_TYPE) }, { itemLongClick(it) }))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ComponentManager.releaseComponent(FansComponent::class.java)
        ComponentManager.obtainComponent(FansComponent::class.java) {
            ComponentManager.obtainComponent(NavigationActivityComponent::class.java){
                App.getAppComponent().add(NavigationActivityModule(activity as NavigationActivity))
            }
                    .add(FansModule(this@FansFragment), DefaultFeedModule(this@FansFragment))
        }.inject(this@FansFragment)
        super.onCreate(savedInstanceState)
    }

    override fun terminateImmortalComponent() {
        ComponentManager.releaseComponent(FansViewModelsComponent::class.java)
    }

    override fun onDestroyView() {
        ComponentManager.releaseComponent(FansComponent::class.java)
        super.onDestroyView()
    }
}
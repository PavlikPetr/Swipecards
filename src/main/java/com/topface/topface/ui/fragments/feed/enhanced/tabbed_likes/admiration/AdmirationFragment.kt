package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.admiration

import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.di.ComponentManager
import com.topface.topface.di.feed.admiration.AdmirationComponent
import com.topface.topface.di.feed.admiration.AdmirationModule
import com.topface.topface.di.feed.admiration.AdmirationViewModelsComponent
import com.topface.topface.di.feed.admiration.DaggerAdmirationViewModelsComponent
import com.topface.topface.di.feed.base.BaseFeedModule
import com.topface.topface.di.navigation_activity.NavigationActivityComponent
import com.topface.topface.di.navigation_activity.NavigationActivityModule
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.BaseLikesFeedFragment
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter

@FlurryOpenEvent(name = AdmirationFragment.Companion.SCREEN_TYPE)
class AdmirationFragment : BaseLikesFeedFragment() {
    companion object {
        const val SCREEN_TYPE = "NewAdmiration"
    }

    override val mViewModel by lazy {
        ComponentManager.obtainComponent(AdmirationViewModelsComponent::class.java) {
            DaggerAdmirationViewModelsComponent.builder().appComponent(App.getAppComponent()).build()
        }.admirationViewModel()
    }

    override fun attachAdapterComponents(adapter: CompositeAdapter) {
        adapter.addAdapterComponent(
                AdmirationAdapterComponent({ itemClick(it, AdmirationFragment.SCREEN_TYPE) }, { itemLongClick(it) }))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ComponentManager.releaseComponent(AdmirationComponent::class.java)
        ComponentManager.obtainComponent(AdmirationComponent::class.java) {
            ComponentManager.obtainComponent(NavigationActivityComponent::class.java) {
                App.getAppComponent().add(NavigationActivityModule(activity as NavigationActivity))
            }
                    .add(AdmirationModule(this@AdmirationFragment), BaseFeedModule(this@AdmirationFragment))
        }.inject(this@AdmirationFragment)
        super.onCreate(savedInstanceState)
    }

    override fun terminateImmortalComponent() {
        ComponentManager.releaseComponent(AdmirationViewModelsComponent::class.java)
    }

    override fun onDestroyView() {
        ComponentManager.releaseComponent(AdmirationComponent::class.java)
        super.onDestroyView()
    }
}
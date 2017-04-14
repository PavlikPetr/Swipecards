package com.topface.topface.di

import com.topface.topface.App
import com.topface.topface.chat.SuspiciousUserCache
import com.topface.topface.data.leftMenu.NavigationState
import com.topface.topface.state.*
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.external_libs.AdjustManager
import com.topface.topface.ui.external_libs.kochava.KochavaManager
import com.topface.topface.utils.NavigationManager
import com.topface.topface.utils.RunningStateManager
import com.topface.topface.utils.config.WeakStorage
import dagger.Component
import javax.inject.Singleton

/**
 * Created by tiberal on 02.02.17.
 */
@Component(modules = arrayOf(AppModule::class, GarbageModule::class))
@Singleton
interface AppComponent {

    fun appState(): TopfaceAppState
    fun eventBus(): EventBus
    fun navigationState(): NavigationState
    fun drawerLayoutState(): DrawerLayoutState
    fun weakStorage(): WeakStorage
    fun lifeCycleState(): LifeCycleState
    fun authState(): AuthState
    fun runningStateManager(): RunningStateManager

    fun kochavaManager(): KochavaManager
    fun adjustManager(): AdjustManager
    fun suspiciousUserCache(): SuspiciousUserCache

    fun inject(app: App)
    fun inject(manager: NavigationManager)
    fun inject(navigationActivity: NavigationActivity)
}
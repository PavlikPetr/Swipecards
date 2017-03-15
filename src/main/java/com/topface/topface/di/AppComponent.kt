package com.topface.topface.di

import android.content.Context
import com.topface.scruffy.ScruffyManager
import com.topface.topface.App
import com.topface.topface.api.Api
import com.topface.topface.data.leftMenu.NavigationState
import com.topface.topface.di.api.ApiModule
import com.topface.topface.di.navigation_activity.NavigationActivityComponent
import com.topface.topface.di.navigation_activity.NavigationActivityModule
import com.topface.topface.state.*
import com.topface.topface.ui.external_libs.AdjustManager
import com.topface.topface.utils.NavigationManager
import com.topface.topface.utils.config.WeakStorage
import dagger.Component
import javax.inject.Singleton

/**
 * Рутовый компонент, пердоставляет базовые зависимости
 * Created by tiberal on 02.02.17.
 */
@Component(modules = arrayOf(AppModule::class, GarbageModule::class, ApiModule::class))
@Singleton
interface AppComponent {

    fun scruffyManager(): ScruffyManager
    fun api(): Api

    fun context(): Context
    fun appState(): TopfaceAppState
    fun eventBus(): EventBus
    fun navigationState(): NavigationState
    fun drawerLayoutState(): DrawerLayoutState
    fun weakStorage(): WeakStorage
    fun lifeCycleState(): LifeCycleState
    fun authState(): AuthState

    fun adjustManager(): AdjustManager

    fun inject(app: App)
    fun inject(manager: NavigationManager)

    fun add(module: NavigationActivityModule): NavigationActivityComponent
}
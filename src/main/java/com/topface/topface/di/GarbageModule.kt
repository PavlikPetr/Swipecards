package com.topface.topface.di

import com.topface.topface.ui.external_libs.AdjustManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Отстойник для всякого шлака
 * Created by tiberal on 06.02.17.
 */
@Module()
class GarbageModule {

    @Provides
    @Singleton
    fun providesAdjustManager() = AdjustManager()
}
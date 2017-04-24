package com.topface.topface.di.chat

import android.os.Bundle
import com.topface.topface.di.scope.FragmentScope
import com.topface.topface.ui.fragments.feed.enhanced.chat.ChatLoader
import com.topface.topface.ui.fragments.feed.enhanced.chat.adapter_components.LoaderStubComponent
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider
import dagger.Module
import dagger.Provides

//todo если нужно делегировать вызовы активити, то в конструктор передавать IStateSaverRegistrator
@Module
class ChatModule {

    @Provides
    @FragmentScope
    fun provideTypeProvider() = object : ITypeProvider {
        override fun getType(java: Class<*>) = when (java) {
            ChatLoader::class.java -> 1
            else -> 0
        }
    }

    @Provides
    @FragmentScope
    fun provideLoaderStubComponent() = LoaderStubComponent()

    @Provides
    @FragmentScope
    fun provideCompositeAdapter(typeProvider: ITypeProvider, loaderStubComponent: LoaderStubComponent): CompositeAdapter {
        val adapter = CompositeAdapter(typeProvider) {
            Bundle().apply {
                //todo итем для подгрузки
            }
        }
        adapter.addAdapterComponent(loaderStubComponent)
        return adapter
    }
}

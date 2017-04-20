package com.topface.topface.di.chat

import android.os.Bundle
import com.topface.topface.di.scope.FragmentScope
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
        override fun getType(java: Class<*>) = 666
    }

    @Provides
    @FragmentScope
    fun provideCompositeAdapter(typeProvider: ITypeProvider): CompositeAdapter {
        val adapter = CompositeAdapter(typeProvider) {
            Bundle().apply {
                //todo итем для подгрузки
            }
        }
        //adapter.addAdapterComponent()
        return adapter
    }
/*
    //todo для тестов
    @Provides
    @FragmentScope
    fun provideBuyButtonComponent() = BuyButtonComponent {}*/


}

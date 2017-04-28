package com.topface.topface.ui.new_adapter.enhanced

interface IProvideItemTypeStrategy {
    val typeProvider: ITypeProvider
    fun provide(item: Any): Int
}

class DefaultProvideItemTypeStrategy(override val typeProvider: ITypeProvider): IProvideItemTypeStrategy {
    override fun provide(item: Any) = typeProvider.getType(item.javaClass)
}

class DummyStrategy(override val typeProvider: ITypeProvider): IProvideItemTypeStrategy {
    override fun provide(item: Any) = 0
}

class ProvideItemTypeStrategyFactory(val typeProvider: ITypeProvider) {
    companion object {
        const val DEFAULT = 1
    }
    fun construct(type: Int) = when(type) {
        DEFAULT -> DefaultProvideItemTypeStrategy(typeProvider)
        else -> DummyStrategy(typeProvider)
    }
}
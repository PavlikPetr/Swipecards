package com.topface.topface.ui.new_adapter.enhanced

abstract class ProvideItemTypeStrategy(val typeProvider: ITypeProvider) {
    abstract fun provide(item: Any): Int
}

class DefaultProvideItemTypeStrategy(typeProvider: ITypeProvider): ProvideItemTypeStrategy(typeProvider) {
    override fun provide(item: Any) = typeProvider.getType(item.javaClass)
}
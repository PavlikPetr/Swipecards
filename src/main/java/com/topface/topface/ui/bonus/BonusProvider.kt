package com.topface.topface.ui.bonus

/**
 * Created by ppavlik on 02.06.17.
 * Провайдер типов
 */
class BonusProvider : com.topface.topface.ui.new_adapter.enhanced.ITypeProvider {
    override fun getType(java: Class<*>) = when (java) {
        Loader::class.java -> 1
        OfferwallButton::class.java -> 2
        else -> 0
    }
}
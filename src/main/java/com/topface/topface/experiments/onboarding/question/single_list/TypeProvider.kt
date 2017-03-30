package com.topface.topface.experiments.onboarding.question.single_list

import com.topface.topface.experiments.onboarding.question.Button
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

internal class TypeProvider: ITypeProvider {
    override fun getType(java: Class<*>): Int {
        when(java) {
            Button::class.java -> return 1
            else -> return 0
        }
    }
}
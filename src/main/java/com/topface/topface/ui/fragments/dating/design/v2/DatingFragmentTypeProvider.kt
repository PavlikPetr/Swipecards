package com.topface.topface.ui.fragments.dating.design.v2

import com.topface.topface.ui.fragments.dating.form.FormModel
import com.topface.topface.ui.fragments.dating.form.GiftsModel
import com.topface.topface.ui.fragments.dating.form.ParentModel
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * Провайдер для итемов анкеты в знакомствах
 * Created by ppavlik on 18.04.17.
 */
class DatingFragmentTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>) = when (java) {
        FormModel::class.java -> 1
        ParentModel::class.java -> 2
        GiftsModel::class.java -> 3
        else -> 0
    }
}

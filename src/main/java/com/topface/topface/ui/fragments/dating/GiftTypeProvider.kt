package com.topface.topface.ui.fragments.dating

import com.topface.topface.data.Gift
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

class GiftTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>) = when (java) {
        Gift::class.java -> 1
        FakeGift::class.java -> 2
        else -> 0
    }
}

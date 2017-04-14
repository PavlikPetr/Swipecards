package com.topface.topface.ui.fragments.dating

import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.databinding.NoGiftsLayoutBinding
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class GiftEmptyComponent: AdapterComponent<NoGiftsLayoutBinding, FakeGift>() {
    override val itemLayout: Int
        get() = R.layout.no_gifts_layout
    override val bindingClass: Class<NoGiftsLayoutBinding>
        get() = NoGiftsLayoutBinding::class.java

    init {
        Debug.error("------------Gift!!!!!!!!!!!!!!EMPTY!!!!!!!!!!1Component конструктор---------------")
    }
    override fun bind(binding:NoGiftsLayoutBinding, data: FakeGift?, position: Int) {
    }
}
package com.topface.topface.ui.fragments.dating

import com.topface.topface.R
import com.topface.topface.databinding.ChildFormItemBinding
import com.topface.topface.ui.fragments.dating.form.ChildItemViewModel
import com.topface.topface.ui.fragments.dating.form.FormModel
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

class ChildItemComponent(private val mApi: FeedApi) : AdapterComponent<ChildFormItemBinding, FormModel>() {

    override val itemLayout: Int
        get() = R.layout.child_form_item
    override val bindingClass: Class<ChildFormItemBinding>
        get() = ChildFormItemBinding::class.java

    override fun bind(binding: ChildFormItemBinding, data: FormModel?, position: Int) {
        data?.data?.let { binding.model = ChildItemViewModel(mApi, data) }
    }
}
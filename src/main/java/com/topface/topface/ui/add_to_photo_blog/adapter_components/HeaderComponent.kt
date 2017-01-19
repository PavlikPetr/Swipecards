package com.topface.topface.ui.add_to_photo_blog.adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.ItemAddToPhotoBlogHeaderBinding
import com.topface.topface.ui.add_to_photo_blog.HeaderItem
import com.topface.topface.ui.add_to_photo_blog.HeaderItemViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Component with simple text and small avatar
 * Created by mbayutin on 10.01.17.
 */
class HeaderComponent : AdapterComponent<ItemAddToPhotoBlogHeaderBinding, HeaderItem>() {

    override val itemLayout: Int
        get() = R.layout.item_add_to_photo_blog_header
    override val bindingClass: Class<ItemAddToPhotoBlogHeaderBinding>
        get() = ItemAddToPhotoBlogHeaderBinding::class.java

    override fun bind(binding: ItemAddToPhotoBlogHeaderBinding, data: HeaderItem?, position: Int) {
        data?.let {
            binding.viewModel = HeaderItemViewModel()
        }
    }
}
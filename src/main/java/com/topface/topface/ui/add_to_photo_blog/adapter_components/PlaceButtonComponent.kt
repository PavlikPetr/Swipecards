package com.topface.topface.ui.add_to_photo_blog.adapter_components

import com.topface.topface.R
import com.topface.topface.databinding.ItemAddToPhotoBlogButtonBinding
import com.topface.topface.ui.add_to_photo_blog.PlaceButtonItem
import com.topface.topface.ui.add_to_photo_blog.PlaceButtonItemViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Component for button "разместить" with price in coins
 * Created by mbayutin on 10.01.17.
 */
class PlaceButtonComponent : AdapterComponent<ItemAddToPhotoBlogButtonBinding, PlaceButtonItem>() {

    override val itemLayout: Int
        get() = R.layout.item_add_to_photo_blog_button
    override val bindingClass: Class<ItemAddToPhotoBlogButtonBinding>
        get() = ItemAddToPhotoBlogButtonBinding::class.java

    override fun bind(binding: ItemAddToPhotoBlogButtonBinding, data: PlaceButtonItem?, position: Int) {
        data?.let {
            binding.viewModel = PlaceButtonItemViewModel(data.price)
        }
    }
}
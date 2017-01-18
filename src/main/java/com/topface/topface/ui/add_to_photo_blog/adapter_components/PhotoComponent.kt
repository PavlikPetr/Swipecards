package com.topface.topface.ui.add_to_photo_blog.adapter_components

import com.topface.topface.R
import com.topface.topface.data.Photo
import com.topface.topface.databinding.ItemAddToPhotoBlogPhotoBinding
import com.topface.topface.ui.add_to_photo_blog.PhotoItemViewModel
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Component for one of user photo in list
 * Created by mbayutin on 13.01.17.
 */
class PhotoComponent : AdapterComponent<ItemAddToPhotoBlogPhotoBinding, Photo>() {
    override val itemLayout: Int
        get() = R.layout.item_add_to_photo_blog_photo
    override val bindingClass: Class<ItemAddToPhotoBlogPhotoBinding>
        get() = ItemAddToPhotoBlogPhotoBinding::class.java

    override fun bind(binding: ItemAddToPhotoBlogPhotoBinding, data: Photo?, position: Int) {
        data?.let {
            binding.viewModel = PhotoItemViewModel(data)
        }
    }
}
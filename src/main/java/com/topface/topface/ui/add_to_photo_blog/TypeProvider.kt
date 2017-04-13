package com.topface.topface.ui.add_to_photo_blog

import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * TypeProvider for experimental add-to-photo-blog screen
 * Created by mbayutin on 10.01.17.
 */
internal class TypeProvider : ITypeProvider {
    override fun getType(java: Class<*>): Int {
        when(java) {
            HeaderItem::class.java -> return 1
            PhotoListItem::class.java -> return 2
            PlaceButtonItem::class.java -> return 3
            else -> return 0
        }
    }
}
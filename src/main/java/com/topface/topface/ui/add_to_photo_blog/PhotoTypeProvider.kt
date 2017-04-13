package com.topface.topface.ui.add_to_photo_blog

import com.topface.topface.data.Photo
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * Type provider for photo list
 * Created by m.bayutin on 13.01.17.
 */
class PhotoTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>): Int {
        when(java) {
            Photo::class.java -> return 1
            else -> return 0
        }
    }
}
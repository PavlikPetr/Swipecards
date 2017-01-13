package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * Type provider for photoblog list items
 * Created by ppavlik on 11.01.17.
 */
class PgotoblogListTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>) = when (java) {
        PhotoBlogAdd::class.java -> 1
        FeedPhotoBlog::class.java -> 2
        else -> 0
    }
}
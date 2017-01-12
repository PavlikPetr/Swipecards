package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * Type provider for photoblog list items
 * Created by ppavlik on 11.01.17.
 */
class PgotoblogListTypeProvider : ITypeProvider {
    override fun getType(java: Class<*>): Int {
        if (java == PhotoBlogAdd::class.java) {
            return 1
        }
        if (java == FeedPhotoBlog::class.java) {
            return 2
        }
        return 0
    }
}
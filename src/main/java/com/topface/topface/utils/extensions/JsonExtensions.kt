package com.topface.topface.utils.extensions

import com.google.gson.JsonArray
import com.topface.framework.JsonUtils
import com.topface.topface.data.FeedUser
import com.topface.topface.data.search.UsersList

/**
 * Различные расширения для серилизации и десерилизации
 * Created by petrp on 11.04.2017.
 */

/**
 * Get JsonArray from the UsersList
 */
fun UsersList<out FeedUser>.jsonArray() =
        JsonArray().apply {
            this@jsonArray.forEach {
                add(JsonUtils.toJson(it))
            }
        }
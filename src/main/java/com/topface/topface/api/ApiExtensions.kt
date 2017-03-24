package com.topface.topface.api

import com.google.gson.JsonArray
import com.topface.topface.data.FeedItem
import java.util.*

/**
 * Примочки для апи
 * Created by tiberal on 06.03.17.
 */

fun List<FeedItem>.getFeedIntIds() = ArrayList<Int>().apply {
    this@getFeedIntIds.filter {
        !it.isLoaderOrRetrier
    }.forEach {
        this.add(it.user.id)
    }
}

fun List<String>.stringListToJsonArray() = JsonArray().apply {
    this@stringListToJsonArray.forEach {
        this@apply.add(it)
    }
}

fun List<Int>.intListToJsonArray() = JsonArray().apply {
    this@intListToJsonArray.forEach {
        this@apply.add(it)
    }
}
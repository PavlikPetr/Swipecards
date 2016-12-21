package com.topface.topface.ui.fragments.feed.feed_utils

import com.topface.topface.data.FeedItem
import com.topface.topface.utils.extensions.toLongSafe
import org.jetbrains.anko.collections.forEachReversedByIndex
import java.util.*

/** Ништяки для фидов
 * Created by tiberal on 10.08.16.
 */

fun <T : FeedItem> List<T>.hasItem(position: Int) = count() > position && position >= 0

fun <T : FeedItem> List<T>.getLast() = if (!isEmpty()) get(count() - 1) else null

fun <T : FeedItem> List<T>.getFirst() = if (!isEmpty()) get(0) else null

fun <T : FeedItem> List<T>.getFirstItem(): T? {
    var item: T? = null
    if (!isEmpty()) {
        item = getFirst()
        if (count() >= 2) {
            item = get(1)
        }
    }
    return item
}

fun <T : FeedItem> List<T>.getRealDataFirstItem(): T? {
    if (!isEmpty()) {
        forEach {
            if (!it.isEmpty()) {
                return it
            }
        }
    }
    return null
}

fun List<Any>.findLastFeedItem(): FeedItem? {
    if (!isEmpty()) {
        forEachReversedByIndex {
            if (it is FeedItem && !it.isLoaderOrRetrier && it.id.toLongSafe() > 0) {
                return it
            }
        }
    }
    return null
}

fun <T : FeedItem> T.isEmpty() = id == null

fun <T : FeedItem> List<T>.getLastItem(): T? {
    var item: T? = null
    if (!isEmpty()) {
        val dataSize = count()
        val last = getLast() ?: return null
        val feedIndex = if (last.isLoaderOrRetrier)
            dataSize - 2
        else
            dataSize - 1
        if (hasItem(feedIndex)) {
            item = get(feedIndex)
        }
    }
    return item
}

fun <T : FeedItem> MutableList<T>.addFirst(item: T) = add(0, item)

fun <T : FeedItem> MutableList<T>.addAllFirst(item: Collection<T>) = addAll(0, item)

fun <T : FeedItem> MutableList<T>.removeFirst() {
    if (!isEmpty()) {
        removeAt(0)
    }
}

fun <T : FeedItem> MutableList<T>.removeLast() {
    if (!isEmpty()) {
        removeAt(count() - 1)
    }
}

fun <T : FeedItem> T?.getUserId() = if (this != null && user != null) user.id else 0

fun <T : FeedItem> MutableList<T>.getFeedIdList(): ArrayList<String> {
    val list = ArrayList<String>()
    this.filter { !it.isLoaderOrRetrier }
            .forEach { list.add(it.id) }
    return list
}

fun <T : FeedItem> MutableList<T>.getUserIdList(): List<Int> {
    var list: List<Int> = listOf()
    this.filter { !it.isLoaderOrRetrier }
            .forEach {
                it.user?.let {
                    list = list.plus(it.id)
                }
            }
    return list
}

fun List<Int>.convertFeedIdList(): ArrayList<String> {
    val list = ArrayList<String>()
    this.forEach {
        list.add(it.toString())
    }
    return list
}



package com.topface.topface.ui.fragments.feed.enhanced.utils

import android.databinding.BindingAdapter
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.topface.framework.utils.Debug
import com.topface.topface.data.FeedItem
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import java.lang.ref.WeakReference
import java.util.*

class ImprovedObservableList<T>(val observableList: ObservableArrayList<T> = ObservableArrayList())
    : ObservableList<T> by observableList {

    var weakListener: WeakReference<ObservableList.OnListChangedCallback<out ObservableList<T>>>? = null
    var canAddListener = false

    override fun addOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<T>>?) {
        if (canAddListener && this.weakListener == null) {
            canAddListener = false
            observableList.addOnListChangedCallback(listener)
            weakListener = WeakReference<ObservableList.OnListChangedCallback<out ObservableList<T>>>(listener)
        }
    }

    override fun removeOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<T>>?) {
        if (listener != null) {
            observableList.removeOnListChangedCallback(listener)
        }
    }

    fun isListenerAdded() = weakListener != null && weakListener?.get() != null

    fun removeListener() {
        val listener = weakListener
        if (listener != null && listener.get() != null) {
            removeOnListChangedCallback(listener.get())
            listener.clear()
        }
        weakListener = null
    }
}

@BindingAdapter("bindDataToFeedRecycler")
fun bindDataToAutoCompleteTextView(recyclerView: RecyclerView, data: ImprovedObservableList<FeedItem>) {
    data.canAddListener = true
    if (!data.isListenerAdded()) {
        val adapter = recyclerView.adapter as CompositeAdapter
        data.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableArrayList<FeedItem>>() {

            override fun onItemRangeRemoved(objects: ObservableArrayList<FeedItem>, positionStart: Int, itemCount: Int) {
                Log.d("ImprovedObservableList", " onItemRangeRemoved")
                Debug.log("EPTA onItemRangeRemoved " + objects.size)
                if (itemCount == 1) {
                    adapter.data.removeAt(positionStart)
                } else {
                    adapter.data.removeAll(ArrayList(adapter.data.subList(positionStart, itemCount)))
                }
                adapter.notifyItemRangeRemoved(positionStart, itemCount)
            }

            override fun onItemRangeInserted(objects: ObservableArrayList<FeedItem>, positionStart: Int, itemCount: Int) {
                Log.d("ImprovedObservableList", " onItemRangeInserted")
                Debug.log("EPTA onItemRangeInserted" + " to pos " + positionStart + " count " + itemCount + " size " + objects.size)
                if (itemCount == 1) {
                    adapter.data.add(positionStart, objects[positionStart])
                    adapter.notifyItemInserted(positionStart)
                } else {
                    val insertedData = ArrayList(objects.subList(positionStart, objects.size))
                    Debug.log("EPTA onItemRangeInserted " + " sublist size " + insertedData.size)
                    adapter.data.addAll(positionStart, insertedData)
                    adapter.notifyItemRangeInserted(positionStart, itemCount)
                    if (positionStart == 0) {
                        recyclerView.layoutManager.scrollToPosition(0)
                    }
                }
            }

            override fun onItemRangeChanged(objects: ObservableArrayList<FeedItem>, positionStart: Int, itemCount: Int) {
                Log.d("ImprovedObservableList", " onChanged ")
                if (itemCount == 1) {
                    adapter.notifyItemChanged(positionStart)
                }
            }

            override fun onItemRangeMoved(objects: ObservableArrayList<FeedItem>, fromPosition: Int, toPosition: Int, itemCount: Int) {
                Log.d("ImprovedObservableList", " onItemRangeMoved")
            }

            override fun onChanged(objects: ObservableArrayList<FeedItem>) {
                Log.d("ImprovedObservableList", " onItemRangeChanged")
            }
        })
        adapter.doOnRelease {
            data.removeListener()
            data.canAddListener = false
        }
        if (data.isNotEmpty()) {
            adapter.data.addAll(data.toMutableList())
        }
    }
}
package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.databinding.BindingAdapter
import android.databinding.ObservableList
import android.support.v7.widget.RecyclerView
import com.topface.framework.utils.Debug
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.databinding.SingleObservableArrayList

/**
 * Created by tiberal on 04.12.16.
 */
object DialogBindingsAdapters {

    @JvmStatic
    @BindingAdapter("bindDataToContactsAdapter")
    fun bindDataToContactsAdapter(recyclerView: RecyclerView, observableArrayList: SingleObservableArrayList<*>) {
        if (!observableArrayList.isListenerAdded() && recyclerView.adapter is CompositeAdapter) {
            val adapter = recyclerView.adapter as CompositeAdapter
            observableArrayList.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableList<*>>() {
                override fun onChanged(objects: ObservableList<*>) {
                    Debug.log("EPTA onChanged" + objects.size)
                    adapter.data.add(objects)
                    adapter.notifyDataSetChanged()
                }

                override fun onItemRangeInserted(objects: ObservableList<*>, positionStart: Int, itemCount: Int) {
                    Debug.log("EPTA onItemRangeInserted" + objects.size)
                    adapter.data.addAll(objects.subList(positionStart, objects.size))
                    adapter.notifyItemRangeInserted(positionStart, itemCount)
                    if (positionStart == 0) {
                        recyclerView.layoutManager.scrollToPosition(0)
                    }
                }

                override fun onItemRangeRemoved(objects: ObservableList<*>, positionStart: Int, itemCount: Int) {
                    Debug.log("EPTA onItemRangeRemoved" + objects.size)
                    adapter.data.removeAll(adapter.data.subList(positionStart, itemCount))
                    adapter.notifyItemRangeRemoved(positionStart, itemCount)
                }

                override fun onItemRangeChanged(objects: ObservableList<*>, positionStart: Int, itemCount: Int) {
                    Debug.log("EPTA onItemRangeChanged" + objects.size)
                }

                override fun onItemRangeMoved(objects: ObservableList<*>, fromPosition: Int, toPosition: Int, itemCount: Int) {
                    Debug.log("EPTA onItemRangeMoved" + objects.size)
                }
            })
        }
    }

}

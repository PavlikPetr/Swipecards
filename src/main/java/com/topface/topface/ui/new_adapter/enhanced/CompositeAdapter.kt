package com.topface.topface.ui.new_adapter.enhanced

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.topface.framework.utils.Debug
import com.topface.topface.utils.ListUtils
import rx.Observable
import rx.Subscriber

/**
 * Адаптер - конструктор, реализуем компонент, подсовываем сюда и все работает
 * Created by tiberal on 28.11.16.
 */
class CompositeAdapter(var typeProvider: ITypeProvider, private var updaterEmitObject: () -> Bundle) : RecyclerView.Adapter<ViewHolder<ViewDataBinding>>() {

    val updateObservable: Observable<Bundle>
    private var mUpdateSubscriber: Subscriber<in Bundle>? = null
    private var mRecyclerView: RecyclerView? = null

    var data: MutableList<Any> = mutableListOf()
    val components: MutableMap<Int, AdapterComponent<*, *>> = mutableMapOf()

    init {
        updateObservable = Observable.create { subscriber ->
            mUpdateSubscriber = subscriber
            mUpdateSubscriber?.onNext(Bundle())
        }
    }

    inline fun <reified D : Any> addAdapterComponent(component: AdapterComponent<*, D>): CompositeAdapter {
        components.put(typeProvider.getType(D::class.java), component)
        return this
    }

    override fun onViewRecycled(holder: ViewHolder<ViewDataBinding>?) {
        super.onViewRecycled(holder)
        if (holder != null) {
            val position = mRecyclerView?.layoutManager?.getPosition(holder.itemView)
            if (position != null && ListUtils.isEntry(position, data)) {
                components[typeProvider.getType(data[position].javaClass)]?.onViewRecycled(holder, data[position], position)
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
        components.values.forEach {
            it.onAttachedToRecyclerView(recyclerView)
        }
        recyclerView?.layoutManager?.let {
            if (it is LinearLayoutManager) {
                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                        val subscriber = mUpdateSubscriber
                        if (subscriber != null) {
                            if (!data.isEmpty()) {
                                val firstVisibleItem = it.findFirstCompletelyVisibleItemPosition()
                                val lastVisibleItem = it.findLastVisibleItemPosition()
                                val visibleItemCount = lastVisibleItem - firstVisibleItem + 1
                                if (firstVisibleItem != RecyclerView.NO_POSITION &&
                                        lastVisibleItem != RecyclerView.NO_POSITION && visibleItemCount != 0 &&
                                        firstVisibleItem + visibleItemCount >= data.size - 1) {
                                    subscriber.onNext(updaterEmitObject())
                                }
                            } else {
                                subscriber.onNext(Bundle())
                            }
                        }
                    }
                })
            } else {
                Debug.debug(this, "Wrong layout manager")
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<ViewDataBinding>?, position: Int) {
        components[typeProvider.getType(data[position].javaClass)]?.onBindViewHolder(holder, data[position], position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ViewDataBinding>? {
        val component = components[viewType]
        val inflater = LayoutInflater.from(parent.context)
        return if (component == null || inflater == null) {
            null
        } else {
            ViewHolder(DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(parent.context),
                    component.itemLayout, parent, false))
        }
    }

    override fun getItemViewType(position: Int) = typeProvider.getType(data[position].javaClass)

    override fun getItemCount() = data.count()

    fun releaseComponents() = components.values.forEach(AdapterComponent<*, *>::release)
}
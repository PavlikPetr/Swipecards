package com.topface.topface.ui.new_adapter.enhanced

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
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
class CompositeAdapter(var typeProvider: ITypeProvider, provideItemTypeStrategyType: Int = ProvideItemTypeStrategyFactory.DEFAULT,
                       private var updaterEmitObject: (CompositeAdapter) -> Bundle) : RecyclerView.Adapter<ViewHolder<ViewDataBinding>>() {

    val updateObservable: Observable<Bundle>
    private var mUpdateSubscriber: Subscriber<in Bundle>? = null
    private var mRecyclerView: RecyclerView? = null
    private var doOnRelease: (() -> Unit)? = null

    var provideItemTypeStrategy = ProvideItemTypeStrategyFactory(typeProvider).construct(provideItemTypeStrategyType)
    var data: MutableList<Any> = mutableListOf()
    val components: MutableMap<Int, AdapterComponent<*, *>> = mutableMapOf()

    private val mOnLinearLayoutManagerScrollListener by lazy {
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                (recyclerView?.layoutManager as? LinearLayoutManager)?.let {
                    mUpdateSubscriber?.let { subscriber ->
                        if (!data.isEmpty()) {
                            val firstVisibleItem = it.findFirstCompletelyVisibleItemPosition()
                            val lastVisibleItem = it.findLastVisibleItemPosition()
                            val visibleItemCount = lastVisibleItem - firstVisibleItem + 1
                            if (firstVisibleItem != RecyclerView.NO_POSITION &&
                                    lastVisibleItem != RecyclerView.NO_POSITION && visibleItemCount != 0 &&
                                    firstVisibleItem + visibleItemCount >= data.size - 1) {
                                subscriber.onNext(updaterEmitObject(this@CompositeAdapter))
                            }
                        } else {
                            subscriber.onNext(Bundle())
                        }
                    }
                }
            }
        }
    }

    private val mOnStaggeredGridLayoutManagerScrollListener by lazy {
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                (recyclerView?.layoutManager as? StaggeredGridLayoutManager)?.let {
                    mUpdateSubscriber?.let { subscriber ->
                        if (!data.isEmpty()) {
                            val firstVisibleItemsList = it.findFirstCompletelyVisibleItemPositions(null)
                            val lastVisibleItemsList = it.findLastVisibleItemPositions(null)
                            val visibleItemCount = (lastVisibleItemsList.getOrNull(lastVisibleItemsList.lastIndex) ?: 0) - (firstVisibleItemsList.getOrNull(0) ?: 0) + 1
                            if (firstVisibleItemsList != null &&
                                    lastVisibleItemsList != null && visibleItemCount != 0 &&
                                    (firstVisibleItemsList.getOrNull(0) ?: 0) + visibleItemCount >= data.size - 1) {
                                subscriber.onNext(updaterEmitObject(this@CompositeAdapter))
                            }
                        } else {
                            subscriber.onNext(Bundle())
                        }
                    }
                }
            }
        }
    }

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
        holder?.let {
            mRecyclerView?.layoutManager?.getPosition(it.itemView)?.let {
                if (ListUtils.isEntry(it, data)) {
                    components[provideItemTypeStrategy.provide(data[it])]?.onViewRecycled(holder, data[it], it)
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
        components.values.forEach {
            it.onAttachedToRecyclerView(recyclerView)
        }
        recyclerView?.let {
            when (it.layoutManager) {
                is LinearLayoutManager -> it.addOnScrollListener(mOnLinearLayoutManagerScrollListener)
                is StaggeredGridLayoutManager -> it.addOnScrollListener(mOnStaggeredGridLayoutManagerScrollListener)
                else -> Debug.debug(this, "Wrong layout manager")
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder<ViewDataBinding>?, position: Int) {
        components[provideItemTypeStrategy.provide(data[position])]?.onBindViewHolder(holder, data[position], position)
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

    override fun getItemViewType(position: Int) = provideItemTypeStrategy.provide(data[position])

    override fun getItemCount() = data.count()


    fun releaseComponents() {
        doOnRelease?.invoke()
        doOnRelease = null
        components.values.forEach(AdapterComponent<*, *>::release)
        mRecyclerView?.removeOnScrollListener(mOnLinearLayoutManagerScrollListener)
        mRecyclerView?.removeOnScrollListener(mOnStaggeredGridLayoutManagerScrollListener)
        mUpdateSubscriber = null
        mRecyclerView = null
    }

    fun doOnRelease(onRelease: () -> Unit) {
        doOnRelease = onRelease
    }
}
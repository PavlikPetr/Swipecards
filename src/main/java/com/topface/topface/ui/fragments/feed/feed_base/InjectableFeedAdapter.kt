package com.topface.topface.ui.fragments.feed.feed_base

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.topface.topface.App
import com.topface.topface.data.FeedItem
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter
import com.topface.topface.utils.adapter_utils.InjectViewBucket
import com.topface.topface.utils.adapter_utils.ViewInjectManager

/**
 * Адаптер который позволяет втыкать в лист вьюхи, которые не будут отображены в данных. Учитывается смещение позиции, и профие радости
 * Created by tiberal on 07.09.16.
 */
abstract class InjectableFeedAdapter<T : ViewDataBinding, D : FeedItem> : BaseRecyclerViewAdapter<T, D>() {

    private val mInjectManager by lazy {
        ViewInjectManager(App.getContext())
    }

    private var rec: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView?) {
        super.onAttachedToRecyclerView(recyclerView)
        rec = recyclerView
    }

    override fun getItemId(position: Int) =
            if (mInjectManager.isFakePosition(position)) {
                position.toLong()
            } else {
                getDataItem(position).id?.toLong() ?: mInjectManager.getTruePosition(position).toLong()
            }


    override fun getItemCount() = super.getItemCount() + mInjectManager.viewBucketsCount

    override fun getDataItem(position: Int): D = data[if (mInjectManager.hasInjectableView()) {
        mInjectManager.getTruePosition(position)
    } else {
        position
    }]

    /**
     * Если позиция по которой нужно получить тип находится в числе фейковых то прокидываем дальше позицию фейка как тип
     */
    override fun getItemViewType(position: Int): Int {
        return if (mInjectManager.isFakePosition(position)) position else TYPE_ITEM
    }

    /**
     * Если тип итемы не соответствует обычному итему, то считаем, что нам прислали позицию фейка в viewType
     * в таком случае создаем холдер именно для фейка
     */
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ItemViewHolder {
        if (viewType != TYPE_ITEM && !data.isEmpty()) {
            val view = mInjectManager.getView(viewType, parent)
            if (view?.tag is ViewDataBinding) {
                return ItemViewHolder(view?.tag as ViewDataBinding, null)
            } else {
                return ItemViewHolder(view, null)
            }
        }
        return super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: ItemViewHolder?, position: Int) {
        if (!mInjectManager.isFakePosition(position)) {
            super.onBindViewHolder(holder, mInjectManager.getTruePosition(position))
        }
    }

    fun registerViewBucket(bucket: InjectViewBucket) {
        mInjectManager.registerInjectViewBucket(bucket)
    }

    fun removeViewBucket(bucket: InjectViewBucket) {
        mInjectManager.removeInjectViewBucket(bucket)
    }

    fun removeAllBuckets() {
        mInjectManager.removeAllBuckets()
    }

}
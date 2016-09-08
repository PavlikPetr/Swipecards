package com.topface.topface.ui.fragments.feed.feed_base

import android.databinding.ViewDataBinding
import android.view.View
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

    override fun getItemId(position: Int) =
            if (mInjectManager.isFakePosition(position) || position >= super.getItemCount() - 1) {
                position.toLong()
            } else {
                getDataItem(mInjectManager.getTruePosition(position)).id?.toLong() ?: mInjectManager.getTruePosition(position).toLong()
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
        if (viewType != TYPE_ITEM) {
            val view = mInjectManager.getView(viewType, parent)
            if (data.isEmpty()) {
                //прячем вьюху, чтоб ее небыбыло видно, если список пустой
                view?.visibility = View.GONE
            }
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
        } else {
            if (!data.isEmpty()) {
                holder?.itemView?.visibility = View.VISIBLE
            }

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
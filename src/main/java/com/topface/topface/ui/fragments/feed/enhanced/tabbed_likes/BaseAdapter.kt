package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.likes.IViewModel
import com.topface.topface.ui.new_adapter.enhanced.IAdapter
import rx.Observable

abstract class BaseAdapter<in T : ViewDataBinding, in D : Any> : BaseAdapter(), IAdapter {

    override var data: MutableList<Any> = mutableListOf()

    private val mInflater by lazy {
        App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount() = data.size

    override fun getItem(position: Int) = data.getOrNull(position)

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
            convertView?.apply {
                (getItem(position) as? D)?.let {
                    findViewModel(DataBindingUtil.getBinding<T>(convertView)).update(it)
                }
            } ?: getBinding(parent).apply {
                (getItem(position) as? D)?.let {
                    bind(this, it)
                }
            }.root

    private fun getBinding(parent: ViewGroup?) = DataBindingUtil.inflate<T>(mInflater, layout, parent, false)

    private fun bind(binding: T?, data: D?) {
        data?.let {
            binding?.setVariable(variableId, getViewModel(it))
        }
    }

    abstract fun findViewModel(binding: T): IViewModel<D>

    abstract fun getViewModel(data: D): IViewModel<D>

    abstract val variableId: Int

    abstract val layout: Int

    override fun releaseComponents() {
    }

    override fun notifyItemChanged(position: Int) {
        notifyDataSetChanged()
    }

    override fun notifyItemChanged(position: Int, payload: Any) {
        notifyDataSetChanged()
    }

    override fun notifyItemRangeChanged(positionStart: Int, itemCount: Int) {
        notifyDataSetChanged()
    }

    override fun notifyItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any) {
        notifyDataSetChanged()
    }

    override fun notifyItemInserted(position: Int) {
        notifyDataSetChanged()
    }

    override fun notifyItemMoved(fromPosition: Int, toPosition: Int) {
        notifyDataSetChanged()
    }

    override fun notifyItemRangeInserted(positionStart: Int, itemCount: Int) {
        notifyDataSetChanged()
    }

    override fun notifyItemRemoved(position: Int) {
        notifyDataSetChanged()
    }

    override fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int) {
        notifyDataSetChanged()
    }

    override val updateObservable: Observable<Bundle>
        get() = Observable.create {}
}
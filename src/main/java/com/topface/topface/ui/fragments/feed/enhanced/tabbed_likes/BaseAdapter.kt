package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.topface.topface.App

/**
 * Created by ppavlik on 18.07.17.
 */
abstract class BaseAdapter<T : ViewDataBinding, D> : BaseAdapter() {

    private var mData = arrayListOf<D>()

    private val mInflater by lazy {
        App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount() = mData.size

    override fun getItem(position: Int) = mData.getOrNull(position)

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
            getBinding(parent).apply {
                bind(this, getItem(position))
            }.root
//        val view = convertView ?: inflate(parent)
//        val viewHolder = convertView?.let {
//            it.tag as? T
//        } ?: createViewHolder().apply {
//            view.tag = this
//        }
//        viewHolder?.let { viewHolder ->
//            getItem(position)?.let {
//                fillView(it, viewHolder)
//            }
//        }
//        return view
//    }

    fun setData(data: ArrayList<D>) {
        mData = data
    }

    fun addData(data: ArrayList<D>) {
        mData.addAll(data)
    }

    private fun getBinding(parent: ViewGroup?) = DataBindingUtil.inflate<T>(mInflater, layout, parent, false)

    abstract fun bind(binding: T?, data: D?)

    abstract val layout: Int
}
package com.topface.topface.ui.new_adapter.enhanced

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

class ViewHolder<out T : ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root)
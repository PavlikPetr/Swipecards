package com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign

import android.databinding.ViewDataBinding
import com.topface.topface.data.FeedDialog
import com.topface.topface.ui.new_adapter.enhanced.AdapterComponent

/**
 * Adapter component for pyre
 * Created by tiberal on 30.11.16.
 */
class DialogItemComponent : AdapterComponent<ViewDataBinding, FeedDialog>(){
    override val itemLayout: Int
        get() = throw UnsupportedOperationException()
    override val bindingClass: Class<ViewDataBinding>
        get() = throw UnsupportedOperationException()

    override fun bind(binding: ViewDataBinding, data: FeedDialog?, position: Int) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
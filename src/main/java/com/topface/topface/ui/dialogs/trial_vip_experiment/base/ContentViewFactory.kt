package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.OldTrialContentBinding
import org.jetbrains.anko.layoutInflater

/**
 * Фабрика вьюшек которые нужно воткунить на место контента в разметке
 * Created by tiberal on 16.11.16.
 */
class ContentViewFactory(private val mContext: Context, val parent: ViewGroup, val args: Bundle) {

    fun createTrialView() =
            DataBindingUtil.inflate<OldTrialContentBinding>(mContext.layoutInflater,
                    R.layout.old_trial_content, parent, false)
}
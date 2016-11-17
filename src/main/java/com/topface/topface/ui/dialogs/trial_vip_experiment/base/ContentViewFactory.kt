package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.Experiment123ContentViewBinding
import com.topface.topface.ui.dialogs.trial_vip_experiment.experiment_1_2_3.Experiment1_2_3_ViewModel
import org.jetbrains.anko.layoutInflater

/**
 * Created by tiberal on 16.11.16.
 */
class ContentViewFactory(private val mContext: Context, val parent: ViewGroup) : IBoilerplateFactory<ViewDataBinding> {

    override fun construct(@ExperimentsType.ExperimentsType type: Long): ViewDataBinding =
            when (type) {
                ExperimentsType.EXPERIMENT_1 -> {
                    DataBindingUtil.inflate<Experiment123ContentViewBinding>(mContext.layoutInflater,
                            R.layout.experiment_1_2_3_content_view, parent, false).apply {
                        viewModel = Experiment1_2_3_ViewModel()
                        capabilitiesIcons.post {
                            indicatorCircles.setViewPager(capabilitiesIcons)
                        }
                    }
                }
            /*
            ExperimentsType.EXPERIMENT_2 -> {
            }
            ExperimentsType.EXPERIMENT_3 -> {
            }
            ExperimentsType.EXPERIMENT_4 -> {
            }
            ExperimentsType.EXPERIMENT_5 -> {
            }
            */
                else -> DataBindingUtil.inflate<ViewDataBinding>(mContext.layoutInflater,
                        R.layout.experiment_boilerplate_layout, null, false)

            }

}
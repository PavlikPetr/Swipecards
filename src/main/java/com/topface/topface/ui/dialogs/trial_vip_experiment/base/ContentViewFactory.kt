package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.Experiment123ContentViewBinding
import com.topface.topface.databinding.LayoutExperiment5Binding
import com.topface.topface.databinding.LayoutExperiment6Binding
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_2
import com.topface.topface.ui.dialogs.trial_vip_experiment.experiment_1_2_3.Experiment1_2_3_ViewModel
import org.jetbrains.anko.layoutInflater

/**
 * Фабрика вьюшек которые нужно воткунить на место контента в разметке
 * Created by tiberal on 16.11.16.
 */
class ContentViewFactory(private val mContext: Context, val parent: ViewGroup) : IBoilerplateFactory<ViewDataBinding> {

    override fun construct(@ExperimentsType.ExperimentsType type: Long): ViewDataBinding =
            when (type) {
                ExperimentsType.EXPERIMENT_1, EXPERIMENT_2, EXPERIMENT_2 -> {
                    DataBindingUtil.inflate<Experiment123ContentViewBinding>(mContext.layoutInflater,
                            R.layout.experiment_1_2_3_content_view, parent, false).apply {
                        viewModel = Experiment1_2_3_ViewModel()
                        capabilitiesIcons.post {
                            indicatorCircles.setViewPager(capabilitiesIcons)
                        }
                    }
                }
            /*
            ExperimentsType.EXPERIMENT_4 -> {
            }*/
                ExperimentsType.EXPERIMENT_5 -> {
                    DataBindingUtil.inflate<LayoutExperiment5Binding>(mContext.layoutInflater,
                            R.layout.layout_experiment_5, parent, false)
                }
                else -> DataBindingUtil.inflate<LayoutExperiment6Binding>(mContext.layoutInflater,
                        R.layout.layout_experiment_6, parent, false)

            }

}
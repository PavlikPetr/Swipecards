package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.*
import com.topface.topface.ui.dialogs.trial_vip_experiment.Experiment4WithBlurViewModel
import com.topface.topface.ui.dialogs.trial_vip_experiment.Experiment4WithoutBlurViewModel
import com.topface.topface.databinding.Experiment123ContentViewBinding
import com.topface.topface.databinding.LayoutExperiment5Binding
import com.topface.topface.databinding.LayoutExperiment6Binding
import com.topface.topface.databinding.OldTrialContentBinding
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_2
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_3
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_SUBTYPE
import com.topface.topface.ui.dialogs.trial_vip_experiment.experiment_1_2_3.Experiment1_2_3_Adapter
import com.topface.topface.ui.dialogs.trial_vip_experiment.experiment_1_2_3.Experiment1_2_3_ViewModel
import org.jetbrains.anko.layoutInflater

/**
 * Фабрика вьюшек которые нужно воткунить на место контента в разметке
 * Created by tiberal on 16.11.16.
 */
class ContentViewFactory(private val mContext: Context, val parent: ViewGroup,
                         val args: Bundle) : IBoilerplateFactory<ViewDataBinding> {

    override fun construct(@ExperimentsType.ExperimentsType type: Long): ViewDataBinding =
            when (type) {
                ExperimentsType.EXPERIMENT_0 -> {
                    DataBindingUtil.inflate<OldTrialContentBinding>(mContext.layoutInflater,
                            R.layout.old_trial_content, parent, false)
                }
                ExperimentsType.EXPERIMENT_1, EXPERIMENT_2, EXPERIMENT_3 -> {
                    DataBindingUtil.inflate<Experiment123ContentViewBinding>(mContext.layoutInflater,
                            R.layout.experiment_1_2_3_content_view, parent, false).apply {
                        viewModel = Experiment1_2_3_ViewModel(args.getInt(Experiment1_2_3_Adapter.MODE))
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
           */
                ExperimentsType.EXPERIMENT_4 -> subTypeChooser(args.getLong(EXPERIMENT_SUBTYPE))

                ExperimentsType.EXPERIMENT_5 -> {
                    DataBindingUtil.inflate<LayoutExperiment5Binding>(mContext.layoutInflater,
                            R.layout.layout_experiment_5, parent, false)
                }
                else -> DataBindingUtil.inflate<LayoutExperiment6Binding>(mContext.layoutInflater,
                        R.layout.layout_experiment_6, parent, false)

            }

    fun subTypeChooser(subType: Long): ViewDataBinding {
        when (subType) {
            ExperimentsType.SubType4_2 -> {
                return DataBindingUtil.inflate<Experiment4Binding>(mContext.layoutInflater, R.layout.experiment4, parent, false)
                        .apply { viewModel = Experiment4WithoutBlurViewModel(this) }
            }
            ExperimentsType.SubType4_3 -> {
                return DataBindingUtil.inflate<Experiment4Binding>(mContext.layoutInflater, R.layout.experiment4, parent, false)
                        .apply { viewModel = Experiment4WithBlurViewModel(this) }
            }
            else -> return DataBindingUtil.inflate<ViewDataBinding>(mContext.layoutInflater,
                    R.layout.experiment_boilerplate_layout, null, false)
        }
    }
}
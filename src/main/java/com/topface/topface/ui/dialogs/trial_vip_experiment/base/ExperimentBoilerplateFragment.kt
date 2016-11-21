package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.databinding.ExperimentBoilerplateLayoutBinding
import com.topface.topface.ui.dialogs.trial_vip_experiment.TransparentMarketFragmentRunner
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentSubType.EXPERIMENT_SUBTYPE
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_TYPE
import org.jetbrains.anko.layoutInflater

/**
 * База для всех экспериментов
 * Created by tiberal on 15.11.16.
 */
class ExperimentBoilerplateFragment : DialogFragment(), TransparentMarketFragmentRunner.IRunner {

    companion object {
        @JvmStatic fun newInstance(@ExperimentsType.ExperimentsType type: Long, @ExperimentSubType.ExperimentsSubType subType: Long) =
                with(ExperimentBoilerplateFragment()) {
                    arguments = Bundle().apply {
                        putLong(EXPERIMENT_TYPE, type)
                        putLong(EXPERIMENT_SUBTYPE, subType)
                    }
                    this
                }
    }

    private val mType by lazy {
        arguments.getLong(EXPERIMENT_TYPE)
    }
    private val mSubType by lazy {
        arguments.getLong(EXPERIMENT_SUBTYPE)
    }

    private val mDialogMetricsFactory by lazy {
        MetricsFactory().construct(mType, mSubType)
    }

    private val mDialogDataFactory by lazy {
        BoilerplateDataFactory().construct(mType, mSubType)
    }

    private val mContentBinding by lazy {
        ContentViewFactory(context.applicationContext, mBinding.content).construct(mType, mSubType)
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<ExperimentBoilerplateLayoutBinding>(context.layoutInflater,
                R.layout.experiment_boilerplate_layout, null, false)
    }

    private val mBoilerplateViewModel by lazy {
        ExperimentBoilerplateViewModel(mPopupRunner = this, dialogMetrics = mDialogMetricsFactory,
                dialogData = mDialogDataFactory)
    }

    private val mMarketFragmentRunner by lazy {
        TransparentMarketFragmentRunner(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_Topface_NoActionBar)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = with(mBinding) {
        viewModel = mBoilerplateViewModel
        root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.content.addView(mContentBinding.root)
    }

    override fun runMarketPopup() = mMarketFragmentRunner.startTransparentMarketFragment { dismiss() }

}

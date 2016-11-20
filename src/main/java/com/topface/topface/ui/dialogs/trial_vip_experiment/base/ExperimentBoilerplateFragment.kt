package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.ExperimentBoilerplateLayoutBinding
import com.topface.topface.ui.dialogs.trial_vip_experiment.IOnFragmentFinishDelegate
import com.topface.topface.ui.dialogs.trial_vip_experiment.TransparentMarketFragmentRunner
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_TYPE
import org.jetbrains.anko.layoutInflater

/**
 * База для всех экспериментов
 * Created by tiberal on 15.11.16.
 */
class ExperimentBoilerplateFragment : DialogFragment(), TransparentMarketFragmentRunner.IRunner {

    var cancelListener: DialogInterface.OnCancelListener? = null
    var dismissListener: DialogInterface.OnDismissListener? = null
    var onFragmentFinishDelegate: IOnFragmentFinishDelegate? = null

    companion object {
        const val TAG = "TrialVipPopup"
        const val SKIP_SHOWING_CONDITION = "skip_showing_condition"
        @JvmOverloads @JvmStatic fun newInstance(@ExperimentsType.ExperimentsType type: Long =
                                                 App.get().options.trialVipExperiment.androidTrialPopupExp,
                                                 skipShowingCondition: Boolean = false, args: Bundle = Bundle()) =
                with(ExperimentBoilerplateFragment()) {
                    arguments = args.apply {
                        putLong(EXPERIMENT_TYPE, type)
                        putBoolean(SKIP_SHOWING_CONDITION, skipShowingCondition)
                    }
                    this
                }
    }

    private val mType by lazy {
        arguments.getLong(EXPERIMENT_TYPE)
    }

    private val mDialogMetricsFactory by lazy {
        MetricsFactory().construct(mType)
    }

    private val mDialogDataFactory by lazy {
        BoilerplateDataFactory().construct(mType)
    }

    private val mContentBinding by lazy {
        ContentViewFactory(context.applicationContext, mBinding.content, arguments).construct(mType)
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

    private fun incrPopupShowCounter() = with(App.getUserConfig()) {
        setTrialVipPopupCounter(trialVipCounter + 1)
        saveConfig()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        cancelListener?.onCancel(dialog)
        if (onFragmentFinishDelegate != null) {
            onFragmentFinishDelegate!!.closeFragmentByForm()
        }
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss(dialog)
        with(arguments) {
            if (this != null && !getBoolean(SKIP_SHOWING_CONDITION)) {
                incrPopupShowCounter()
            }
        }
    }

    override fun onDestroy() {
        cancelListener = null
        dismissListener = null
        onFragmentFinishDelegate = null
        super.onDestroy()
    }

    override fun runMarketPopup() = mMarketFragmentRunner.startTransparentMarketFragment { dismiss() }

}

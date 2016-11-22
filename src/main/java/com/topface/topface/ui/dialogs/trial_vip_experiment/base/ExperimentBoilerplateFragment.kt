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
import com.topface.topface.ui.dialogs.trial_vip_experiment.TrialVipExperimentStatistics
import com.topface.topface.ui.dialogs.trial_vip_experiment.base.ExperimentsType.EXPERIMENT_SUBTYPE
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
    private var mArgs: Bundle? = null

    companion object {
        const val TAG = "TrialVipPopup"
        const val SKIP_SHOWING_CONDITION = "skip_showing_condition"
        @JvmOverloads @JvmStatic fun newInstance(@ExperimentsType.ExperimentsType type: Long =
                                                 App.get().options.trialVipExperiment.androidTrialPopupExp,
                                                 @ExperimentsType.ExperimentsSubType subType: Long = 0L,
                                                 skipShowingCondition: Boolean = false, args: Bundle = Bundle()) =
                with(ExperimentBoilerplateFragment()) {
                    arguments = args.apply {
                        putLong(EXPERIMENT_TYPE, type)
                        putLong(EXPERIMENT_SUBTYPE, subType)
                        putBoolean(SKIP_SHOWING_CONDITION, skipShowingCondition)
                    }
                    this
                }
    }

    private val mType by lazy {
        mArgs?.getLong(EXPERIMENT_TYPE)
    }

    private val mDialogMetricsFactory by lazy {
        mArgs?.let { mType?.let { it1 -> MetricsFactory(it).construct(it1) } }
    }

    private val mDialogDataFactory by lazy {
        mArgs?.let { mType?.let { it1 -> BoilerplateDataFactory(context.applicationContext, mBinding.content, it).construct(it1) } }
    }

    private val mContentBinding by lazy {
        mArgs?.let { mType?.let { it1 -> ContentViewFactory(context.applicationContext, mBinding.content, it).construct(it1) } }
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<ExperimentBoilerplateLayoutBinding>(context.layoutInflater,
                R.layout.experiment_boilerplate_layout, null, false)
    }

    private val mBoilerplateViewModel by lazy {
        mDialogMetricsFactory?.let {
            mDialogDataFactory?.let { it1 ->
                ExperimentBoilerplateViewModel(mPopupRunner = this, dialogMetrics = it,
                        dialogData = it1)
            }
        }
    }

    private val mMarketFragmentRunner by lazy {
        TransparentMarketFragmentRunner(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            with(App.getUserConfig()) {
                val showCounter = trialVipShowCounter + 1
                TrialVipExperimentStatistics.sendPopupShow(showCounter)
                setTrialVipPopupShowCounter(showCounter)
                saveConfig()
            }

        }
        setStyle(STYLE_NO_FRAME, R.style.Theme_Topface_NoActionBar)
        mArgs = arguments
        mArgs = if (mArgs == null) savedInstanceState else mArgs
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = with(mBinding) {
        viewModel = mBoilerplateViewModel
        root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.content.addView(mContentBinding?.root)
    }

    private fun incrPopupShowCounter() = with(App.getUserConfig()) {
        setQueueTrialVipPopupCounter(queueTrialVipCounter + 1)
        saveConfig()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        TrialVipExperimentStatistics.sendPopupClose()
        cancelListener?.onCancel(dialog)
        onFragmentFinishDelegate?.closeFragmentByForm()
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

    override fun runMarketPopup() = mMarketFragmentRunner.startTransparentMarketFragment {
        TrialVipExperimentStatistics.sendPurchaseCompleted()
        dismiss()
    }
}

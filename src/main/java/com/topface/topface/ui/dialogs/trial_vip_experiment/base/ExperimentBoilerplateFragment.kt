package com.topface.topface.ui.dialogs.trial_vip_experiment.base

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.IdRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.statistics.generated.NewProductsKeysGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Profile
import com.topface.topface.databinding.ExperimentBoilerplateLayoutBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.statistics.FBStatistics
import com.topface.topface.ui.DialogFragmentWithSafeTransaction
import com.topface.topface.ui.dialogs.trial_vip_experiment.IOnFragmentFinishDelegate
import com.topface.topface.ui.dialogs.trial_vip_experiment.IRunner
import com.topface.topface.ui.dialogs.trial_vip_experiment.TrialVipExperimentStatistics
import com.topface.topface.ui.fragments.buy.GpPurchaseActivity
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.jetbrains.anko.layoutInflater
import rx.Subscription

/**
 * База для всех экспериментов
 * Created by tiberal on 15.11.16.
 */
class ExperimentBoilerplateFragment : DialogFragmentWithSafeTransaction(), IRunner {

    var cancelListener: DialogInterface.OnCancelListener? = null
    var dismissListener: DialogInterface.OnDismissListener? = null
    var onFragmentFinishDelegate: IOnFragmentFinishDelegate? = null
    private lateinit var mArgs: Bundle
    private var mPremiumStatusSubscription: Subscription

    companion object {
        const val TAG = "TrialVipPopup"
        const val SKIP_SHOWING_CONDITION = "skip_showing_condition"
        const val FRAGMENT_CONTAINER_ID = "fragment_container_id"
        @JvmOverloads @JvmStatic fun newInstance(@IdRes fragmentContainerId: Int = R.id.fragment_content,
                                                 skipShowingCondition: Boolean = false, args: Bundle = Bundle()) =
                with(ExperimentBoilerplateFragment()) {
                    arguments = args.apply {
                        putBoolean(SKIP_SHOWING_CONDITION, skipShowingCondition)
                        putInt(FRAGMENT_CONTAINER_ID, fragmentContainerId)
                    }
                    this
                }
    }

    private val mAppState: TopfaceAppState by lazy {
        App.getAppComponent().appState()
    }

    private val mNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private val mDialogMetricsFactory by lazy {
        MetricsFactory(mArgs).createBoilerplateDialogMetrics()
    }

    private val mDialogDataFactory by lazy {
        BoilerplateDataFactory(mBinding.content, mArgs).createBoilerplateData()
    }

    private val mContentBinding by lazy {
        ContentViewFactory(context.applicationContext, mBinding.content, mArgs).createTrialView()
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<ExperimentBoilerplateLayoutBinding>(context.layoutInflater,
                R.layout.experiment_boilerplate_layout, null, false)
    }

    private val mBoilerplateViewModel by lazy {
        ExperimentBoilerplateViewModel(mPopupRunner = this, dialogMetrics = mDialogMetricsFactory,
                dialogData = mDialogDataFactory) {
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NewProductsKeysGeneratedStatistics.sendNow_TRIAL_VIP_POPUP_SHOW(activity.applicationContext)
        FBStatistics.onContentViewed(FBStatistics.PLACE_POPUP_VIP_TRIAL)
        if (savedInstanceState == null) {
            with(App.getUserConfig()) {
                val showCounter = trialVipShowCounter + 1
                TrialVipExperimentStatistics.sendPopupShow(showCounter)
                setTrialVipPopupShowCounter(showCounter)
                saveConfig()
            }
        }
        setStyle(STYLE_NO_FRAME, R.style.Theme_Topface_NoActionBar)
        mArgs = savedInstanceState ?: arguments
    }

    init {
        mPremiumStatusSubscription = mAppState.getObservable(Profile::class.java)
                .filter { it.premium }
                .applySchedulers()
                .subscribe(shortSubscription {
                    dismiss()
                    FBStatistics.onVipTrialStarted()
                })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putAll(mArgs)
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
        mPremiumStatusSubscription.safeUnsubscribe()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GpPurchaseActivity.ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            TrialVipExperimentStatistics.sendPurchaseCompleted()
            dismiss()
        }
    }

    override fun runMarketPopup() {
        mNavigator.showPurchaseProduct(App.get().options.trialVipExperiment.subscriptionSku, TAG)
    }
}

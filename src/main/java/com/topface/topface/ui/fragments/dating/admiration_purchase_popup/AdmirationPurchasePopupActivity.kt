package com.topface.topface.ui.fragments.dating.admiration_purchase_popup

import android.os.Bundle
import android.transition.Transition
import com.topface.statistics.generated.NewProductsKeysGeneratedStatistics
import com.topface.topface.R
import com.topface.topface.databinding.AdmirationPurchasePopupBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.analytics.TrackedFragmentActivity
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.views.toolbar.view_models.InvisibleToolbarViewModel
import com.topface.topface.utils.Utils
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscription

/**
 * Это активити попата восхищения. Такие дела.
 * Created by siberia87 on 01.11.16.
 */
class AdmirationPurchasePopupActivity : TrackedFragmentActivity<AdmirationPurchasePopupBinding>(), IAdmirationPurchasePopupHide {

    override fun getToolbarBinding(binding: AdmirationPurchasePopupBinding) = binding.toolbarInclude

    override fun getLayout() = R.layout.admiration_purchase_popup

    override fun generateToolbarViewModel(toolbar: ToolbarBinding) = InvisibleToolbarViewModel(toolbar)

    var isFinishTransition = false

    companion object {
        const val INTENT_ADMIRATION_PURCHASE_POPUP = 69
        const val CURRENT_USER = "current_user"
        const val FINISH_TRANSITION_KEY = "finish_transition"
    }

    private val mAdmirationPurchasePopupViewModel by lazy {
        AdmirationPurchasePopupViewModel(viewBinding, mAdmirationPurchasePopupHide = this, mNavigator = mNavigator,
                currentUser = intent.getParcelableExtra(CURRENT_USER))
    }

    private val mNavigator by lazy {
        FeedNavigator(this)
    }

    private var mDatingGoAdmirationSubscription: Subscription? = null

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putBoolean(FINISH_TRANSITION_KEY, isFinishTransition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        isFinishTransition = savedInstanceState?.getBoolean(FINISH_TRANSITION_KEY) ?: isFinishTransition
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        // Используется метод setViewModel() поскольку при работе с пропертей viewModel возникает ошибка: Unresolved reference
        viewBinding.setViewModel(mAdmirationPurchasePopupViewModel)
        if (Utils.isLollipop()) {
            FabTransform.setup(this, viewBinding.container)
            // Жду пока закончится анимация и после меняю значение флага, иначе апа будет валиться.
            window.enterTransition.addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(p0: Transition?) {
                    isFinishTransition = !isFinishTransition
                }

                override fun onTransitionResume(p0: Transition?) {
                }

                override fun onTransitionPause(p0: Transition?) {
                }

                override fun onTransitionCancel(p0: Transition?) {
                }

                override fun onTransitionStart(p0: Transition?) {
                }

            })
        }
    }

    override fun onResume() {
        super.onResume()
        mDatingGoAdmirationSubscription = NewProductsKeysGeneratedStatistics.sendPost_DATING_GO_ADMIRATION(applicationContext)
    }

    override fun onPause() {
        super.onPause()
        mDatingGoAdmirationSubscription.safeUnsubscribe()
    }

    override fun hideAdmirationPurchasePopup(resultCode: Int) {
        // Сие славное условие значит, что клики по кнопке восхищения будут игнорироваться, пока не закончится анимация
        if (Utils.isLollipop() && !isFinishTransition) return
        setResult(resultCode)
        if (Utils.isLollipop()) {
            if (resultCode == AdmirationPurchasePopupViewModel.RESULT_USER_BUY_VIP) {
                finish()
            } else {
                finishAfterTransition()
            }
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        // Да-да, вот так грубо пропускаем super только, когда по значениею флага isFinishTransition убедимся,
        // что анимация открытия попапа закончилась.
        if (Utils.isLollipop() && !isFinishTransition) return
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdmirationPurchasePopupViewModel.release()
    }
}

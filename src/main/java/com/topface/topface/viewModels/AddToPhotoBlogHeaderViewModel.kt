package com.topface.topface.viewModels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.ObservableField
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.BalanceData
import com.topface.topface.data.Photos
import com.topface.topface.databinding.AddToPhotoBlogHeaderLayoutBinding
import com.topface.topface.requests.AddPhotoFeedRequest
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.handlers.ApiHandler
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.adapters.LeadersRecyclerViewAdapter
import com.topface.topface.utils.FlurryManager
import com.topface.topface.utils.FlurryManager.GET_LEAD
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.extensions.getFakePhotosCount
import com.topface.topface.utils.rx.shortSubscription
import rx.subscriptions.CompositeSubscription


/**
 * Медель для хедера постановки в фотоблог
 * Created by tiberal on 25.07.16.
 */
class AddToPhotoBlogHeaderViewModel(binding: AddToPhotoBlogHeaderLayoutBinding, var mLockerVisualisator: ILockerVisualisator?
                                    , var mActivityDelegate: IActivityDelegate?, var mPurchasesVisualisator: IPurchasesFragmentVisualisator?
                                    , var mPhotoHelperVisualisator: IPhotoHelperVisualisator?, var mAdapterInteractor: IAdapterInteractor?) :
        BaseViewModel<AddToPhotoBlogHeaderLayoutBinding>(binding), View.OnClickListener {

    private val mAppState: TopfaceAppState by lazy {
        App.getAppComponent().appState()
    }
    private val mSubscriptions = CompositeSubscription()
    lateinit private var mBalance: BalanceData
    val inputText = ObservableField<String>()

    private companion object {
        const val MAX_SYMBOL_COUNT = 120
    }

    init {
        mSubscriptions.add(mAppState.getObservable(BalanceData::class.java).subscribe(shortSubscription {
            it?.let {
                mBalance = it
            }
        }))
        addButtons(binding.buttonsContainer)
    }

    fun addButtons(container: LinearLayout) {
        val buttonsLayout = container
        buttonsLayout.removeAllViews()
        val buttons = App.get().options.buyLeaderButtons
        buttons.indices.forEach {
            (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                    .inflate(R.layout.add_leader_button, buttonsLayout)
            val buttonCurrent = buttonsLayout.getChildAt(buttonsLayout.childCount - 1) as Button
            buttonCurrent.text = buttons[it].title
            buttonCurrent.tag = it
            buttonCurrent.setOnClickListener(this)
        }
    }

    fun getMaxLength() = MAX_SYMBOL_COUNT

    fun getDescription() = R.string.add_photo_feed_header_description

    fun getHintMessage() = R.string.add_leader_hint

    override fun onClick(v: View?) {
        v?.let {
            val tag = v.tag
            if (tag is Int) {
                pressedAddToLeader(tag)
            }
        }
    }

    private fun pressedAddToLeader(position: Int) {
        val buttonData = App.get().options.buyLeaderButtons[position]
        val selectedPhotoId = mAdapterInteractor?.getSelectedPhotoId() ?: 0
        if (mAdapterInteractor?.getItemCount() ?: 0 > mAdapterInteractor?.getAdapterData()?.getFakePhotosCount() ?: 0) {
            if (mBalance.money < buttonData.price) {
                mPurchasesVisualisator?.showPurchasesFragment(buttonData.price)
            } else if (selectedPhotoId > LeadersRecyclerViewAdapter.EMPTY_SELECTED_POS) {
                mLockerVisualisator?.showLocker()
                AddPhotoFeedRequest(selectedPhotoId, context, buttonData.photoCount, inputText.get()
                        , buttonData.price.toLong()).callback(object : ApiHandler() {

                    override fun success(response: IApiResponse) {
                        FlurryManager.getInstance().sendSpendCoinsEvent(buttonData.price, GET_LEAD)
                        mActivityDelegate?.let {
                            it.setResult(Activity.RESULT_OK, Intent())
                            it.finish()
                        }
                    }

                    override fun fail(codeError: Int, response: IApiResponse) {
                        mLockerVisualisator?.hideLocker()
                        if (codeError == ErrorCodes.PAYMENT) {
                            mPurchasesVisualisator?.showPurchasesFragment(buttonData.price)
                        } else {
                            Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }).exec()
            } else {
                Toast.makeText(App.getContext(), R.string.leaders_need_photo, Toast.LENGTH_SHORT).show()
            }
        } else {
            mPhotoHelperVisualisator?.showPhotoHelper()
        }
    }

    override fun release() {
        super.release()
        mLockerVisualisator = null
        mActivityDelegate = null
        mPurchasesVisualisator = null
        mPhotoHelperVisualisator = null
        mAdapterInteractor = null
    }

    interface ILockerVisualisator {
        fun showLocker()
        fun hideLocker()
    }

    interface IPurchasesFragmentVisualisator {
        fun showPurchasesFragment(price: Int)
    }

    interface IPhotoHelperVisualisator {
        fun showPhotoHelper()
    }

    interface IAdapterInteractor {
        fun getSelectedPhotoId(): Int
        fun getItemCount(): Int
        fun getAdapterData(): Photos
    }

}
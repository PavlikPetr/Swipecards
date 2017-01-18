package com.topface.topface.ui.add_to_photo_blog

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.topface.statistics.generated.NewProductsKeysGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.BalanceData
import com.topface.topface.databinding.AddToPhotoBlogRedesignLayoutBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.requests.AddPhotoFeedRequest
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.handlers.ApiHandler
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.state.EventBus
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.add_to_photo_blog.adapter_components.HeaderComponent
import com.topface.topface.ui.add_to_photo_blog.adapter_components.PhotoListComponent
import com.topface.topface.ui.add_to_photo_blog.adapter_components.PlaceButtonComponent
import com.topface.topface.ui.fragments.TrackedLifeCycleActivity
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.views.toolbar.view_models.BackToolbarViewModel
import com.topface.topface.utils.FlurryManager
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * Experimental redesign of add-to-photo-blog screen
 * Created by mbayutin on 10.01.17.
 */

class AddToPhotoBlogRedesignActivity : TrackedLifeCycleActivity<AddToPhotoBlogRedesignLayoutBinding>() {
    private companion object {
        val SELECTED_PHOTO_ID = "selected_photo_id"
    }
    @Inject lateinit internal var mAppState: TopfaceAppState
    @Inject lateinit var mEventBus: EventBus
    private lateinit var mPhotoSelectedSubscription: Subscription
    private lateinit var mPlaceButtonTapSubscription: Subscription
    lateinit private var mBalance: BalanceData
    private val mSubscriptions = CompositeSubscription()
    // договаривались использовать цену из первой кнопки лидеров
    private val mPrice : Int by lazy { App.get().options.buyLeaderButtons[0].price }

    private val mNavigator by lazy { FeedNavigator(this) }

    private var mLastSelectedPhotoId = 0

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(TypeProvider()) { Bundle() }
                .addAdapterComponent(HeaderComponent())
                .addAdapterComponent(PhotoListComponent(this@AddToPhotoBlogRedesignActivity))
                .addAdapterComponent(PlaceButtonComponent())
    }

    override fun getToolbarBinding(binding: AddToPhotoBlogRedesignLayoutBinding) = binding.toolbarInclude

    override fun getLayout() = R.layout.add_to_photo_blog_redesign_layout

    override fun generateToolbarViewModel(toolbar: ToolbarBinding) =
            BackToolbarViewModel(toolbar,
                    getString(R.string.add_to_photo_blog_title), this@AddToPhotoBlogRedesignActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
        mPhotoSelectedSubscription = mEventBus.getObservable(PhotoSelectedEvent::class.java)
                .subscribe { event ->
                    mLastSelectedPhotoId = event.id
                }
        mPlaceButtonTapSubscription = mEventBus.getObservable(PlaceButtonTapEvent::class.java)
                .subscribe { event ->
                    placeOrBuy()
                }
        mSubscriptions.add(mAppState.getObservable(BalanceData::class.java).subscribe(object : RxUtils.ShortSubscription<BalanceData>() {
            override fun onNext(balance: BalanceData?) = balance?.let {
                mBalance = it
            } ?: Unit
        }))

        NewProductsKeysGeneratedStatistics.sendNow_PHOTOFEED_SEND_OPEN(applicationContext)
        val lastSelectedPhotoId = onRestoreState(savedInstanceState)
        initRecyclerView(viewBinding.content, lastSelectedPhotoId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdapter.releaseComponents()
        mPhotoSelectedSubscription.safeUnsubscribe()
        mPlaceButtonTapSubscription.safeUnsubscribe()
        mSubscriptions.safeUnsubscribe()
    }

    override fun onUpClick() {
        finish()
    }

    private fun initRecyclerView(recyclerView: RecyclerView, lastSelectedPhotoId: Int) {
        with(recyclerView) {
            layoutManager = LinearLayoutManager(this@AddToPhotoBlogRedesignActivity)
            adapter = mAdapter
            post {
                mAdapter.data = mutableListOf(HeaderItem(), PhotoListItem(lastSelectedPhotoId), PlaceButtonItem(mPrice))
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_PHOTO_ID, mLastSelectedPhotoId)
    }

    fun onRestoreState(savedInstanceState: Bundle?): Int {
        var lastSelectedPhotoId = App.get().profile.photos[0].id
        if (savedInstanceState != null) {
            val storedId = savedInstanceState.getInt(SELECTED_PHOTO_ID)
            if (storedId != 0) lastSelectedPhotoId = storedId
        }
        return lastSelectedPhotoId
    }

    private fun placeOrBuy() {
        if (mBalance.money < mPrice) {
            startPurchasesActivity()
        } else {
            viewBinding.locker.setVisibility(View.VISIBLE)
            AddPhotoFeedRequest(mLastSelectedPhotoId, this, 1, ""
                    , mPrice.toLong()).callback(object : ApiHandler() {

                override fun success(response: IApiResponse) {
                    FlurryManager.getInstance().sendSpendCoinsEvent(mPrice, FlurryManager.GET_LEAD)
                    setResult(Activity.RESULT_OK)
                    finish()
                }

                override fun fail(codeError: Int, response: IApiResponse) {
                    viewBinding.locker.setVisibility(View.GONE)
                    if (codeError == ErrorCodes.PAYMENT) {
                        startPurchasesActivity()
                    } else {
                        Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }).exec()
        }
    }

    private fun startPurchasesActivity() {
        mNavigator.showPurchaseCoins()
    }
}
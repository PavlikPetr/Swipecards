package com.topface.topface.ui.add_to_photo_blog

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.topface.statistics.generated.NewProductsKeysGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.AddToPhotoBlogRedesignLayoutBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.requests.ApiRequest
import com.topface.topface.ui.add_to_photo_blog.adapter_components.HeaderComponent
import com.topface.topface.ui.add_to_photo_blog.adapter_components.PhotoListComponent
import com.topface.topface.ui.add_to_photo_blog.adapter_components.PlaceButtonComponent
import com.topface.topface.ui.fragments.TrackedLifeCycleActivity
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.views.toolbar.view_models.BackToolbarViewModel
import com.topface.topface.utils.extensions.photosForPhotoBlog
import com.topface.topface.utils.http.IRequestClient

/**
 * Experimental redesign of add-to-photo-blog screen
 * Created by mbayutin on 10.01.17.
 */

class AddToPhotoBlogRedesignActivity : TrackedLifeCycleActivity<AddToPhotoBlogRedesignLayoutBinding>(), IRequestClient {
    private companion object {
        const val SELECTED_PHOTO_ID = "selected_photo_id"
    }

    private val mRequests = mutableListOf<ApiRequest>()

    private val mFeedNavigator by lazy { FeedNavigator(this) }
    private val mViewModel by lazy { AddToPhotoBlogRedesignActivityViewModel(this, mFeedNavigator) }
    private val mFeedRequestFactory by lazy {
        FeedRequestFactory(this)
    }
    private val mApi by lazy {
        FeedApi(this, this, mFeedRequestFactory = mFeedRequestFactory)
    }

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(TypeProvider()) { Bundle() }
                .addAdapterComponent(HeaderComponent(mViewModel.lastSelectedPhotoId))
                .addAdapterComponent(PhotoListComponent(mViewModel.lastSelectedPhotoId, mApi))
                .addAdapterComponent(PlaceButtonComponent(mViewModel.lastSelectedPhotoId))
    }

    override fun getToolbarBinding(binding: AddToPhotoBlogRedesignLayoutBinding) = binding.toolbarInclude

    override fun getLayout() = R.layout.add_to_photo_blog_redesign_layout

    override fun generateToolbarViewModel(toolbar: ToolbarBinding) =
            BackToolbarViewModel(toolbar,
                    getString(R.string.add_to_photo_blog_title), this@AddToPhotoBlogRedesignActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding.viewModel = mViewModel
        NewProductsKeysGeneratedStatistics.sendNow_PHOTOFEED_SEND_OPEN(applicationContext)
        initRecyclerView(viewBinding.content,
                mViewModel.price
        )
        restoreLastSelectedPhotoId(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdapter.releaseComponents()
        mViewModel.release()
        removeAllRequests()
    }

    override fun onUpClick() = finish()

    private fun initRecyclerView(recyclerView: RecyclerView, price: Int) =
        with(recyclerView) {
            layoutManager = LinearLayoutManager(this@AddToPhotoBlogRedesignActivity)
            adapter = mAdapter
            post {
                mAdapter.data = mutableListOf(HeaderItem(), PhotoListItem(), PlaceButtonItem(price))
            }
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_PHOTO_ID, mViewModel.lastSelectedPhotoId.get())
    }

    override fun registerRequest(request: ApiRequest?) {
        request?.let {
            if (!mRequests.contains(it)) mRequests.add(it)
        }
    }

    override fun cancelRequest(request: ApiRequest?) {
        request?.cancelFromUi()
    }

    private fun removeAllRequests() {
        if (mRequests.size > 0) {
            mRequests.forEach { cancelRequest(it) }
            mRequests.clear()
        }
    }

    fun restoreLastSelectedPhotoId(savedInstanceState: Bundle?) {
        var lastSelectedPhotoId = App.get().profile.photos.photosForPhotoBlog().first?.id ?: 0
        if (savedInstanceState != null) {
            val storedId = savedInstanceState.getInt(SELECTED_PHOTO_ID)
            if (storedId != 0) lastSelectedPhotoId = storedId
        }
        mViewModel.lastSelectedPhotoId.set(lastSelectedPhotoId)
    }
}
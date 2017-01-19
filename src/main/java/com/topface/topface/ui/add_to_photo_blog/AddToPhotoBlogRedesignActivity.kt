package com.topface.topface.ui.add_to_photo_blog

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.topface.statistics.generated.NewProductsKeysGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.AddToPhotoBlogRedesignLayoutBinding
import com.topface.topface.databinding.ToolbarBinding
import com.topface.topface.ui.add_to_photo_blog.adapter_components.HeaderComponent
import com.topface.topface.ui.add_to_photo_blog.adapter_components.PhotoListComponent
import com.topface.topface.ui.add_to_photo_blog.adapter_components.PlaceButtonComponent
import com.topface.topface.ui.fragments.TrackedLifeCycleActivity
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.views.toolbar.view_models.BackToolbarViewModel

/**
 * Experimental redesign of add-to-photo-blog screen
 * Created by mbayutin on 10.01.17.
 */

class AddToPhotoBlogRedesignActivity : TrackedLifeCycleActivity<AddToPhotoBlogRedesignLayoutBinding>(),
        AddToPhotoBlogRedesignActivityViewModel.IActivityDelegateWithFeedNavigator {
    private companion object {
        const val SELECTED_PHOTO_ID = "selected_photo_id"
    }
    private lateinit var mViewModel: AddToPhotoBlogRedesignActivityViewModel
    private val mNavigator by lazy { FeedNavigator(this) }

    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(TypeProvider()) { Bundle() }
                .addAdapterComponent(HeaderComponent())
                .addAdapterComponent(PhotoListComponent())
                .addAdapterComponent(PlaceButtonComponent())
    }

    override fun getToolbarBinding(binding: AddToPhotoBlogRedesignLayoutBinding) = binding.toolbarInclude

    override fun getLayout() = R.layout.add_to_photo_blog_redesign_layout

    override fun generateToolbarViewModel(toolbar: ToolbarBinding) =
            BackToolbarViewModel(toolbar,
                    getString(R.string.add_to_photo_blog_title), this@AddToPhotoBlogRedesignActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = AddToPhotoBlogRedesignActivityViewModel(this)
        viewBinding.viewModel = mViewModel
        NewProductsKeysGeneratedStatistics.sendNow_PHOTOFEED_SEND_OPEN(applicationContext)
        initRecyclerView(viewBinding.content,
                restoreLastSelectedPhotoId(savedInstanceState),
                mViewModel.price
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdapter.releaseComponents()
        mViewModel.release()
    }

    override fun onUpClick() = finish()

    private fun initRecyclerView(recyclerView: RecyclerView, lastSelectedPhotoId: Int, price: Int) =
        with(recyclerView) {
            layoutManager = LinearLayoutManager(this@AddToPhotoBlogRedesignActivity)
            adapter = mAdapter
            post {
                mAdapter.data = mutableListOf(HeaderItem(), PhotoListItem(lastSelectedPhotoId), PlaceButtonItem(price))
            }
        }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SELECTED_PHOTO_ID, mViewModel.lastSelectedPhotoId)
    }

    fun restoreLastSelectedPhotoId(savedInstanceState: Bundle?): Int {
        var lastSelectedPhotoId = App.get().profile.photos.first.id
        if (savedInstanceState != null) {
            val storedId = savedInstanceState.getInt(SELECTED_PHOTO_ID)
            if (storedId != 0) lastSelectedPhotoId = storedId
        }
        return lastSelectedPhotoId
    }

    override fun getFeedNavigator(): IFeedNavigator { return mNavigator }

}
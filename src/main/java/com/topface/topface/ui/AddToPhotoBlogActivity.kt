package com.topface.topface.ui

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.Photos
import com.topface.topface.databinding.AddToPhotoBlogHeaderLayoutBinding
import com.topface.topface.databinding.AddToPhotoBlogLayoutBinding
import com.topface.topface.statistics.TakePhotoStatistics
import com.topface.topface.ui.adapters.LeadersRecyclerViewAdapter
import com.topface.topface.ui.adapters.LoadingListAdapter
import com.topface.topface.ui.analytics.TrackedFragmentActivity
import com.topface.topface.ui.dialogs.TakePhotoPopup
import com.topface.topface.ui.fragments.PurchasesFragment
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.Utils
import com.topface.topface.utils.actionbar.ActionBarView
import com.topface.topface.utils.extensions.photosForPhotoBlog
import com.topface.topface.viewModels.AddToPhotoBlogHeaderViewModel
import com.topface.topface.viewModels.AddToPhotoBlogViewModel

/**
 * Активити постановки в фотоблог
 * Created by tiberal on 25.07.16.
 */

class AddToPhotoBlogActivity : TrackedFragmentActivity(), AddToPhotoBlogHeaderViewModel.ILockerVisualisator
        , AddToPhotoBlogHeaderViewModel.IPurchasesFragmentVisualisator
        , AddToPhotoBlogHeaderViewModel.IPhotoHelperVisualisator, AddToPhotoBlogHeaderViewModel.IAdapterInteractor {

    private var mIsPhotoDialogShown: Boolean = false
    lateinit private var mHeaderViewModel: AddToPhotoBlogHeaderViewModel
    lateinit private var mHeaderBinding: AddToPhotoBlogHeaderLayoutBinding
    private val mScreenBinding by lazy {
        DataBindingUtil.setContentView<AddToPhotoBlogLayoutBinding>(this, R.layout.add_to_photo_blog_layout)
    }
    private val mScreenViewModel by lazy { AddToPhotoBlogViewModel(mScreenBinding, AddPhotoHelper(this)) }
    private val mAdapter by lazy {
        val profile = App.get().profile
        (LeadersRecyclerViewAdapter(
                profile.photos.photosForPhotoBlog(),
                profile.photosCount, LoadingListAdapter.Updater {
            mScreenViewModel.sendAlbumRequest()
        })
                .setFooter(createFooterView(), false)
                .setHeader(createHeaderView(), false)) as LeadersRecyclerViewAdapter
    }
    private var mSelectedPos: Int = 1

    private companion object {
        val SELECTED_POSITION = "selected_position"
        val SELECTED_PHOTO_ID = "selected_photo_id"
        val ALREADY_SHOWN = "already_shown"
        val PAGE_NAME = "adtoleader"
        val GREETING_TEXT = "greeting_text"
    }

    override fun showLocker() = mScreenBinding.locker.setVisibility(View.VISIBLE)

    override fun hideLocker() = mScreenBinding.locker.setVisibility(View.GONE)

    override fun showPurchasesFragment(price: Int) {
        Debug.error("money price " + price)
        startActivity(PurchasesActivity.createBuyingIntent(this.localClassName,
                PurchasesFragment.TYPE_LEADERS, price, App.get().options.topfaceOfferwallRedirect))
    }

    private fun showPhotoHelper() {
        showPhotoHelper(!mIsPhotoDialogShown)
    }

    override fun showPhotoHelper(isNeedShow: Boolean) {
        if (isNeedShow) {
            if (!App.getConfig().userConfig.isUserAvatarAvailable && App.get().profile.photo == null) {
                TakePhotoPopup.newInstance(TakePhotoStatistics.PLC_ADD_TO_LEADER).show(supportFragmentManager, TakePhotoPopup.TAG)
                mIsPhotoDialogShown = true
            }
        }
    }

    override fun getSelectedPhotoId() = mAdapter.selectedPhotoId

    override fun getItemCount() = mAdapter.itemCount

    override fun getAdapterData() = mAdapter.adapterData ?: Photos()

    private fun createFooterView() = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.gridview_footer_progress_bar, null, false)

    private fun createHeaderView(): View {
        mHeaderBinding = DataBindingUtil.inflate((getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                , R.layout.add_to_photo_blog_header_layout, null, false)
        mHeaderViewModel = AddToPhotoBlogHeaderViewModel(mHeaderBinding, this, this, this, this, this)
        mHeaderBinding.handlers = mHeaderViewModel
        return mHeaderBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            onRestoreState(it)
        }
        initActionBar(supportActionBar)
        //https://youtrack.jetbrains.com/issue/KT-12402
        mScreenBinding.handlers = mScreenViewModel
        initRecyclerView(mScreenBinding.userPhotosGrid)
    }

    fun initActionBar(actionBar: ActionBar?) {
        actionBar?.let {
            ActionBarView(it, this).setArrowUpView(getString(R.string.publish_photo)) { finish() }
            with(it) {
                setIcon(android.R.color.transparent)
                setDisplayHomeAsUpEnabled(false)
                setDisplayUseLogoEnabled(true)
                setDisplayShowCustomEnabled(true)
                setDisplayShowTitleEnabled(false)
            }
        }
    }

    private fun initRecyclerView(recyclerView: RecyclerView) {
        val manager = StaggeredGridLayoutManager(resources.getInteger(R.integer.add_to_leader_column_count)
                , StaggeredGridLayoutManager.VERTICAL)
        with(recyclerView) {
            layoutManager = manager
            adapter = mAdapter
            post {
                mAdapter.selectedPhotoPos = mSelectedPos
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        with(outState) {
            putInt(SELECTED_PHOTO_ID, mAdapter.selectedPhotoId)
            putInt(SELECTED_POSITION, mAdapter.selectedPhotoPos)
            putBoolean(ALREADY_SHOWN, mIsPhotoDialogShown)
            putString(GREETING_TEXT, mHeaderViewModel.inputText.get() ?: Utils.EMPTY)
        }
    }


    fun onRestoreState(savedInstanceState: Bundle) {
        mAdapter.selectedPhotoId = savedInstanceState.getInt(SELECTED_PHOTO_ID, 0)
        mScreenBinding.userPhotosGrid.post {
            mSelectedPos = savedInstanceState.getInt(SELECTED_POSITION, 1)
        }
        mIsPhotoDialogShown = savedInstanceState.getBoolean(ALREADY_SHOWN)
        mHeaderViewModel.inputText.set(savedInstanceState.getString(GREETING_TEXT, Utils.EMPTY))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mScreenViewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPause() {
        super.onPause()
        mIsPhotoDialogShown = true
    }

    override fun onStop() {
        super.onStop()
        mScreenViewModel.release()
        mHeaderViewModel.release()
    }

    override fun onResume() {
        super.onResume()
        showPhotoHelper()
    }

}

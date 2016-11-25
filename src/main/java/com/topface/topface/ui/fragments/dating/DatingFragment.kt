package com.topface.topface.ui.fragments.dating

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.SearchUser
import com.topface.topface.databinding.DatingAlbumLayoutBinding
import com.topface.topface.databinding.DatingButtonsLayoutBinding
import com.topface.topface.databinding.FragmentDatingLayoutBinding
import com.topface.topface.ui.GiftsActivity
import com.topface.topface.ui.edit.EditContainerActivity
import com.topface.topface.ui.fragments.ToolbarActivity
import com.topface.topface.ui.fragments.dating.admiration_purchase_popup.IStartAdmirationPurchasePopup
import com.topface.topface.ui.fragments.dating.form.ChildItemDelegate
import com.topface.topface.ui.fragments.dating.form.GiftsItemDelegate
import com.topface.topface.ui.fragments.dating.form.ParentItemDelegate
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.toolbar.PrimalCollapseFragment
import com.topface.topface.ui.new_adapter.CompositeAdapter
import com.topface.topface.ui.new_adapter.IType
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import com.topface.topface.ui.views.toolbar.view_models.NavigationToolbarViewModel
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.IStateSaverRegistrator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.support.v4.dimen

/**
 * Знакомства. Такие дела.
 * Created by tiberal on 07.10.16.
 */
class DatingFragment : PrimalCollapseFragment<DatingButtonsLayoutBinding, DatingAlbumLayoutBinding>()
        , DatingButtonsEventsDelegate, IDatingViewModelEvents, IDatingButtonsView, IEmptySearchVisibility,
        IStartAdmirationPurchasePopup, IDatingAlbumView {

    override val anchorViewResId: Int
        get() = R.layout.dating_buttons_layout
    override val collapseViewResId: Int
        get() = R.layout.dating_album_layout
    override val toolbarSize: Int
        get() = dimen(R.dimen.dating_album_height)

    private lateinit var mAddPhotoHelper: AddPhotoHelper

    private val mBinding by lazy {
        DataBindingUtil.inflate<FragmentDatingLayoutBinding>(context.layoutInflater, R.layout.fragment_dating_layout, null, false)
    }

    private lateinit var mUserSearchList: CachableSearchList<SearchUser>

    // ------------- этот блок нужно будет вынести в даггер2 --------------

    private val mDatingButtonsViewModel by lazy {
        DatingButtonsViewModel(mAnchorBinding, mApi, mNavigator, mUserSearchList, mDatingButtonsEvents = this,
                mDatingButtonsView = this, mEmptySearchVisibility = this, mStartAdmirationPurchasePopup = this)
    }
    private val mDatingAlbumViewModel by lazy {
        DatingAlbumViewModel(mCollapseBinding, mApi, mController, mUserSearchList, mNavigator, mAlbumActionsListener = this)
    }
    private val mDatingFragmentViewModel by lazy {
        DatingFragmentViewModel(mBinding, mApi, mUserSearchList, mDatingViewModelEvents = this,
                mDatingButtonsView = this, mEmptySearchVisibility = this)
    }
    private val mDatingOptionMenuManager by lazy {
        //todo срет логами, починить
        DatingOptionMenuManager(mNavigator)
    }

    private val mApi by lazy {
        FeedApi(context, this)
    }

    private val mNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private val mController by lazy {
        AlbumLoadController(AlbumLoadController.FOR_PREVIEW)
    }
    //~~~~~~~~~~~~~~~~~~~~~~~ конец ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    companion object {
        const val USER_SEARCH_LIST = "user_search_list"
    }

    override fun bindModels() {
        super.bindModels()
        mAnchorBinding.setModel(mDatingButtonsViewModel)
        mBinding.setModel(mDatingButtonsViewModel)
        mCollapseBinding.model = mDatingAlbumViewModel
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mAddPhotoHelper = AddPhotoHelper(this, null).apply {
            setOnResultHandler(object : Handler() {
                override fun handleMessage(msg: Message?) {
                    AddPhotoHelper.handlePhotoMessage(msg)
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (savedInstanceState != null) {
            mUserSearchList = savedInstanceState.getParcelableArrayList<Parcelable>(USER_SEARCH_LIST) as CachableSearchList<SearchUser>
        } else {
            mUserSearchList = CachableSearchList<SearchUser>(SearchUser::class.java)
        }
        super.onCreateView(inflater, container, savedInstanceState)
        val stateSaverRegistrator = activity
        if (stateSaverRegistrator is IStateSaverRegistrator) {
            stateSaverRegistrator.registerStateDelegate(mDatingAlbumViewModel, mDatingButtonsViewModel, mDatingFragmentViewModel)
        }
        initFormList()
        return mBinding.root
    }

    //todo доработать составной адаптер прокидывать в делегат изменения стейта
    val govnocod by lazy {
        GiftsItemDelegate(mApi, mNavigator)
    }
    val govnocod1 by lazy {
        ChildItemDelegate(mApi)
    }

    private fun initFormList() = with(mBinding.formsList) {
        layoutManager = LinearLayoutManager(context)
        adapter = CompositeAdapter<IType>().apply {
            addAdapterItemDelegate(ChildItemDelegate.TYPE, govnocod1)
            addAdapterItemDelegate(ParentItemDelegate.TYPE, ParentItemDelegate())
            addAdapterItemDelegate(GiftsItemDelegate.TYPE, govnocod)
        }
        addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                if (layoutManager.getPosition(view) == 0) {
                    outRect?.top = dimen(R.dimen.form_list_padding)
                } else {
                    outRect?.top = 0
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelableArrayList(USER_SEARCH_LIST, mUserSearchList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDatingButtonsViewModel.release()
        mDatingFragmentViewModel.release()
        govnocod.onDestroyView()
        mAddPhotoHelper.releaseHelper()
    }

    override fun onDetach() {
        super.onDetach()
        val stateSaverRegistrator = activity
        if (stateSaverRegistrator is IStateSaverRegistrator) {
            stateSaverRegistrator.unregisterStateDelegate(mDatingAlbumViewModel, mDatingButtonsViewModel, mDatingFragmentViewModel)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mDatingFragmentViewModel.onActivityResult(requestCode, resultCode, data)
        mDatingAlbumViewModel.onActivityResult(requestCode, resultCode, data)
        govnocod.onActivityResult(requestCode, resultCode, data)
        mDatingButtonsViewModel.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == EditContainerActivity.INTENT_EDIT_FILTER ||
                resultCode == Activity.RESULT_OK && requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
            mDatingFragmentViewModel.onActivityResult(requestCode, resultCode, data)
        } else {
            mAddPhotoHelper.processActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDataReceived(user: SearchUser) {
        mDatingAlbumViewModel.albumData.set(user.photos)
        mCollapseBinding.datingAlbum?.let {
            mDatingButtonsViewModel.currentUser = user
            mDatingAlbumViewModel.currentUser = user
        }
    }

    override fun startAnimateAdmirationPurchasePopup(viewID: Int, @ColorInt fabColorResId: Int,
                                                     @DrawableRes fabIconResId: Int) =
            mNavigator.showAdmirationPurchasePopup(mDatingAlbumViewModel.currentUser,
                    if (viewID == DatingButtonsViewModel.BUTTON_FROM_ANCHOR) mAnchorBinding.sendAdmiration
                    else mBinding.sendAdmiration, activity, fabColorResId, fabIconResId)

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        mDatingOptionMenuManager.onCreateOptionsMenu(menu, inflater)
    }

    override fun showTakePhoto() = mNavigator.showTakePhotoPopup()

    override fun onNewSearchUser(user: SearchUser) {
        with(mDatingAlbumViewModel) {
            setUser(user)
        }
        with(mDatingFragmentViewModel) {
            currentUser = user
            prepareFormsData(user)
        }
    }

    override fun onResume() {
        super.onResume()
        Debug.log("GIFTS_BUGS dating resume current user id ${mDatingFragmentViewModel.currentUser?.id}")
        operateWithToolbar({ this.isCollapsStyle.set(true) })
        updateToolbar(mDatingAlbumViewModel.currentUser)
    }

    override fun onDestroy() {
        super.onDestroy()
        operateWithToolbar({ this.isCollapsStyle.set(false) })
    }

    override fun onUserShow(user: SearchUser) = updateToolbar(user)

    private fun updateToolbar(user: SearchUser?) =
            ToolbarManager.setToolbarSettings(ToolbarSettingsData(title = user?.nameAndAge ?: Utils.EMPTY,
                    isOnline = user?.online ?: false))

    override fun onOptionsItemSelected(item: MenuItem?) =
            mDatingOptionMenuManager.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    override fun isScrimVisible(isVisible: Boolean) {
        mDatingOptionMenuManager.isScrimVisible(isVisible)
        mDatingButtonsViewModel.isScrimVisible(isVisible)
        operateWithToolbar({ this.isScrimVisible(isVisible) })
    }

    override fun isCollapsed(isCollapsed: Boolean) {
        mDatingButtonsViewModel.isCollapsed(isCollapsed)
        operateWithToolbar({ this.isCollapsed(isCollapsed) })
    }

    private fun operateWithToolbar(block: NavigationToolbarViewModel.() -> Unit) {
        (activity as? ToolbarActivity<*>)?.let {
            (it.getToolbarViewModel() as? NavigationToolbarViewModel)?.run(block)
        }
    }

    override fun showControls() = mDatingButtonsViewModel.isDatingButtonsVisible.set(View.VISIBLE)

    override fun hideControls() = mDatingButtonsViewModel.isDatingButtonsVisible.set(View.INVISIBLE)

    override fun lockControls() = mDatingButtonsViewModel.isDatingButtonsLocked.set(false)

    override fun unlockControls() = mDatingButtonsViewModel.isDatingButtonsLocked.set(true)

    override fun showEmptySearchDialog() = mNavigator.showEmptyDating()

    override fun hideEmptySearchDialog() = mNavigator.closeEmptyDating()
}
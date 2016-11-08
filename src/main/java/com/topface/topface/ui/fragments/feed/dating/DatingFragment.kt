package com.topface.topface.ui.fragments.feed.dating

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Parcelable
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.SearchUser
import com.topface.topface.databinding.DatingAlbumLayoutBinding
import com.topface.topface.databinding.DatingButtonsLayoutBinding
import com.topface.topface.databinding.FragmentDatingLayoutBinding
import com.topface.topface.ui.GiftsActivity
import com.topface.topface.ui.edit.EditContainerActivity
import com.topface.topface.ui.fragments.feed.dating.admiration_purchase_popup.AdmirationPurchasePopupActivity
import com.topface.topface.ui.fragments.feed.dating.admiration_purchase_popup.FabTransform
import com.topface.topface.ui.fragments.feed.dating.admiration_purchase_popup.IAnimateAdmirationPurchasePopup
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.toolbar.PrimalCollapseFragment
import com.topface.topface.utils.*
import com.topface.topface.utils.config.UserConfig
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.support.v4.dimen
import org.jetbrains.anko.wrapContent

/**
 * Знакомства. Такие дела.
 * Created by tiberal on 07.10.16.
 */
class DatingFragment : PrimalCollapseFragment<DatingButtonsLayoutBinding, DatingAlbumLayoutBinding>()
        , DatingButtonsEventsDelegate, IDatingViewModelEvents, IDatingButtonsView, IEmptySearchVisibility, IAnimateAdmirationPurchasePopup {

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
                mDatingButtonsView = this, mEmptySearchVisibility = this, mAnimateAdmirationPurchasePopup = this)
    }
    private val mDatingAlbumViewModel by lazy {
        DatingAlbumViewModel(mCollapseBinding, mApi, mController, mUserSearchList)
    }
    private val mDatingFragmentViewModel by lazy {
        DatingFragmentViewModel(mBinding, mApi, mNavigator, mUserSearchList, mDatingViewModelEvents = this,
                mDatingButtonsView = this, mEmptySearchVisibility = this)
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
        mAnchorBinding.model = mDatingButtonsViewModel
        mBinding.model = mDatingFragmentViewModel
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
        return mBinding.root
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelableArrayList(USER_SEARCH_LIST, mUserSearchList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mDatingButtonsViewModel.release()
        mDatingFragmentViewModel.release()
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
        if (resultCode == AdmirationPurchasePopupActivity.RESULT_CODE_BOUGHT_VIP ||
                resultCode == AdmirationPurchasePopupActivity.RESULT_CODE_BOUGHT_COINS) {
            mDatingButtonsViewModel.wrapperSendAdmiration()
        }

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

    override fun startAnimateAdmirationPurchasePopup(transitionView: View) {
        val intent = Intent(activity, AdmirationPurchasePopupActivity::class.java)
        intent.putExtra(AdmirationPurchasePopupActivity.CURRENT_USER, mDatingFragmentViewModel.getCurrentUser())
        if (Utils.isLollipop()) {
            FabTransform.addExtras(intent, resources.getColor(R.color.dating_fab_small), R.drawable.admiration)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView,
                    getString(R.string.transition_name_admiration_popup))
            startActivityForResult(intent, 0, options.toBundle())
        } else {
            startActivityForResult(intent, 0)
        }
    }

    override fun showTakePhoto() = mNavigator.showTakePhotoPopup()

    override fun onNewSearchUser(user: SearchUser) {
        with(mDatingAlbumViewModel) {
            albumData.set(user.photos)
            currentUser = user
        }
        mDatingButtonsViewModel.currentUser = user
    }

    override fun showControls() = mDatingButtonsViewModel.isDatingButtonsVisible.set(View.VISIBLE)

    override fun hideControls() = mDatingButtonsViewModel.isDatingButtonsVisible.set(View.INVISIBLE)

    override fun lockControls() = mDatingButtonsViewModel.isDatingButtonsLocked.set(false)

    override fun unlockControls() = mDatingButtonsViewModel.isDatingButtonsLocked.set(true)

    override fun showEmptySearchDialog() {
    }

    override fun hideEmptySearchDialog() {
    }
}

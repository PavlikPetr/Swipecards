package com.topface.topface.ui.fragments.dating.dating_redesign

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import com.topface.topface.R
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.SearchUser
import com.topface.topface.databinding.AcNewNavigationBinding
import com.topface.topface.databinding.DatingReredesignBinding
import com.topface.topface.ui.GiftsActivity
import com.topface.topface.ui.NavigationActivity
import com.topface.topface.ui.edit.EditContainerActivity
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.dating.IEmptySearchVisibility
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.IStateSaverRegistrator
import com.topface.topface.utils.LocaleConfig
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import org.jetbrains.anko.layoutInflater

/**
 * Редизайн знакомств
 * Created by tiberal on 07.10.16.
 */
class DatingFragment : BaseFragment(), IEmptySearchVisibility {

    private val mBinding by lazy {
        DataBindingUtil.inflate<DatingReredesignBinding>(context.layoutInflater, R.layout.dating_reredesign, null, false)
    }

    private val mViewModel by lazy {
        DatingFragmentViewModel(context, mNavigator, mApi, this, mController, mUserSearchList)
    }

    private val mApi by lazy {
        FeedApi(context, this)
    }

    private val mController by lazy {
        AlbumLoadController(AlbumLoadController.FOR_PREVIEW)
    }

    private val mDatingOptionMenuManager by lazy {
        DatingOptionMenuManager(mNavigator)
    }

    private val mNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private lateinit var mAddPhotoHelper: AddPhotoHelper
    private val mUserSearchList: CachableSearchList<SearchUser> = CachableSearchList<SearchUser>(SearchUser::class.java)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        (activity as? IStateSaverRegistrator)?.apply { registerLifeCycleDelegate(mViewModel) }
        return mBinding.apply { viewModel = mViewModel }.root
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mViewModel.onActivityResult(requestCode, resultCode, data)
        mAddPhotoHelper.processActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mAddPhotoHelper.releaseHelper()
        mViewModel.release()
    }

    override fun onResume() {
        super.onResume()
        setFitSystemWindow(false)
    }

    override fun onPause() {
        super.onPause()
        setFitSystemWindow(true)
        if (LocaleConfig.localeChangeInitiated) {
            mUserSearchList.removeAllUsers()
            mUserSearchList.saveCache()
        } else {
            mUserSearchList.saveCache()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        mDatingOptionMenuManager.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?) =
            mDatingOptionMenuManager.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)

    private fun setFitSystemWindow(isNeedFit: Boolean) {
        ((activity as? NavigationActivity)?.viewBinding as? AcNewNavigationBinding)?.viewModel?.fitSystemWindow?.set(isNeedFit)
    }

    override fun showEmptySearchDialog() = mNavigator.showEmptyDating { mViewModel.update(false, false) }

    override fun hideEmptySearchDialog() = mNavigator.closeEmptyDating()
}
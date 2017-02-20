package com.topface.topface.ui.fragments.dating.dating_redesign

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import com.topface.topface.R
import com.topface.topface.data.search.CachableSearchList
import com.topface.topface.data.search.SearchUser
import com.topface.topface.databinding.DatingReredesignBinding
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.dating.IEmptySearchVisibility
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.IStateSaverRegistrator
import com.topface.topface.utils.LocaleConfig
import com.topface.topface.utils.extensions.getDrawable
import com.topface.topface.utils.extensions.loadBackground
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import org.jetbrains.anko.layoutInflater
import rx.Subscription

class DatingFragment : BaseFragment(), IEmptySearchVisibility {

    private var mLoadBackgroundSubscription: Subscription? = null

    private val mBinding by lazy {
        DataBindingUtil.inflate<DatingReredesignBinding>(context.layoutInflater, R.layout.dating_reredesign, null, false)
    }

    private val mViewModel by lazy {
        DatingFragmentViewModel(context, mNavigator, mApi, this, mController, mUserSearchList) {
            with(mBinding.albumRoot) {
                mLoadBackgroundSubscription.safeUnsubscribe()
                mLoadBackgroundSubscription = loadBackground(it)
                        .retry(2)
                        .applySchedulers()
                        .subscribe(shortSubscription {
                            TransitionDrawable(arrayOf((background as? TransitionDrawable)
                                    ?.getDrawable(1) ?: R.drawable.bg_blur.getDrawable() ?: it, it)).apply {
                                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                    @Suppress("DEPRECATION")
                                    setBackgroundDrawable(this)
                                } else {
                                    background = this
                                }
                            }.startTransition(300)
                        })
            }
        }
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

    override fun onDetach() {
        super.onDetach()
        val stateSaverRegistrator = activity
        if (stateSaverRegistrator is IStateSaverRegistrator) {
            stateSaverRegistrator.unregisterLifeCycleDelegate(mViewModel)
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
        mLoadBackgroundSubscription.safeUnsubscribe()
    }

    override fun onPause() {
        super.onPause()
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

    override fun showEmptySearchDialog() = mNavigator.showEmptyDating { mViewModel.update(false, false) }

    override fun hideEmptySearchDialog() = mNavigator.closeEmptyDating()
}
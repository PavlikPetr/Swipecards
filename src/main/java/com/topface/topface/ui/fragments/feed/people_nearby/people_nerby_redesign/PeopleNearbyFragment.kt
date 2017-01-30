package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.Manifest
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.PeopleNearbyFragmentLayoutBinding
import com.topface.topface.state.EventBus
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.fragments.BaseFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.PeopleNearbyFragment.Companion.PAGE_NAME
import com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign.people_nearby_adapter_components.*
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.extensions.getDimen
import com.topface.topface.utils.extensions.getPermissionStatus
import com.topface.topface.utils.extensions.isGrantedPermissions
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate
import org.jetbrains.anko.layoutInflater
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject


/**
 * Новый экран "Люди рядом", который содержит функционал фотоленты
 * Created by ppavlik on 10.01.17.
 */

@FlurryOpenEvent(name = PAGE_NAME)
@RuntimePermissions
class PeopleNearbyFragment : BaseFragment(), IPopoverControl, IViewSize {

    @Inject lateinit var mEventBus: EventBus

    companion object {
        const val PAGE_NAME = "PeopleNearby"
    }

    private val mAppbarLayoutParams: AppBarLayout.LayoutParams? by lazy {
        (activity.findViewById(R.id.collapsing_layout) as? CollapsingToolbarLayout)
                ?.layoutParams as? AppBarLayout.LayoutParams
    }
    private var mAppbarScrollFlags: Int = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
            AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS

    private lateinit var mPeopleNearbyPopover: PeopleNearbyPopover

    private val mPeopleNearbyListComponent by lazy { PeopleNearbyListComponent(context, mApi, mNavigator, this) }

    private val mFeedRequestFactory by lazy {
        FeedRequestFactory(context)
    }

    private lateinit var mPhotoHelper: AddPhotoHelper

    private val mApi by lazy {
        FeedApi(context, this, mFeedRequestFactory = mFeedRequestFactory)
    }
    private val mNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }
    private val mBinding by lazy {
        DataBindingUtil.inflate<PeopleNearbyFragmentLayoutBinding>(context.layoutInflater,
                R.layout.people_nearby_fragment_layout, null, false)
    }
    private val mPeopleNearbyTypeProvider by lazy {
        PeopleNearbyTypeProvider()
    }
    private val mAdapter: CompositeAdapter by lazy {
        CompositeAdapter(mPeopleNearbyTypeProvider) { Bundle() }
                .addAdapterComponent(PeopleNearbyPermissionsDeniedComponent {
                    PeopleNearbyFragmentPermissionsDispatcher.sendInitGeoEventWithCheck(this@PeopleNearbyFragment)
                })
                .addAdapterComponent(PeopleNearbyPermissionsNeverAskAgainComponent())
                .addAdapterComponent(PeopleNearbyLoaderComponent())
                .addAdapterComponent(activity.registerLifeCycleDelegate(PhotoBlogListComponent(context,
                        mApi, mNavigator, this, this)))
                .addAdapterComponent(mPeopleNearbyListComponent)
    }

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            AddPhotoHelper.handlePhotoMessage(msg)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mPhotoHelper = AddPhotoHelper(this@PeopleNearbyFragment, null).setOnResultHandler(mHandler)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mPhotoHelper.processActivityResult(requestCode, resultCode, data)
    }

    override fun show() {
        mPeopleNearbyPopover.show()
    }

    override fun closeByUser() {
        mPeopleNearbyPopover.closeByUser()
    }

    override fun closeProgrammatically() {
        mPeopleNearbyPopover.closeProgrammatically()
    }

    override fun size(size: Size) {
        mPeopleNearbyListComponent.size(size.apply {height = mBinding.list.measuredHeight - (R.dimen.photoblog_item_avatar_height.getDimen().toInt()
            +R.dimen.photoblog_item_margin_top.getDimen().toInt()
            +R.dimen.photoblog_item_margin_bottom.getDimen().toInt()
            +R.dimen.dialog_stroke_size.getDimen().toInt())
        }
        )

    }

    private val mViewModel by lazy {
        PeopleNearbyFragmentViewModel(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.get().inject(this)
        initList()
        mBinding.viewModel = mViewModel
        mPeopleNearbyPopover = PeopleNearbyPopover(context, mNavigator) { mBinding.root.findViewById(R.id.photoblogInGeoAvatar) }
        return mBinding.root
    }

    override fun onPause() {
        overrideScrollFlags()
        super.onPause()
    }

    override fun onResume() {
        overrideScrollFlags()
        super.onResume()
        ToolbarManager.setToolbarSettings(ToolbarSettingsData(getString(R.string.people_nearby)))
        if (context.isGrantedPermissions(listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))) {
            sendInitGeoEvent()
        } else {
            showPermissionsScreen()
        }
    }

    private fun overrideScrollFlags() {
        mAppbarLayoutParams?.apply {
            val currentFlags = scrollFlags
            scrollFlags = mAppbarScrollFlags
            mAppbarScrollFlags = currentFlags
        }
    }

    override fun onDetach() {
        super.onDetach()
        closeProgrammatically()
        activity.unregisterLifeCycleDelegate(mAdapter.components.values)
        activity.unregisterLifeCycleDelegate(mViewModel)
        mPeopleNearbyPopover.release()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        /**
         * https://code.google.com/p/android/issues/detail?id=78062
         * увидел, промониторь изменения и обнови дату/поправь
         * без изменений
         * 12.01.17
         */
        with(mBinding.refresh) {
            destroyDrawingCache()
            clearAnimation()
        }
        mViewModel.release()
        mAdapter.releaseComponents()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeProgrammatically()
        mPeopleNearbyPopover.release()
        mPhotoHelper.releaseHelper()
    }

    private fun initList() = with(mBinding.list) {
        layoutManager = NonScrolledLinearLayoutManager(context)
        adapter = mAdapter
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun sendInitGeoEvent() {
        mViewModel.geoPermissionsGranted()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PeopleNearbyFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
        App.getAppConfig().putPermissionsState(permissions, grantResults)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun showPermissionsScreen() {
        mViewModel.askGeoPermissions(activity.getPermissionStatus(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
}
package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.Manifest
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.leftMenu.DrawerLayoutStateData
import com.topface.topface.databinding.PeopleNearbyFragmentLayoutBinding
import com.topface.topface.state.DrawerLayoutState
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
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.extensions.getPermissionStatus
import com.topface.topface.utils.extensions.isGrantedPermissions
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import com.topface.topface.utils.unregisterLifeCycleDelegate
import org.jetbrains.anko.layoutInflater
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import rx.Subscription
import javax.inject.Inject


/**
 * Новый экран "Люди рядом", который содержит функционал фотоленты
 * Created by ppavlik on 10.01.17.
 */

@FlurryOpenEvent(name = PAGE_NAME)
@RuntimePermissions
class PeopleNearbyFragment : BaseFragment(), IPopoverControl, IViewSize {

    @Inject lateinit var mEventBus: EventBus
    @Inject lateinit var mDrawerLayoutState: DrawerLayoutState

    companion object {
        const val PAGE_NAME = "PeopleNearby"
    }

    private var mDrawerStateSubscription: Subscription? = null
    private val mPopupVindow: PopupWindow? by lazy {
        context.layoutInflater.inflate(R.layout.people_nearby_popover, null)?.let {
            PopupWindow(it, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                it.findViewById(R.id.close)?.setOnClickListener { dismiss() }
            }
        }
    }

    private val mPeopleNearbyListComponent by lazy { PeopleNearbyListComponent(context, mApi, mNavigator, this) }

    // показываем PopupWindow о том, что фотолента помогает популярности
    override fun show() {
        mPopupVindow?.showAsDropDown(mBinding.root.findViewById(R.id.photoblogInGeoAvatar))
    }

    // скрываем PopupWindow
    override fun close() {
        mPopupVindow?.dismiss()
    }

    private val mFeedRequestFactory by lazy {
        FeedRequestFactory(context)
    }

    private val mApi by lazy {
        FeedApi(context, this, mFeedRequestFactory = mFeedRequestFactory)
    }
    private val mNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }
    private val mBinding by lazy {
        DataBindingUtil.inflate<PeopleNearbyFragmentLayoutBinding>(context.layoutInflater, R.layout.people_nearby_fragment_layout, null, false)
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
                .addAdapterComponent(PhotoBlogListComponent(context, mApi, mNavigator, this, this))
                .addAdapterComponent(mPeopleNearbyListComponent)
    }

    override fun size(size: Size) {
        mPeopleNearbyListComponent.size(size.apply { height = mBinding.list.measuredHeight - height })
    }

    private val mViewModel by lazy {
        PeopleNearbyFragmentViewModel(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.get().inject(this)
        mDrawerStateSubscription = mDrawerLayoutState
                .observable
                .subscribe(shortSubscription {
                    if (it.state == DrawerLayoutStateData.SLIDE) {
                        close()
                    }
                })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initList()
        mBinding.viewModel = mViewModel
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        ToolbarManager.setToolbarSettings(ToolbarSettingsData(getString(R.string.people_nearby)))
        if (context.isGrantedPermissions(listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))) {
            sendInitGeoEvent()
        } else {
            showPermissionsScreen()
        }
    }

    override fun onDetach() {
        super.onDetach()
        close()
        activity.unregisterLifeCycleDelegate(mAdapter.components.values)
        activity.unregisterLifeCycleDelegate(mViewModel)
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
        close()
        mDrawerStateSubscription.safeUnsubscribe()
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
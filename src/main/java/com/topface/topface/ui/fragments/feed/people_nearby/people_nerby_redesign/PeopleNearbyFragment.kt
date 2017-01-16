package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.Manifest
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
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
import com.topface.topface.utils.IActivityDelegate
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
import android.support.v4.view.ViewCompat.setElevation
import android.os.Build.VERSION.SDK_INT
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Build
import android.widget.ImageButton
import android.widget.LinearLayout


/**
 * Новый экран "Люди рядом", который содержит функционал фотоленты
 * Created by ppavlik on 10.01.17.
 */

@FlurryOpenEvent(name = PAGE_NAME)
@RuntimePermissions
class PeopleNearbyFragment : BaseFragment(), IPopoverControl {

    override fun show() {
        val customView = (context.getSystemService(LAYOUT_INFLATER_SERVICE) as? LayoutInflater)?.inflate(R.layout.people_nearby_popover, null)
        val mPopupWindow = PopupWindow(
                customView,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )
        customView?.findViewById(R.id.root)?.setOnClickListener {
            mPopupWindow.dismiss()
        }
        mPopupWindow.showAtLocation(mBinding.root, Gravity.CENTER, 0, 0)
    }

    @Inject lateinit var mEventBus: EventBus

    companion object {
        const val PAGE_NAME = "PeopleNearby"
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
                .addAdapterComponent(PeopleNearbyEmptyListComponent())
                .addAdapterComponent(PeopleNearbyEmptyLocationComponent())
                .addAdapterComponent(PeopleNearbyPermissionsDeniedComponent {
                    PeopleNearbyFragmentPermissionsDispatcher.sendInitGeoEventWithCheck(this@PeopleNearbyFragment)
                })
                .addAdapterComponent(PeopleNearbyPermissionsNeverAskAgainComponent())
                .addAdapterComponent(PeopleNearbyLoaderComponent())
                .addAdapterComponent(activity.registerLifeCycleDelegate(PhotoBlogListComponent(context, mApi, mNavigator)))
    }
    private val mViewModel by lazy {
        PeopleNearbyFragmentViewModel(this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        App.get().inject(this)
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

    private fun initList() = with(mBinding.list) {
        layoutManager = LinearLayoutManager(context)
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
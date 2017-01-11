package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
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
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate
import org.jetbrains.anko.layoutInflater
import javax.inject.Inject

/**
 * Новый экран "Люди рядом", который содержит функционал фотоленты
 * Created by ppavlik on 10.01.17.
 */

@FlurryOpenEvent(name = PAGE_NAME)
class PeopleNearbyFragment : BaseFragment() {

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
    private val mBinding: PeopleNearbyFragmentLayoutBinding by lazy {
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
                    // TODO отправить ивент о запуске менеджера ГЕО
                })
                .addAdapterComponent(PeopleNearbyPermissionsNeverAskAgainComponent())
                .addAdapterComponent(PeopleNearbyLoaderComponent())
                // при создании фрагмента вставляем лоадер, который прибъем после получения первых данных
    }
    private val mViewModel by lazy {
        PeopleNearbyFragmentViewModel(context, mApi) { mAdapter.updateObservable }.apply {
            activity.registerLifeCycleDelegate(this)
        }
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
         * 14.12.16
         */
        with(mBinding.refresh) {
            destroyDrawingCache()
            clearAnimation()
        }
        mViewModel.release()
        mAdapter.releaseComponents()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mViewModel.onActivityResult(requestCode, resultCode, data)
    }

    private fun initList() = with(mBinding.list) {
        layoutManager = LinearLayoutManager(context)
        adapter = mAdapter
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter.data.add(PeopleNearbyEmptyListComponent())
    }
}
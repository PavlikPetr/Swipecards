package com.topface.topface.ui.fragments.feed.dating

import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.LayoutEmptyDatingBinding
import com.topface.topface.requests.ApiRequest
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.http.IRequestClient
import java.util.*

/**
 * Created by mbulgakov on 10.11.16.
 */
class DatingEmptyFragment() : AbstractDialogFragment(), IRequestClient {

    companion object {
        const val TAG = "empty_dating_fragment"
        fun newInstance() = DatingEmptyFragment()
    }

    private val mRequests = LinkedList<ApiRequest>()

    private lateinit var mBinding: LayoutEmptyDatingBinding

    private val mApi by lazy {
        FeedApi(context, this)
    }

    private val mNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private val mViewModel by lazy {
        DatingEmptyFragmentViewModel(dialog, mBinding, mApi, mNavigator)
    }

    override fun initViews(root: View) {
        mBinding = LayoutEmptyDatingBinding.bind(root)
        with(mBinding) {
            setViewModel(mViewModel)
        }
    }

    override fun isModalDialog() = false

    override fun getDialogLayoutRes() = R.layout.layout_empty_dating

    override fun registerRequest(request: ApiRequest?) {
        if (!mRequests.contains(request)) {
            mRequests.add(request!!)
        }
    }

    override fun cancelRequest(request: ApiRequest) {
        request.cancelFromUi()
    }

}
package com.topface.topface.ui.dialogs.new_rate

import android.app.DialogFragment
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.topface.topface.R
import com.topface.topface.databinding.RateAppLayoutBinding
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.DeleteFeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.http.IRequestClient
import org.jetbrains.anko.layoutInflater

class RateAppFragment(private val mFeedNavigator: FeedNavigator) : DialogFragment(), IDialogCloser {
    companion object{
        const val TAG = "RateAppFragment"
    }

    private val mViewModel by lazy { RateAppViewModel(mFeedNavigator, this@RateAppFragment, mApi) }

    private val mBinding by lazy {
        DataBindingUtil.inflate<RateAppLayoutBinding>(context.layoutInflater, R.layout.rate_app_layout, null, false)
    }

    private val mApi by lazy {
        FeedApi(context, activity as IRequestClient, DeleteFeedRequestFactory(context))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = with(mBinding) {
        viewModel = mViewModel
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        root
    }

    override fun closeIt() {
        dialog.cancel()
    }


}
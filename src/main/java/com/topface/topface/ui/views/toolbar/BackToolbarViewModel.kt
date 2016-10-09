package com.topface.topface.ui.views.toolbar

import android.support.v7.widget.Toolbar
import android.view.View
import com.topface.topface.App
import com.topface.topface.data.BalanceData
import com.topface.topface.data.CountersData
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.utils.RxUtils
import rx.Subscription
import javax.inject.Inject
import com.topface.topface.R

/**
 * Created by petrp on 09.10.2016.
 */

class BackToolbarViewModel(toolbar: Toolbar, title: String, clickListener: View.OnClickListener)
: ToolbarBaseViewModel(toolbar, title = title, icon = R.drawable.ic_arrow_up, onUpButtonClickListener = clickListener) {

}

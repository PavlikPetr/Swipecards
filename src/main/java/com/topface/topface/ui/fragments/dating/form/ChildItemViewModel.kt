package com.topface.topface.ui.fragments.dating.form

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.R
import com.topface.topface.requests.IApiResponse
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.RxUtils

/**
 * VM итема формы
 * Created by tiberal on 07.11.16.
 */
class ChildItemViewModel(private val mApi: FeedApi, private val data: FormModel) {

    val title = ObservableField<String>(data.data?.first)
    val icon = ObservableInt(data.iconRes)
    val background = ObservableInt(data.formItemBackground)
    val subTitle = ObservableField<String>(data.data?.second)
    val isRequestButtonVisible = ObservableInt(if (data.isEmptyItem) View.VISIBLE else View.INVISIBLE)

    fun sendInfoRequest() {
        data.userId?.let {
            mApi.callStandartMessageRequest(data.formType, it).subscribe(object : RxUtils.ShortSubscription<IApiResponse>() {
                override fun onNext(type: IApiResponse?) {
                    icon.set(R.drawable.ask_info_done)
                }
            })
        }
    }

}
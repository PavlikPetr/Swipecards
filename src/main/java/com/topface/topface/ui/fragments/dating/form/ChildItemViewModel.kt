package com.topface.topface.ui.fragments.dating.form

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.annotation.DrawableRes
import android.view.View
import com.topface.topface.R
import com.topface.topface.requests.IApiResponse
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.Utils

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
        if (data.iconRes != R.drawable.ask_info_done) {
            data.userId?.let {
                setIcon(R.drawable.ask_info_done)
                mApi.callStandartMessageRequest(data.formType, it)
                        .subscribe(object : RxUtils.ShortSubscription<IApiResponse>() {
                            override fun onNext(type: IApiResponse?) {
                                data.onRequestSended?.invoke()
                            }

                            override fun onError(e: Throwable?) {
                                Utils.showErrorMessage()
                                setIcon(R.drawable.bt_question)
                                super.onError(e)
                            }
                        })
            }
        }
    }

    private fun setIcon(@DrawableRes res: Int) {
        data.iconRes = res
        icon.set(res)
    }

}
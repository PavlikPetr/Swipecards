package com.topface.topface.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import com.topface.framework.utils.Debug
import java.util.*
import kotlin.properties.Delegates

/**
 * ProgressBar для отравки статистики
 * Created by ppavlik on 24.03.17.
 */
class CustomProgressBar constructor(context: Context, attrs: AttributeSet?,
                                    defStyleAttr: Int,
                                    defStyleRes: Int) :
        ProgressBar(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    private var isProgressVisible by Delegates.observable<Boolean?>(null) { prop, old, new ->
        new?.let { new ->
            if (new && new != old) {
                mOnShowTime = Calendar.getInstance().timeInMillis
                sendShowEvent()
            }
            old?.let {
                if (new != it) {
                    sendHideEvent()
                }
            }
        }
    }

    private var mOnShowTime: Long = 0
    private var mPlc = "UNDEFINED"

    companion object {
        const val TAG = "CustomProgressBar"
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isProgressVisible = false
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        isProgressVisible = visibility == View.VISIBLE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isProgressVisible = visibility == View.VISIBLE
    }

    /**
     * Задать название экрана, который отобразил лоадер
     * @param plc - место показа лоадера
     */
    fun setPlc(plc: String) {
        mPlc = plc
    }

    private fun sendShowEvent() {
        Debug.error("$TAG sendShowEvent plc:$mPlc")
    }


    private fun sendHideEvent() {
        Debug.error("$TAG sendHideEvent plc:$mPlc timeout:${Calendar.getInstance().timeInMillis - mOnShowTime}")
    }
}
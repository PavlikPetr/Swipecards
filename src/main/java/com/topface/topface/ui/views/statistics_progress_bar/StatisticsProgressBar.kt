package com.topface.topface.ui.views.statistics_progress_bar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ProgressBar
import com.topface.framework.utils.Debug
import com.topface.statistics.android.Slices
import com.topface.statistics.generated.ProgressStatisticsGeneratedStatistics
import com.topface.topface.R
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Emitter
import rx.Observable
import rx.lang.kotlin.withIndex
import rx.subscriptions.CompositeSubscription
import java.util.concurrent.TimeUnit

/**
 * ProgressBar для отравки статистики
 * Created by ppavlik on 24.03.17.
 */
open class StatisticsProgressBar constructor(context: Context, attrs: AttributeSet?,
                                             defStyleAttr: Int,
                                             defStyleRes: Int) :
        ProgressBar(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0) {
        attrs?.let {
            parseAttribute(it, defStyleAttr)
        }
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    companion object {
        const val TAG = "StatisticsProgressBar"
        const val PLC_UNDEFINED = "UNDEFINED"
    }

    private var mPlc = PLC_UNDEFINED
    private var mEmitter: Emitter<Boolean>? = null
    private var mSubscription = CompositeSubscription()

    init {
        mSubscription.add(Observable.fromEmitter<Boolean>({ emitter ->
            mEmitter = emitter
        }, Emitter.BackpressureMode.LATEST)
//                .filter {
////                    Debug.log("$TAG catch new value ui:$this isVisible:$it")
//                    true
//                }
                .map {
                    Debug.log("BeforeDistinct ui:$this state:$it")
                    it
                }
                .distinctUntilChanged()
                .map {
                    Debug.log("AfterDistinct ui:$this state:$it")
                    it
                }
//                .withIndex()
//                .filter { it.index > 0 || it.value }
//                .map { it.value }
                .timeInterval()
                .subscribe(shortSubscription {
                    it?.let {
                        if (it.value) {
                            sendShowEvent()
                        } else {
                            sendHideEvent(it.intervalInMilliseconds)
                        }
                    }
                }))
        // костыль на случай если не сработал onVisibilityChanged
        mSubscription.add(Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribe(shortSubscription { mEmitter?.onNext(visibility == View.VISIBLE && alpha > 0) }))
    }

    private fun parseAttribute(attrs: AttributeSet, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StatisticsProgressBar, defStyleAttr, 0)
        setPlc(a.getString(R.styleable.StatisticsProgressBar_plc))
        a.recycle()
    }

    override fun setVisibility(visibility: Int) {
        mEmitter?.onNext(visibility == View.VISIBLE)
        super.setVisibility(visibility)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mEmitter?.onNext(false)
        mEmitter?.onCompleted()
        mSubscription.safeUnsubscribe()
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        mEmitter?.onNext(visibility == View.VISIBLE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mEmitter?.onNext(visibility == View.VISIBLE && alpha > 0)
    }

    override fun onSetAlpha(alpha: Int): Boolean {
        mEmitter?.onNext(alpha > 0)
        return super.onSetAlpha(alpha)
    }

    /**
     * Задать название экрана, который отобразил лоадер
     * @param plc - место показа лоадера
     */
    fun setPlc(plc: String?) {
        if (mPlc.isNullOrEmpty() || mPlc == PLC_UNDEFINED) {
            mPlc = plc ?: PLC_UNDEFINED
        }
    }

    private fun sendShowEvent() {
        Debug.log("$TAG sendShowEvent plc:$mPlc")
        ProgressStatisticsGeneratedStatistics.sendNow_LOADER_SHOW(Slices().apply { putSlice("plc", mPlc) })
    }

    private fun sendHideEvent(interval: Long) {
        Debug.log("$TAG sendHideEvent plc:$mPlc timeout:$interval")
        ProgressStatisticsGeneratedStatistics.sendNow_LOADER_HIDE(Slices().apply {
            putSlice("plc", mPlc)
            putSlice("int", interval.toString())
        })
    }
}
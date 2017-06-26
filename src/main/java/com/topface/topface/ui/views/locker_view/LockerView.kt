package com.topface.topface.ui.views.locker_view

import android.content.Context
import android.databinding.DataBindingUtil
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.topface.topface.R
import com.topface.topface.databinding.LayoutLockerViewBinding
import com.topface.topface.ui.views.statistics_progress_bar.StatisticsProgressBar
import org.jetbrains.anko.layoutInflater

/**
 * Кастомная вью лоадера с текстом
 * Created by petrp on 28.03.2017.
 */
class LockerView : RelativeLayout {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initialize(attrs, defStyleAttr)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(attrs, defStyleAttr)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(attrs)
    }

    constructor(context: Context) : super(context) {
        initialize()
    }

    private var mPlc = StatisticsProgressBar.PLC_UNDEFINED
    private var mViewModel: LockerViewViewModel? = null

    private val mBinding by lazy {
        DataBindingUtil.inflate<LayoutLockerViewBinding>(context.layoutInflater, R.layout.layout_locker_view, null, false)
    }

    private fun initialize(attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
        mViewModel = LockerViewViewModel(mPlc)
        attrs?.let {
            parseAttribute(it, defStyleAttr)
        }
        mViewModel?.visibility?.set(visibility)
    }

    private fun parseAttribute(attrs: AttributeSet, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StatisticsProgressBar, defStyleAttr, 0)
        setPlc(a.getString(R.styleable.StatisticsProgressBar_plc))
        a.recycle()
    }

    fun setPlc(plc: String?) {
        if (mPlc.isNullOrEmpty() || mPlc == StatisticsProgressBar.PLC_UNDEFINED) {
            mPlc = plc ?: StatisticsProgressBar.PLC_UNDEFINED
            mViewModel?.plc?.set(plc)
        }
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        mViewModel?.visibility?.set(visibility)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mBinding.viewModel = mViewModel?.apply { visibility.set(this@LockerView.visibility) }
        mBinding.executePendingBindings()
    }

    override fun onTouchEvent(event: MotionEvent) = true
}
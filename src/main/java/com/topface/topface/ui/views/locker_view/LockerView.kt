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
class LockerView constructor(context: Context, attrs: AttributeSet?,
                             defStyleAttr: Int,
                             defStyleRes: Int) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0) {
        attrs?.let {
            parseAttribute(it, defStyleAttr)
        }
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    private val mBinding by lazy {
        DataBindingUtil.inflate<LayoutLockerViewBinding>(context.layoutInflater, R.layout.layout_locker_view, null, false)
    }

    private val mViewModel by lazy {
        LockerViewViewModel(mPlc)
    }

    private var mPlc: String = StatisticsProgressBar.PLC_UNDEFINED

    init {
        mBinding.viewModel = mViewModel
    }

    private fun parseAttribute(attrs: AttributeSet, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StatisticsProgressBar, defStyleAttr, 0)
        setPlc(a.getString(R.styleable.StatisticsProgressBar_plc))
        a.recycle()
    }

    fun setPlc(plc: String) {
        mPlc = plc
        mViewModel.plc.set(plc)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mViewModel.release()
    }

    override fun onVisibilityChanged(changedView: View?, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        mViewModel.visibility.set(visibility)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mViewModel.visibility.set(visibility)
    }

    override fun onTouchEvent(event: MotionEvent) = true
}
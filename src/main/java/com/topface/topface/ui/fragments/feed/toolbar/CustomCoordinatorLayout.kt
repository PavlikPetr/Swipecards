package com.topface.topface.ui.fragments.feed.toolbar

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent
import com.topface.framework.utils.Debug
import com.topface.topface.R

/**
 * Created by ppavlik on 13.12.16.
 */
class CustomCoordinatorLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : CoordinatorLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mPrevMotionEv: MyMotionEvent = MyMotionEvent.empty()
    private var mIsHorizontalScroll: Boolean? = null

    override fun onInterceptTouchEvent(motionEvent: MotionEvent?): Boolean {
        printMotionEvent("catchTouch", motionEvent)
        when (motionEvent?.action) {
            MotionEvent.ACTION_DOWN -> {
                dropSavedData()
                motionEvent?.let { mPrevMotionEv = MyMotionEvent(it) }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsHorizontalScroll == null) {
                    mIsHorizontalScroll = getDistanceX(motionEvent) > 3*getDistanceY(motionEvent)
                    motionEvent?.let { Debug.error("COORDINATOR_TOUCH mIsHorizontalScroll = $mIsHorizontalScroll ") }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                dropSavedData()
            }
        }
        return senMotionEvent(motionEvent)
    }

    private fun dropSavedData() {
        recyclePrevMotionEvent()
        mIsHorizontalScroll = null
    }

    private fun recyclePrevMotionEvent() {
        mPrevMotionEv.recycle()
    }

    private fun getDistanceX(motionEvent: MotionEvent?): Float {
        motionEvent?.let { currentMotionEvent ->
            if (!mPrevMotionEv.isEmpty()) {
                return Math.abs(currentMotionEvent.x - mPrevMotionEv.x)
            }
        }
        return 0f
    }

    private fun getDistanceY(motionEvent: MotionEvent?): Float {
        motionEvent?.let { currentMotionEvent ->
            if (!mPrevMotionEv.isEmpty()) {
                return Math.abs(currentMotionEvent.y - mPrevMotionEv.y)
            }
        }
        return 0f
    }

    private fun senMotionEvent(motionEvent: MotionEvent?): Boolean {
        if (motionEvent != null) {
            mIsHorizontalScroll?.let {
                if (it) {
                    findViewById(R.id.collapse_frame)?.let { view ->
                        if(!mPrevMotionEv.isEmpty()){
                            mPrevMotionEv.getMotionEvent().apply {
                                view.onTouchEvent(this)
                                printMotionEvent("send it to view", this)
                                recyclePrevMotionEvent()
                            }
                        }
                        view.onTouchEvent(motionEvent)
                        printMotionEvent("send motionEvent to view", motionEvent)
                    }

                } else {
                    if(!mPrevMotionEv.isEmpty()){
                        mPrevMotionEv.getMotionEvent().apply {
                            super.onInterceptTouchEvent(this)
                            printMotionEvent("send it to super", this)
                            recyclePrevMotionEvent()
                        }
                    }
                    printMotionEvent("send motionEvent to super", motionEvent)
                    return super.onInterceptTouchEvent(motionEvent)
                }
            }
        }
        return false
    }

    private fun printMotionEvent(tag: String, ev: MotionEvent?) {
        ev?.let { Debug.error("COORDINATOR_TOUCH $tag action = ${ev.action} x = ${ev.x} y = ${ev.y} ") } ?: Debug.error("COORDINATOR_TOUCH $tag action = null")
    }

    data class MyMotionEvent(var downTime: Long = 0L, var eventTime: Long = 0L, var action: Int = MyMotionEvent.UNDEFINED_ACTION,
                             var x: Float = 0F, var y: Float = 0F, var metaState: Int = UNDEFINED_META_STATE) {
        constructor(ev: MotionEvent) : this(ev.downTime, ev.eventTime, ev.action, ev.x, ev.y, ev.metaState)

        companion object {
            public const val UNDEFINED_ACTION = Int.MAX_VALUE
            public const val UNDEFINED_META_STATE = Int.MAX_VALUE
            public fun empty() = MyMotionEvent()
        }

        public fun getMotionEvent() = MotionEvent.obtain(downTime, eventTime, action, x, y, metaState)

        public fun isEmpty() = downTime == 0L && eventTime == 0L && action == UNDEFINED_ACTION
                && x == 0F && y == 0F && metaState == UNDEFINED_META_STATE

        public fun recycle() {
            downTime = 0L
            eventTime = 0L
            action = UNDEFINED_ACTION
            x = 0F
            y = 0F
            metaState = UNDEFINED_META_STATE
        }
    }
}
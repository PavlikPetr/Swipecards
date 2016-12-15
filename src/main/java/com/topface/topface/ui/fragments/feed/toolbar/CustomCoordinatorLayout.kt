package com.topface.topface.ui.fragments.feed.toolbar

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent
import com.topface.framework.utils.Debug
import com.topface.topface.R
import com.topface.topface.utils.Utils

/**
 * Created by ppavlik on 13.12.16.
 */
class CustomCoordinatorLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : CoordinatorLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mPrevMotionEv: MyMotionEvent = MyMotionEvent.empty()
    private var mIsHorizontalScroll: Boolean? = null
    private val mViewConfigList = listOf<ViewConfig>(ViewConfig(R.id.dating_album, 1f, 0.4f, true))

    override fun onInterceptTouchEvent(motionEvent: MotionEvent?): Boolean {
        printMotionEvent("catchTouch", motionEvent)
        when (motionEvent?.action) {
            MotionEvent.ACTION_DOWN -> {
                dropSavedData()
                motionEvent?.let {
                    mPrevMotionEv = MyMotionEvent(it)
                    mPrevMotionEv.viewConfig = findTrackedViewTouch(it)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsHorizontalScroll == null) {
                    mIsHorizontalScroll = mPrevMotionEv.viewConfig.horizontalRider * getDistanceX(motionEvent) >
                            mPrevMotionEv.viewConfig.verticalRider * getDistanceY(motionEvent)
                    motionEvent?.let { Debug.error("COORDINATOR_TOUCH mIsHorizontalScroll = $mIsHorizontalScroll ") }
                }
            }
        }
        return senMotionEvent(motionEvent)
    }

    private fun findTrackedViewTouch(motionEvent: MotionEvent?): ViewConfig {
        motionEvent?.let { ev ->
            mViewConfigList.find {
                val id = it.id
                if (id != null) {
                    findViewById(id)?.let { view ->
                        val position = Utils.getLocationInWindow(view)
                        ev.x > position[0] && ev.x < (position[0] + view.width) && ev.y > position[1] && ev.y < (position[1] + view.height)
                    } ?: false
                } else false
            }?.let { return it }
        }
        return ViewConfig()
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
            if (!mPrevMotionEv.isRecycled) {
                return Math.abs(currentMotionEvent.x - mPrevMotionEv.x)
            }
        }
        return 0f
    }

    private fun getDistanceY(motionEvent: MotionEvent?): Float {
        motionEvent?.let { currentMotionEvent ->
            if (!mPrevMotionEv.isRecycled) {
                return Math.abs(currentMotionEvent.y - mPrevMotionEv.y)
            }
        }
        return 0f
    }

    private fun senMotionEvent(motionEvent: MotionEvent?): Boolean {
        if (motionEvent != null) {
            val id = mPrevMotionEv.viewConfig.id
            if (id != null) {
                mIsHorizontalScroll?.let {
                    if (it == mPrevMotionEv.viewConfig.isHorizontalScrollPrefer) {
                        val tempMotionEvent = MyMotionEvent(motionEvent)
                        if (it) {
                            tempMotionEvent.y = mPrevMotionEv.y
                        } else {
                            tempMotionEvent.x = mPrevMotionEv.x
                        }
                        sendMotionEventToView(tempMotionEvent.getMotionEvent(), id)?.let { return it }
                    } else {
                        sendMotionEventToParent(motionEvent)?.let { return it }
                    }
                }
            } else {
                sendMotionEventToParent(motionEvent)?.let { return it }
            }
        }
        return false
    }

    private fun sendMotionEventToView(motionEvent: MotionEvent?, viewId: Int): Boolean? {
        if (motionEvent != null) {
            findViewById(viewId)?.let { view ->
                if (!mPrevMotionEv.isRecycled) {
                    mPrevMotionEv.getMotionEvent().apply {
                        view.onTouchEvent(this)
                        printMotionEvent("send it to view", this)
                        recyclePrevMotionEvent()
                    }
                }
                view.onTouchEvent(motionEvent)
                printMotionEvent("send motionEvent to view", motionEvent)
            }
        }
        return null
    }

    private fun sendMotionEventToParent(motionEvent: MotionEvent?): Boolean? {
        if (motionEvent != null) {
            if (!mPrevMotionEv.isRecycled) {
                mPrevMotionEv.getMotionEvent().apply {
                    super.onInterceptTouchEvent(this)
                    printMotionEvent("send it to super", this)
                    recyclePrevMotionEvent()
                }
            }
            printMotionEvent("send motionEvent to super", motionEvent)
            return super.onInterceptTouchEvent(motionEvent)
        }
        return null
    }

    private fun printMotionEvent(tag: String, ev: MotionEvent?) {
        ev?.let { Debug.error("COORDINATOR_TOUCH $tag action = ${ev.action} x = ${ev.x} y = ${ev.y} ") } ?: Debug.error("COORDINATOR_TOUCH $tag action = null")
    }

    data class MyMotionEvent(var downTime: Long = 0L, var eventTime: Long = 0L, var action: Int = MyMotionEvent.UNDEFINED_ACTION,
                             var x: Float = 0F, var y: Float = 0F, var metaState: Int = UNDEFINED_META_STATE,
                             var viewConfig: ViewConfig = ViewConfig(), var isRecycled: Boolean = false) {
        constructor(ev: MotionEvent) : this(ev.downTime, ev.eventTime, ev.action, ev.x, ev.y, ev.metaState)

        companion object {
            public const val UNDEFINED_ACTION = Int.MAX_VALUE
            public const val UNDEFINED_META_STATE = Int.MAX_VALUE
            public fun empty() = MyMotionEvent(isRecycled = true)
        }

        public fun getMotionEvent() = MotionEvent.obtain(downTime, eventTime, action, x, y, metaState)

        public fun recycle() {
            isRecycled = true
        }
    }

    data class ViewConfig(var id: Int? = null, var horizontalRider: Float = 1F, var verticalRider: Float = 1F, var isHorizontalScrollPrefer: Boolean? = null)
}
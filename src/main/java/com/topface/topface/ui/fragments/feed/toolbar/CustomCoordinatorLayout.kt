package com.topface.topface.ui.fragments.feed.toolbar

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.MotionEvent
import com.topface.framework.utils.Debug
import com.topface.topface.utils.Utils

/**
 * CoordinatorLayout который умеет досылать события touch во view, заданных в setViewConfigList
 * Created by ppavlik on 13.12.16.
 */
class CustomCoordinatorLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : CoordinatorLayout(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private var mPrevMotionEv: MyMotionEvent = MyMotionEvent.empty()
    private var mIsHorizontalScroll: Boolean? = null
    private var mViewConfigList = listOf<ViewConfig>()

    override fun onInterceptTouchEvent(motionEvent: MotionEvent?): Boolean {
        when (motionEvent?.action) {
            MotionEvent.ACTION_DOWN -> {
                dropSavedData()
                motionEvent?.let {
                    mPrevMotionEv = MyMotionEvent(it)
                    /**
                     * находим view, на которую приходится первое касание
                     * ориентируемся только на список вью, в которых следует переопределить логику поведения скролов
                     */
                    mPrevMotionEv.viewConfig = findTrackedViewTouch(it)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // только при первом событии ACTION_MOVE определяем направление скрола, чтобы оно не менялось в процессе
                if (mIsHorizontalScroll == null) {
                    mIsHorizontalScroll = mPrevMotionEv.viewConfig.horizontalRider * getDistanceX(motionEvent) >
                            mPrevMotionEv.viewConfig.verticalRider * getDistanceY(motionEvent)
                }
            }
        }
        return senMotionEvent(motionEvent)
    }

    public fun setViewConfigList(list: List<ViewConfig>) {
        mViewConfigList = list
    }

    private fun findTrackedViewTouch(motionEvent: MotionEvent?): ViewConfig {
        motionEvent?.let { ev ->
            mViewConfigList.find {
                val id = it.id
                if (id != null) {
                    findViewById(id)?.let { view ->
                        val position = Utils.getLocationInWindow(view)
                        ev.x > position[0] && ev.x < (position[0] + view.width) &&
                                ev.y > position[1] && ev.y < (position[1] + view.height)
                    } ?: false
                } else false
            }?.let { return it }
        }
        return ViewConfig()
    }

    // сбрасываем хранимые данные в дефолт
    private fun dropSavedData() {
        recyclePrevMotionEvent()
        mIsHorizontalScroll = null
    }

    private fun recyclePrevMotionEvent() = mPrevMotionEv.recycle()

    // находим величину смещения по оси X
    private fun getDistanceX(motionEvent: MotionEvent?): Float {
        motionEvent?.let {
            if (!mPrevMotionEv.isRecycled) {
                return Math.abs(it.x - mPrevMotionEv.x)
            }
        }
        return 0f
    }

    // находим величину смещения по оси Y
    private fun getDistanceY(motionEvent: MotionEvent?): Float {
        motionEvent?.let {
            if (!mPrevMotionEv.isRecycled) {
                return Math.abs(it.y - mPrevMotionEv.y)
            }
        }
        return 0f
    }

    private fun senMotionEvent(motionEvent: MotionEvent?) = motionEvent?.let {
        val id = mPrevMotionEv.viewConfig.id
        /**
         * если тач начинался со view, для которой следует корректировать свайп,
         * то в нее и отправим данные
         * в противном случае в super
         */
        if (id != null) {
            mIsHorizontalScroll?.let {
                /**
                 * если текущее состояние направления скрола совпадает с тем, которое следует
                 * переопределить для этой view, то подменяем координаты одной из осей
                 * т.е. если скролл горизонтальный, то подменим координаты тача по Y
                 * это нужно для того, чтобы view, в которую мы досылаем тач не сомневалась,
                 * что скролл именно в этом направлении выполняется
                 *
                 * если направление скрола не совпадает с предпочтениями этой вью, то отправляем тачи
                 * в super
                 */
                if (it == mPrevMotionEv.viewConfig.isHorizontalScrollPrefer) {
                    val tempMotionEvent = MyMotionEvent(motionEvent)
                    if (it) {
                        tempMotionEvent.y = mPrevMotionEv.y
                    } else {
                        tempMotionEvent.x = mPrevMotionEv.x
                    }
                    sendMotionEventToView(tempMotionEvent.getMotionEvent(), id)?.let { it }
                } else {
                    sendMotionEventToParent(motionEvent)?.let { it }
                }
            }
        } else {
            sendMotionEventToParent(motionEvent)?.let { it }
        }
    } ?: false

    // досылаем тачи во вью, на которую пришелся ACTION_DOWN, если она была в списке конфигураций
    private fun sendMotionEventToView(motionEvent: MotionEvent?, viewId: Int): Boolean? {
        if (motionEvent != null) {
            findViewById(viewId)?.let { view ->
                if (!mPrevMotionEv.isRecycled) {
                    mPrevMotionEv.getMotionEvent().apply {
                        view.onTouchEvent(this)
                        recyclePrevMotionEvent()
                    }
                }
                view.onTouchEvent(motionEvent)
            }
        }
        return null
    }

    // отправляем тачи в super
    private fun sendMotionEventToParent(motionEvent: MotionEvent?): Boolean? {
        if (motionEvent != null) {
            if (!mPrevMotionEv.isRecycled) {
                mPrevMotionEv.getMotionEvent().apply {
                    super.onInterceptTouchEvent(this)
                    recyclePrevMotionEvent()
                }
            }
            return super.onInterceptTouchEvent(motionEvent)
        }
        return null
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

    data class ViewConfig(var id: Int? = null, var horizontalRider: Float = 1F, var verticalRider: Float = 1F,
                          var isHorizontalScrollPrefer: Boolean? = null)
}
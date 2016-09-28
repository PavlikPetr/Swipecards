package com.topface.topface.ui.fragments.feed.feed_base

import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import android.support.annotation.LayoutRes
import android.view.View
import android.view.ViewStub
import com.topface.framework.utils.Debug
import com.topface.topface.BR
import com.topface.topface.viewModels.BaseViewModel

/**
 * Контроллер для управлления заглушками
 * Created by tiberal on 10.08.16.
 * @param T empty screen binding
 * @param VM empty screen VM
 * @property mStub - view stub proxy from binding class (from base feed fragment layout)
 */
abstract class BaseFeedLockerController<T : ViewDataBinding, VM : BaseViewModel<T>>(private val mStub: ViewStubProxy) : ViewStub.OnInflateListener, IFeedLockerView {

    protected var mStubModel: VM? = null
    private var mViewStub: ViewStub? = null
    var lockScreenFactory: ILockScreenVMFactory<T>? = null
    var mVariableId: Int = BR.lockViewModel

    abstract fun initLockedFeedStub(errorCode: Int)

    abstract fun initEmptyFeedStub()

    fun setLockerLayout(@LayoutRes lockerLayout: Int) {
        mStub.setOnInflateListener(this)
        mStub.viewStub.layoutResource = lockerLayout
    }

    override fun onFilledFeed() {
        Debug.error("FEED onFilledFeed")
        setStubVisibility(View.GONE)
    }

    override fun onEmptyFeed() {
        Debug.error("FEED onEmptyFeed")
        setStubVisibility(View.VISIBLE)
        initEmptyFeedStub()
    }

    override fun onLockedFeed(errorCode: Int) {
        Debug.error("FEED onLockedFeed")
        setStubVisibility(View.VISIBLE)
        initLockedFeedStub(errorCode)
    }

    override fun onInflate(stub: ViewStub?, inflated: View?) {
        //после эого метода стаб в прокси будет null, такие вот дела
        mViewStub = stub
        lockScreenFactory?.let { factory ->
            mStub.binding?.let {
                mStubModel = factory.construct(it) as VM
                it.setVariable(mVariableId, mStubModel)
            }
        }
    }

    private fun setStubVisibility(visibility: Int) = (mStub.viewStub ?: mViewStub)?.let {
        it.visibility = visibility
    }

    fun release() {
        mStubModel?.release()
        mViewStub = null
        mStubModel = null
        lockScreenFactory = null
    }

    interface ILockScreenVMFactory<T : ViewDataBinding> {
        fun construct(binding: ViewDataBinding): BaseViewModel<T>
    }

}


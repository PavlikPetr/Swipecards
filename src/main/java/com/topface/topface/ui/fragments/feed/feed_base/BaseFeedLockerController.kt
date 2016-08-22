package com.topface.topface.ui.fragments.feed.feed_base

import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import android.support.annotation.LayoutRes
import android.view.View
import android.view.ViewStub
import com.topface.topface.BR
import com.topface.topface.viewModels.BaseViewModel

/**
 * Контроллер для управлления заглушками
 * Created by tiberal on 10.08.16.
 */
abstract class BaseFeedLockerController<T : ViewDataBinding, VM : BaseViewModel<T>>(private val mStub: ViewStubProxy) : ViewStub.OnInflateListener, IFeedLockerView {

    protected var mStubModel: VM? = null
    var lockScreenFactory: ILockScreenVMFactory<T>? = null
    var mVariableId: Int = BR.lockViewModel

    abstract fun initLickedFeedStub(errorCode: Int)

    abstract fun initEmptyFeedStub()

    fun setLockerLayout(@LayoutRes lockerLayout: Int) {
        mStub.setOnInflateListener(this)
        mStub.viewStub.layoutResource = lockerLayout
    }

    override fun onFilledFeed() {
        mStub.viewStub.visibility = View.GONE
    }


    override fun onEmptyFeed() {
        mStub.viewStub.visibility = View.VISIBLE
        initEmptyFeedStub()
    }

    override fun onLockedFeed(errorCode: Int) {
        mStub.viewStub.visibility = View.VISIBLE
        initLickedFeedStub(errorCode)
    }

    override fun onInflate(stub: ViewStub?, inflated: View?) {
        lockScreenFactory?.let { factory ->
            mStub.binding?.let {
                mStubModel = factory.construct(it) as VM
                it.setVariable(mVariableId, mStubModel)
            }
        }
    }

    fun release() {
        mStubModel?.release()
        mStubModel = null
        lockScreenFactory = null
    }

    interface ILockScreenVMFactory<T : ViewDataBinding> {
        fun construct(binding: ViewDataBinding): BaseViewModel<T>
    }

}


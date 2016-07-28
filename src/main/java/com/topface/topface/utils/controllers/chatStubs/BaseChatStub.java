package com.topface.topface.utils.controllers.chatStubs;


import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.view.ViewStub;

import com.android.databinding.library.baseAdapters.BR;
import com.topface.topface.viewModels.BaseViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseChatStub<T extends ViewDataBinding, D extends BaseViewModel<T>> {

    private T mBinding;
    private D mViewModel;
    private ViewStub mViewStub;

    public BaseChatStub(@NotNull ViewStub stub) {
        mViewStub = stub;
    }

    public void initViews() {
        if (mViewStub != null) {
            mViewStub.setLayoutResource(getViewLayout());
            mBinding = DataBindingUtil.bind(mViewStub.inflate());
            mViewModel = createViewModel(mBinding);
            mBinding.setVariable(BR.viewModel, mViewModel);
        }
    }

    @Nullable
    public T getBinding() {
        return mBinding;
    }

    @Nullable
    public D getViewModel() {
        return mViewModel;
    }

    public void release() {
        if (mViewModel != null) {
            mViewModel.release();
            mViewModel = null;
        }
        mBinding = null;
        mViewStub = null;
    }

    @LayoutRes
    abstract int getViewLayout();

    @NotNull
    abstract D createViewModel(T binding);
}

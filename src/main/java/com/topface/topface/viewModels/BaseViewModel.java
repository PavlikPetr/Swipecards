package com.topface.topface.viewModels;

import android.databinding.ViewDataBinding;
import android.os.Bundle;

/**
 * Модель с базовым функционалом
 * Created by tiberal on 28.06.16.
 */
public class BaseViewModel<T extends ViewDataBinding> {

    private Bundle mArguments;
    private T mBinding;

    public BaseViewModel(T binding) {
        mBinding = binding;
    }

    public BaseViewModel(T binding, Bundle arguments) {
        mBinding = binding;
        mArguments = arguments;
    }

    public void release() {
        mBinding = null;
    }

    public T getBinding() {
        return mBinding;
    }

    public Bundle getArguments() {
        return mArguments;
    }
}

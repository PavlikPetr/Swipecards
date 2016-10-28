package com.topface.topface.viewModels;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Модель с базовым функционалом
 * Created by tiberal on 28.06.16.
 */
public class BaseViewModel<T extends ViewDataBinding> {

    private Bundle mArguments;
    private T mBinding;
    private Context mContext;

    public BaseViewModel(@NotNull T binding) {
        this(binding, null);
    }

    public BaseViewModel(@NotNull T binding, @Nullable Bundle arguments) {
        mBinding = binding;
        mArguments = arguments;
        mContext = binding.getRoot().getContext().getApplicationContext();
    }

    public void release() {
        mBinding = null;
        mContext = null;
    }

    public T getBinding() {
        return mBinding;
    }

    public Context getContext() {
        return mContext;
    }

    @Nullable
    public Bundle getArguments() {
        return mArguments;
    }
}

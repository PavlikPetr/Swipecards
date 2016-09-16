package com.topface.topface.utils.adapter_utils;

import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.Nullable;

/**
 * Фабричный интефейс,использовать, когда нужно воткнуть несколько одинаковых вьюх.
 * Created by tiberal on 24.06.16.
 */
public interface IInjectViewFactory {

    View construct(@Nullable ViewGroup parent);

}

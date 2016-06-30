package com.topface.topface.utils.adapter_utils;

import android.view.View;

/**
 * Фабричный интефейс,использовать, когда нужно воткнуть несколько одинаковых вьюх.
 * Created by tiberal on 24.06.16.
 */
public interface IInjectViewFactory {

    View construct();

}

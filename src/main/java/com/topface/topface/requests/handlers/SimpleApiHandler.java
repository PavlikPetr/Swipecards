package com.topface.topface.requests.handlers;

import android.app.Activity;

import com.topface.topface.requests.IApiResponse;
import com.topface.topface.utils.Utils;

public class SimpleApiHandler extends ApiHandler {
    @Override
    public void success(IApiResponse response) {
        //Этот метод можно переопределить
    }

    @Override
    public void fail(int codeError, IApiResponse response) {
        //По умолчанию показываем Toast с ошибкой
        fail(true);
        //Этот метод можно переопределить
    }

    public void fail(boolean showError) {
        if (showError && hasContext() && getContext() instanceof Activity) {
            Utils.showErrorMessage();
        }
    }

    @Override
    public void always(IApiResponse response) {
        super.always(response);
    }
}

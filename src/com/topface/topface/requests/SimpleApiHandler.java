package com.topface.topface.requests;

import android.app.Activity;
import com.topface.topface.utils.Utils;

public class SimpleApiHandler extends ApiHandler {
    @Override
    public void success(ApiResponse response) {
        //Этот метод можно переопределить
    }

    @Override
    public void fail(int codeError, ApiResponse response) {
        //По умолчанию показываем Toast с ошибкой
        fail(codeError, response, true);
        //Этот метод можно переопределить
    }

    public void fail(int codeError, ApiResponse response, boolean showError) {
        if (showError && hasContext() && getContext() instanceof Activity) {
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showErrorMessage(getContext());
                }
            });
        }
    }

    @Override
    public void always(ApiResponse response) {
        //Этот метод можно переопределить
    }
}

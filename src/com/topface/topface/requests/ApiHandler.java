package com.topface.topface.requests;

import android.os.Handler;
import android.widget.Toast;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import org.json.JSONObject;

abstract public class ApiHandler extends Handler {

    public void response(ApiResponse response) {
        try {
            if (response.code == ApiResponse.ERRORS_PROCCESED) {
                fail(ApiResponse.ERRORS_PROCCESED, new ApiResponse(ApiResponse.ERRORS_PROCCESED, "Client exception"));
            } else if (response.code == ApiResponse.PREMIUM_ACCESS_ONLY) {
                //Сообщение о необходимости Премиум-статуса
                Toast.makeText(App.getContext(), R.string.general_premium_access_error, Toast.LENGTH_SHORT).show();
                fail(response.code, response);
            } else if (response.code != ApiResponse.RESULT_OK) {
                fail(response.code, response);
            } else {
                setCounters(response);
                success(response);
            }
        } catch (Exception e) {
            Debug.error("api handler exception", e);
        }
    }

    abstract public void success(ApiResponse response) throws NullPointerException;

    abstract public void fail(int codeError, ApiResponse response) throws NullPointerException;

    private void setCounters(ApiResponse response) {
        try {
            JSONObject counters = response.counters;
            String method = response.method;
            if (counters != null) {
                CountersManager.getInstance(App.getContext()).setMethod(method);
                CountersManager.getInstance(App.getContext()).
                        setAllCounters(
                                counters.optInt("unread_likes"),
                                counters.optInt("unread_symphaties"),
                                counters.optInt("unread_messages"),
                                counters.optInt("unread_visitors")
                        );
            }
        } catch (Exception e) {
            Debug.error("api handler exception", e);
        }
    }

}

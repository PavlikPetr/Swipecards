package com.topface.topface.requests;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import org.json.JSONObject;

abstract public class ApiHandler extends Handler {

    private Context mContext;

    public void response(ApiResponse response) {
        try {
            if (response.code == ApiResponse.ERRORS_PROCCESED) {
                fail(ApiResponse.ERRORS_PROCCESED, new ApiResponse(ApiResponse.ERRORS_PROCCESED, "Client exception"));
            } else if (response.code == ApiResponse.PREMIUM_ACCESS_ONLY) {
                Debug.error(App.getContext().getString(R.string.general_premium_access_error));

                //Сообщение о необходимости Премиум-статуса
                showToast(R.string.general_premium_access_error);

                fail(response.code, response);
            } else if (response.code != ApiResponse.RESULT_OK) {
                fail(response.code, response);
            } else {
                setCounters(response);
                success(response);
            }
        } catch (Exception e) {
            Debug.error("ApiHandler exception", e);
            fail(ApiResponse.ERRORS_PROCCESED, new ApiResponse(ApiResponse.ERRORS_PROCCESED, e.getMessage()));
        }
        try {
            always(response);
        } catch (Exception e) {
            Debug.error("ApiHandler always callback exception", e);
        }
    }

    private void showToast(final int stringId) {
        if (mContext != null && mContext instanceof Activity) {
            try {
                //показываем уведомление
                final Activity activity = ((Activity) mContext);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        if (stringId == R.string.general_premium_access_error) {
//                            Intent intent = new Intent(activity.getApplicationContext(), ContainerActivity.class);
//                            activity.startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
//                        } else {
                            Toast.makeText(App.getContext(), stringId, Toast.LENGTH_SHORT).show();
//                        }
                    }
                });
            } catch (Exception e) {
                Debug.error(e);
            }
        }
    }

    abstract public void success(ApiResponse response);

    abstract public void fail(int codeError, ApiResponse response);

    public void always(ApiResponse response) {
        //Можно переопределить, если вам нужен коллбэк, который выполняется всегда, вне зависимости от результата
    }

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

    protected boolean hasContext() {
        return mContext != null;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    protected Context getContext() {
        return mContext;
    }
}

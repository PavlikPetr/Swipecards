package com.topface.topface.utils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.utils.social.OkAuthorizer;

import org.json.JSONObject;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import rx.functions.Action1;

public class OkUtils {

    public static void sendAppStartRequest() {
        sendAppStartRequest( new Action1<String>() {
            @Override
            public void call(String s) {
                Debug.log("Request " + GetInstallSourceRequest.SERVICE_NAME + " return result: " + s);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Debug.error("Sending request " + GetInstallSourceRequest.SERVICE_NAME + " catch error " + throwable);
            }
        });
    }

    public static void sendAppStartRequest( final Action1<String> onNextListener, final Action1<Throwable> onErrorListener) {
        final Odnoklassniki ok = new OkAuthorizer().getOkAuthObj(App.getAppSocialAppsIds());
        ok.checkValidTokens(new OkListener() {
            @Override
            public void onSuccess(JSONObject json) {
                Debug.log("Current OK token is valid. Try send app started request.");
                new GetInstallSourceRequest(ok, App.getContext()).getObservable().subscribe(onNextListener, onErrorListener);
            }

            @Override
            public void onError(String error) {
                Debug.error(error);
            }
        });
    }
}
package com.topface.topface.utils.social;

import android.text.TextUtils;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.state.TopfaceAppState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import javax.inject.Inject;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkRequestMode;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Петр on 03.04.2016.
 * Get user data in background
 */
public class CurrentUser extends OkRequest implements OkRequest.OkRequestListener {

    private static final String SERVICE_NAME = "users.getCurrentUser";

    @Inject
    TopfaceAppState mAppState;

    public CurrentUser(@NotNull Odnoklassniki ok) {
        super(ok);
        App.from(App.getContext()).inject(this);
        setUserListener(this);
    }

    @Override
    public void onSuccess(@Nullable OkUserData data) {
        mAppState.setData(data);
    }

    @Override
    public void onFail() {

    }

    @Override
    protected String getRequest(Odnoklassniki ok) throws IOException {
        return ok.request(SERVICE_NAME, null, OkRequestMode.DEFAULT);
    }

    @Override
    protected void getObservable(Observable<String> observable) {
        observable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                if (TextUtils.isEmpty(s)) {
                    Debug.log("OkRequest " + SERVICE_NAME + " request failed");
                    callFail();
                } else {
                    OkUserData data = JsonUtils.fromJson(s, OkUserData.class);
                    Debug.log("Odnoklassniki " + SERVICE_NAME + " request success result: " + s);
                    callSuccess(data);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                callFail();
            }
        }, new Action0() {
            @Override
            public void call() {

            }
        });
    }
}

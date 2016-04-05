package com.topface.topface.utils.social;

import com.topface.topface.App;
import com.topface.topface.state.TopfaceAppState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import ru.ok.android.sdk.Odnoklassniki;

/**
 * Created by Петр on 03.04.2016.
 * Get user data in background
 */
public class CurrentUser extends OkRequest implements OkRequest.OkRequestListener {

    private static final String SERVICE_NAME = "users.getCurrentUser";

    @Inject
    TopfaceAppState mAppState;

    public CurrentUser(@NotNull Odnoklassniki ok) {
        super(ok, SERVICE_NAME);
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
}

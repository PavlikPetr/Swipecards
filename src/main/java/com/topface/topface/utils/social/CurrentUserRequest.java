package com.topface.topface.utils.social;

import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkRequestMode;

/**
 * Created by Петр on 03.04.2016.
 * Get user data in background
 */
public class CurrentUserRequest extends OkRequest<OkUserData> {

    private static final String SERVICE_NAME = "users.getCurrentUser";

    public CurrentUserRequest(@NotNull Odnoklassniki ok) {
        super(ok);
    }

    @Override
    protected String getRequest(Odnoklassniki ok) throws IOException {
        return ok.request(SERVICE_NAME, null, OkRequestMode.DEFAULT);
    }

    @Override
    protected Type getDataType() {
        return new TypeToken<OkUserData>() {
        }.getType();
    }
}

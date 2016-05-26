package com.topface.topface.utils.social;

import com.google.gson.reflect.TypeToken;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

import ru.ok.android.sdk.Odnoklassniki;

/**
 * Created by Петр on 03.04.2016.
 * Get user data in background
 */
public class CurrentUserRequest extends OkRequest<OkUserData> {

    private static final String SERVICE_NAME = "users.getCurrentUser";

    public CurrentUserRequest(@NotNull Odnoklassniki ok) {
        super(ok);
    }

    @NotNull
    @Override
    protected String getRequestMethod() {
        return SERVICE_NAME;
    }

    @Override
    protected Type getDataType() {
        return new TypeToken<OkUserData>() {
        }.getType();
    }
}
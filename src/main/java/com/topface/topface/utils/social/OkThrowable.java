package com.topface.topface.utils.social;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

public class OkThrowable extends Throwable {

    public enum OkThrowableType {
        EMPTY_RESPONSE("");
        String mMessage;

        OkThrowableType(String message) {
            mMessage = message;
        }

        public String getMessage() {
            return mMessage;
        }
    }

    public OkThrowable(@NotNull OkThrowableType type) {
        this(type, null);
    }

    public OkThrowable(@NotNull OkThrowableType type, @Nullable String message) {
        super(getDetailMessage(type, message));
    }

    private static String getDetailMessage(OkThrowableType type, String message) {
        return TextUtils.isEmpty(message) ? type.getMessage() : type.getMessage() + " res: " + message;
    }
}

package com.topface.topface.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.topface.topface.utils.social.OkRequest;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkRequestMode;
import ru.ok.android.sdk.util.OkDevice;

/**
 * Created by Петр on 03.04.2016.
 * Report app start
 */
public class GetInstallSourceRequest extends OkRequest<String> {

    public static final String SERVICE_NAME = "sdk.getInstallSource";
    private static final String ADVERTISING_IDENTIFIER_PARAM = "adv_id";

    private Context mContext;


    public GetInstallSourceRequest(@NotNull Odnoklassniki ok, Context context) {
        super(ok);
        mContext = context;
    }


    @NotNull
    @Override
    protected String getRequestMethod() {
        return SERVICE_NAME;
    }

    @Nullable
    @Override
    protected Map<String, String> getRequestParams() {
        Map<String, String> param = new HashMap<>();
        param.put(ADVERTISING_IDENTIFIER_PARAM, OkDevice.getAdvertisingId(mContext));
        return param;
    }

    @Nullable
    @Override
    protected EnumSet<OkRequestMode> getRequestMode() {
        return EnumSet.of(OkRequestMode.GET, OkRequestMode.UNSIGNED);
    }

    @Override
    protected Type getDataType() {
        return new TypeToken<String>() {
        }.getType();
    }
}
package com.topface.topface.utils;

import android.support.annotation.Nullable;

import com.google.gson.reflect.TypeToken;
import com.topface.framework.JsonUtils;
import com.topface.topface.BuildConfig;
import com.topface.topface.data.OkStatsData;
import com.topface.topface.data.OkStatsResponseData;
import com.topface.topface.utils.social.OkRequest;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ru.ok.android.sdk.Odnoklassniki;

/**
 * Created by Петр on 03.04.2016.
 * Report ok statistics
 */
public class ReportStatsRequest extends OkRequest<OkStatsResponseData> {

    private static final String SERVICE_NAME = "sdk.reportStats";
    private static final String STATISTICS_FIELD_NAME = "stats";
    private static final String TIME_FIELD_NAME = "time";
    private static final String VERSION_FIELD_NAME = "version";

    private OkStatsData.OkStatsObjectData[] mData;

    public ReportStatsRequest(@NotNull Odnoklassniki ok, OkStatsData.OkStatsObjectData... data) {
        super(ok);
        mData = data;
    }

    @Nullable
    @Override
    protected Map<String, String> getRequestParams() {
        Map<String, String> param = new HashMap<>();
        param.put(STATISTICS_FIELD_NAME, JsonUtils.toJson(new OkStatsData(Calendar.getInstance().getTimeInMillis(), BuildConfig.VERSION_NAME, mData)));
        return param;
    }

    @NotNull
    @Override
    protected String getRequestMethod() {
        return SERVICE_NAME;
    }

    @Override
    protected Type getDataType() {
        return new TypeToken<OkStatsResponseData>() {
        }.getType();
    }
}
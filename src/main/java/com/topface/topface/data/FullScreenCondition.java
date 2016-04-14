package com.topface.topface.data;

import com.topface.topface.utils.DateUtils;

import org.json.JSONObject;

public class FullScreenCondition {
    // TODO после мержа в develop проверить серилизацию
    private static final String INTERVAL = "fullscreenInterval";
    private static final String PERIOD = "interval";
    private static final String SHOW_COUNT = "dailyShows";
    private static final String CONDITION_OBJECT = "startFullScreen";

    private long mFullScreenInterval;
    private long mFullScreenPeriod;
    private int mFullscreenShowCount;

    public FullScreenCondition() {
        mFullScreenInterval = DateUtils.DAY_IN_SECONDS;
        mFullScreenPeriod = 0;
        mFullscreenShowCount = 1;
    }

    public FullScreenCondition(JSONObject response) {
        mFullScreenInterval = response.optLong(INTERVAL, DateUtils.DAY_IN_SECONDS);
        if (response.has(CONDITION_OBJECT)) {
            JSONObject object = response.optJSONObject(CONDITION_OBJECT);
            if (object != null) {
                mFullScreenPeriod = object.optLong(PERIOD, 0);
                mFullscreenShowCount = object.optInt(SHOW_COUNT, 1);
            }
        }
    }

    public long getInterval() {
        return mFullScreenInterval;
    }

    public long getPeriod() {
        return mFullScreenPeriod;
    }

    public int getShowCount() {
        return mFullscreenShowCount;
    }
}

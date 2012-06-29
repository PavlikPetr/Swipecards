package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;

public abstract class AbstractData {
    public static Object parse(ApiResponse response) {
        return null;
    }
    public int getUid() {
        return -1;
    };
    public String getBigLink() {
        return null;
    };
    public String getSmallLink() {
        return null;
    };
}

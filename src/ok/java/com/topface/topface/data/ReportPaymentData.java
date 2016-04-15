package com.topface.topface.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by ppavlik on 15.04.16.
 * Data class for ReportPaymentRequest
 */
public class ReportPaymentData {
    @SerializedName("result")
    private boolean mResult;

    public boolean isSuccess() {
        return mResult;
    }
}

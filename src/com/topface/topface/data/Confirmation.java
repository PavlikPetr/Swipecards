package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Confirmation extends AbstractData {

    public boolean completed;

    public Confirmation(ApiResponse response) {
        Confirmation.parse(response, this);
    }

    public static Confirmation parse(ApiResponse response) {
        return new Confirmation(response);
    }

    private static Confirmation parse(ApiResponse response, Confirmation confirm) {
        try {
            confirm.completed = response.jsonResult.optBoolean("completed");
        } catch (Exception e) {
            Debug.log("Completed.class", "Wrong response parsing: " + e);
        }

        return confirm;
    }
}
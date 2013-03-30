package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;

public class Register extends AbstractData{

    public static final String FIELD_USER_ID = "user_id";
    private int mUserId;

    public Register(ApiResponse response) {
        if(response != null && response.jsonResult != null) {
            mUserId = response.jsonResult.optInt(FIELD_USER_ID);
        }
    }

   public int getUserId() {
       return mUserId;
   }
}

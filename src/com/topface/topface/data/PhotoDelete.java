package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class PhotoDelete extends AbstractData {
    // Data
    public boolean completed;
    //---------------------------------------------------------------------------
    public static PhotoDelete parse(ApiResponse response) {
        PhotoDelete delete = new PhotoDelete();

        try {
            delete.completed = response.mJSONResult.optBoolean("completed");
        } catch(Exception e) {
            Debug.log("PhotoDelete.class", "Wrong response parsing: " + e);
        }

        return delete;
    }
    //---------------------------------------------------------------------------
}

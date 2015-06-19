package com.topface.topface.data;

import android.content.Intent;

/**
 * Created by ppetr on 19.06.15.
 * hold all data from onActivityResult
 */
public class ActivityResultData {
    public int requestCode;
    public int resultCode;
    public Intent data;

    public ActivityResultData(int requestCode, int resultCode, Intent data) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
    }
}

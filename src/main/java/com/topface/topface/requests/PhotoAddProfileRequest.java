package com.topface.topface.requests;

import android.content.Context;
import android.net.Uri;

import com.topface.topface.App;
import com.topface.topface.Ssid;
import com.topface.topface.utils.IProgressListener;

/**
 * Created by kirussell on 19/01/15.
 * Request to add photo to profile
 *
 */
public class PhotoAddProfileRequest extends PhotoAddRequest {

    public PhotoAddProfileRequest(Uri uri, Context context, IProgressListener listener) {
        super(uri, context, listener);
    }

    @Override
    public String getApiUrl() {
        return App.getAppConfig().getApiDomain() + "v" + API_VERSION + "/photo-upload/profile?ssid=" + Ssid.get();
    }

    @Override
    protected String getPlaceForStatistics() {
        return "Profile";
    }
}

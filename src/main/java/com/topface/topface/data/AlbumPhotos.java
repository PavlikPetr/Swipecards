package com.topface.topface.data;

import com.topface.framework.JsonUtils;
import com.topface.topface.requests.ApiResponse;

/**
 * Created by kirussell on 05.02.14.
 * {@link com.topface.topface.requests.AlbumRequest}
 */
public class AlbumPhotos extends Photos {

    public final boolean more;

    public AlbumPhotos(ApiResponse response) {
        super(JsonUtils.fromJson(response.getJsonResult().optJSONArray("items").toString(), Photos.class));
        more = response.getJsonResult().optBoolean("more");
    }
}

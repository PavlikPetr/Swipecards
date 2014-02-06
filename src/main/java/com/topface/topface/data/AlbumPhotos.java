package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;

/**
 * Created by kirussell on 05.02.14.
 * {@link com.topface.topface.requests.AlbumRequest}
 */
public class AlbumPhotos extends Photos {

    public final boolean more;
    @SuppressWarnings("unused")
    private final int count;

    public AlbumPhotos(ApiResponse response) {
        super(response.getJsonResult().optJSONArray("items"));
        more = response.getJsonResult().optBoolean("more");
        count = response.getJsonResult().optInt("count");
    }
}

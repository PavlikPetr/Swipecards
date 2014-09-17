package com.topface.topface.utils.loadcontollers;

import android.content.Context;
import android.content.res.Resources;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.receivers.ConnectionChangeReceiver;

import java.util.HashMap;

import static com.topface.topface.receivers.ConnectionChangeReceiver.*;
import static com.topface.topface.receivers.ConnectionChangeReceiver.ConnectionType.*;

/**
 * Created by ilya on 12.09.14.
 */
public class AlbumLoadController extends LoadController{

    public static final int FOR_GALLERY = 0;
    public static final int FOR_PREVIEW = 1;

    private int mType;

    public AlbumLoadController(int type) {
        mType = type;
        init();
    }

    @Override
    protected int[] getPreloadLimits() {
        Resources resources = App.getContext().getResources();
        switch (mType) {
            case FOR_PREVIEW:
                return resources.getIntArray(R.array.album_preview_limit);
            case FOR_GALLERY:
                return resources.getIntArray(R.array.album_gallery_limit);
        }
        return null;
    }

    @Override
    protected int[] getPreloadOffset() {
        Resources resources = App.getContext().getResources();
        switch (mType) {
            case FOR_PREVIEW:
                return resources.getIntArray(R.array.album_preview_offset);
            case FOR_GALLERY:
                return resources.getIntArray(R.array.album_gallery_offset);
        }
        return null;
    }
}

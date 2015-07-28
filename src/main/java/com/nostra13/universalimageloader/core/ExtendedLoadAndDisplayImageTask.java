package com.nostra13.universalimageloader.core;

import android.os.Handler;

import com.nostra13.universalimageloader.utils.IoUtils;
import com.nostra13.universalimageloader.utils.L;
import com.topface.topface.utils.IRequestConnectionListener;
import com.topface.topface.utils.RequestConnectionListenerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by kirussell on 26.04.2014.
 * Extended LoadAndDisplayImageTask to inject some statistics events tracking
 */
public class ExtendedLoadAndDisplayImageTask extends LoadAndDisplayImageTask {

    public static final String SERVICE_NAME = "imgDwn";

    private final ImageLoaderConfiguration configuration;
    private final String memoryCacheKey;

    public ExtendedLoadAndDisplayImageTask(ImageLoaderEngine engine, ImageLoadingInfo imageLoadingInfo, Handler handler) {
        super(engine, imageLoadingInfo, handler);
        this.configuration = engine.configuration;
        this.memoryCacheKey = imageLoadingInfo.memoryCacheKey;
    }

    private boolean downloadImage() throws IOException {
        IRequestConnectionListener listener = RequestConnectionListenerFactory.create(SERVICE_NAME);
        listener.onConnectionStarted();
        listener.onConnectInvoked();
        InputStream is = this.getDownloader().getStream(this.uri, this.options.getExtraForDownloader());
        listener.onConnectionEstablished();
        if (is == null) {
            L.e("No stream for image [%s]", new Object[]{this.memoryCacheKey});
            return false;
        } else {
            boolean var2;
            try {
                var2 = this.configuration.diskCache.save(this.uri, is, this);
            } finally {
                IoUtils.closeSilently(is);
                listener.onConnectionClose();
            }

            return var2;
        }
    }
}

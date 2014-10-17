package com.nostra13.universalimageloader.core;

import android.os.Handler;

import com.nostra13.universalimageloader.utils.IoUtils;
import com.topface.topface.utils.IRequestConnectionListener;
import com.topface.topface.utils.RequestConnectionListenerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by kirussell on 26.04.2014.
 * Extended LoadAndDisplayImageTask to inject some statistics events tracking
 */
public class ExtendedLoadAndDisplayImageTask extends LoadAndDisplayImageTask {

    public static final String SERVICE_NAME = "imgDwn";

    public ExtendedLoadAndDisplayImageTask(ImageLoaderEngine engine, ImageLoadingInfo imageLoadingInfo, Handler handler) {
        super(engine, imageLoadingInfo, handler);
    }

    @Override
    protected boolean downloadImage(File targetFile) throws IOException {
        IRequestConnectionListener listener = new RequestConnectionListenerFactory().create(SERVICE_NAME);
        listener.onConnectionStarted();
        listener.onConnectInvoked();
        InputStream is = getDownloader().getStream(uri, options.getExtraForDownloader());
        listener.onConnectionEstablished();
        boolean loaded;
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile), BUFFER_SIZE);
            try {
                loaded = IoUtils.copyStream(is, os, this);
                listener.onConnectionClose();
            } finally {
                IoUtils.closeSilently(os);
            }
        } finally {
            IoUtils.closeSilently(is);
        }
        return loaded;
    }
}

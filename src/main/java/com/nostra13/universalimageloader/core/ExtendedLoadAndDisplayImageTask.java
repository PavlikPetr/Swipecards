package com.nostra13.universalimageloader.core;

import android.os.Handler;
import com.nostra13.universalimageloader.utils.IoUtils;
import com.topface.statistics.TfStatConsts;
import com.topface.statistics.android.Slices;
import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.utils.Connectivity;

import java.io.*;

/**
 * Created by kirussell on 26.04.2014.
 */
public class ExtendedLoadAndDisplayImageTask extends LoadAndDisplayImageTask {

    public static final String SERVICE_NAME = "imgDwn";

    public ExtendedLoadAndDisplayImageTask(ImageLoaderEngine engine, ImageLoadingInfo imageLoadingInfo, Handler handler) {
        super(engine, imageLoadingInfo, handler);
    }

    @Override
    protected boolean downloadImage(File targetFile) throws IOException {
        StatisticsTracker tracker = StatisticsTracker.getInstance();
        Slices slices = new Slices()
                .putSlice(TfStatConsts.con, TfStatConsts.getConnType(Connectivity.getConnType(App.getContext())))
                .putSlice(TfStatConsts.mtd, TfStatConsts.getMtd(SERVICE_NAME));
        long startTimestamp = System.currentTimeMillis();
        InputStream is = getDownloader().getStream(uri, options.getExtraForDownloader());
        long connEstablishedTime = System.currentTimeMillis();
        tracker.sendEvent(
                TfStatConsts.api_connect_time,
                slices.putSlice(TfStatConsts.val, TfStatConsts.getConnTimeVal(connEstablishedTime - startTimestamp))
        );
        boolean loaded;
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(targetFile), BUFFER_SIZE);
            try {
                loaded = IoUtils.copyStream(is, os, this);
                long endResponseReadTime = System.currentTimeMillis();
                tracker.sendEvent(
                        TfStatConsts.api_load_time,
                        slices.putSlice(TfStatConsts.val, TfStatConsts.getConnTimeVal(endResponseReadTime - connEstablishedTime))
                );
                tracker.sendEvent(
                        TfStatConsts.api_request_time,
                        slices.putSlice(TfStatConsts.val, TfStatConsts.getRequestTimeVal(endResponseReadTime - startTimestamp))
                );
            } finally {
                IoUtils.closeSilently(os);
            }
        } finally {
            IoUtils.closeSilently(is);
        }
        return loaded;
    }
}

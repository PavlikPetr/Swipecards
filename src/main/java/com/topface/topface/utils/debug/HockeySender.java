package com.topface.topface.utils.debug;

import android.content.Context;

import com.topface.topface.utils.CacheProfile;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HockeySender implements ReportSender {
    public static String BASE_URL = "https://rink.hockeyapp.net/api/2/apps/";
    public static String CRASHES_PATH = "/crashes";
    private final boolean mIsDebug;

    public HockeySender(boolean debug) {
        mIsDebug = debug;
    }

    private String createCrashLog(CrashReportData report) {
        Date now = new Date();
        StringBuilder log = new StringBuilder();

        log.append("Package: ").append(report.get(ReportField.PACKAGE_NAME)).append("\n");
        log.append("Version: ").append(getVersion(report)).append("\n");
        log.append("Android: ").append(report.get(ReportField.ANDROID_VERSION)).append("\n");
        log.append("Manufacturer: ").append(android.os.Build.MANUFACTURER).append("\n");
        log.append("Model: ").append(report.get(ReportField.PHONE_MODEL)).append("\n");
        log.append("Date: ").append(now).append("\n");
        log.append("\n");
        log.append(report.get(ReportField.STACK_TRACE));

        return log.toString();
    }

    private String getVersion(CrashReportData report) {
        return mIsDebug ? report.get(ReportField.APP_VERSION_NAME) : report.get(ReportField.APP_VERSION_CODE);
    }

    @Override
    public void send(Context context, CrashReportData report) throws ReportSenderException {
        String log = createCrashLog(report);
        String url = BASE_URL + ACRA.getConfig().formUri() + CRASHES_PATH;

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            List<NameValuePair> parameters = new ArrayList<>();
            parameters.add(new BasicNameValuePair("raw", log));
            String uid;
            if (CacheProfile.uid > 0) {
                uid = Integer.toString(CacheProfile.uid);
            } else {
                uid = report.get(ReportField.INSTALLATION_ID);
            }
            parameters.add(new BasicNameValuePair("userID", uid));
            parameters.add(new BasicNameValuePair("contact", report.get(ReportField.USER_EMAIL)));
            parameters.add(new BasicNameValuePair("description", report.get(ReportField.USER_COMMENT)));

            httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

            httpClient.execute(httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
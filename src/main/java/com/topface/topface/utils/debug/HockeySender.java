package com.topface.topface.utils.debug;

import android.content.Context;
import android.os.Build;

import com.topface.framework.utils.Debug;
import com.topface.topface.BuildConfig;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HockeySender implements ReportSender {
    public static String BASE_URL = "https://rink.hockeyapp.net/api/2/apps/";
    public static String CRASHES_PATH = "/crashes";

    private String createCrashLog(CrashReportData report) {
        Date now = new Date();
        StringBuilder log = new StringBuilder();
        log.append("Package: ").append(report.get(ReportField.PACKAGE_NAME)).append("\n");
        log.append("Version: ").append(report.get(ReportField.APP_VERSION_CODE)).append("\n");
        log.append("Android: ").append(report.get(ReportField.ANDROID_VERSION)).append("\n");
        log.append("Manufacturer: ").append(android.os.Build.MANUFACTURER).append("\n");
        log.append("Model: ").append(report.get(ReportField.PHONE_MODEL)).append("\n");
        log.append("Date: ").append(now).append("\n");
        log.append("\n");
        log.append(report.get(ReportField.STACK_TRACE));
        return log.toString();
    }

    public CrashReportData createLocalReport(Context context, Exception ex) {
        CrashReportData report = new CrashReportData();
        report.put(ReportField.PACKAGE_NAME, context.getPackageName());
        report.put(ReportField.APP_VERSION_CODE, Integer.toString(BuildConfig.VERSION_CODE));
        report.put(ReportField.ANDROID_VERSION, Build.VERSION.RELEASE);
        report.put(ReportField.PHONE_MODEL, Build.DEVICE);
        report.put(ReportField.INSTALLATION_ID, "unknown");
        if (ex != null && ex.getStackTrace() != null) {
            StringWriter writer = new StringWriter();
            ex.printStackTrace(new PrintWriter(writer));
            report.put(ReportField.STACK_TRACE, writer.toString());
        }
        return report;
    }

    @Override
    public void send(Context context, CrashReportData report) throws ReportSenderException {
        send(report);
    }

    public void send(CrashReportData report) {
        String log = createCrashLog(report);
        Debug.log("HockeyAppSender", log);
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
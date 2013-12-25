package com.topface.topface.utils.debug;

import android.content.Context;
import android.content.Intent;

import com.topface.topface.utils.CacheProfile;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.PRODUCT;
import static org.acra.ReportField.SHARED_PREFERENCES;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_COMMENT;
import static org.acra.ReportField.USER_CRASH_DATE;

public class DebugEmailSender implements ReportSender {
    private final Context mContext;

    public DebugEmailSender(Context ctx) {
        mContext = ctx;
    }


    @Override
    public void send(CrashReportData errorContent) throws ReportSenderException {

        final String subject = "[Crash Report] [uid: " + CacheProfile.uid + "] Topface";

        final String body = buildBody(errorContent);

        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ACRA.getConfig().mailTo()});
        mContext.startActivity(emailIntent);
    }

    private String buildBody(CrashReportData errorContent) {
        errorContent.put(
                PHONE_MODEL,
                errorContent.get(BRAND) +
                        " " + errorContent.get(PHONE_MODEL) +
                        " (" + errorContent.get(PRODUCT) + ")"
        );


        ReportField[] fields = {APP_VERSION_CODE, APP_VERSION_NAME,
                PHONE_MODEL, PRODUCT, ANDROID_VERSION,
                USER_APP_START_DATE,
                USER_CRASH_DATE, USER_COMMENT, STACK_TRACE, LOGCAT,
                SHARED_PREFERENCES
        };

        final StringBuilder builder = new StringBuilder();
        for (ReportField field : fields) {
            builder.append(field.toString()).append("=");
            builder.append(errorContent.get(field));
            builder.append('\n');
        }
        return builder.toString();
    }
}

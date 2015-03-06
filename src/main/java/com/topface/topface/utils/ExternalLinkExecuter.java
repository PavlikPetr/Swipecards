package com.topface.topface.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.topface.topface.R;
import com.topface.topface.statistics.RedirectStatistics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalLinkExecuter {

    private OnExternalLinkListener listener;

    public ExternalLinkExecuter(OnExternalLinkListener listener) {
        this.listener = listener;
    }

    public void execute(Context context, Intent intent) {
        if (intent != null) {

            Uri data = intent.getData();
            if (data != null) {
                String scheme = data.getScheme();
                if (TextUtils.equals(context.getString(R.string.default_sheme), scheme)) {
                    String host = data.getHost();
                    if (TextUtils.equals(context.getString(R.string.offerwall_host), host)) {
                        listener.onOfferWall();
                    } else {
                        RedirectStatistics.send(host);
                    }
                } else {
                    if (checkHost(data)) {

                        String path = data.getPath();
                        String[] splittedPath = path.split("/");

                        executeLinkAction(splittedPath);
                    }
                }
            }
        }
    }

    private boolean checkHost(Uri data) {
        String host = data != null ? data.getHost() : null;
        if (host != null) {
            Pattern topfacePattern = Pattern.compile(".*topface\\.ru|.*topface\\.com");
            return topfacePattern.matcher(host).matches();
        }
        return false;
    }

    private void executeLinkAction(String[] splittedPath) {
        Pattern profilePattern = Pattern.compile("profile");
        Pattern confirmPattern = Pattern.compile("confirm.*");

        if (profilePattern.matcher(splittedPath[1]).matches() && splittedPath.length >= 3) {
            listener.onProfileLink(Integer.parseInt(splittedPath[2]));
        } else if (confirmPattern.matcher(splittedPath[1]).matches()) {

            Pattern codePattern = Pattern.compile("[0-9]+-[0-f]+-[0-9]*");
            Matcher matcher = codePattern.matcher(splittedPath[1]);
            if (matcher.find()) {
                String code = matcher.group();
                listener.onConfirmLink(code);
            }

        }
    }

    public interface OnExternalLinkListener {

        public void onProfileLink(int profileID);

        public void onConfirmLink(String code);

        public void onOfferWall();
    }

}

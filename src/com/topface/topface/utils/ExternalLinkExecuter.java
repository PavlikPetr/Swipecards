package com.topface.topface.utils;

import android.content.Intent;
import android.net.Uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternalLinkExecuter {

    private static final String TOPFACE_CHEME = "topface";

    private OnExternalLinkListener listener;

    public ExternalLinkExecuter(OnExternalLinkListener listener) {
        this.listener = listener;
    }

    public void execute(Intent intent) {
        if (intent != null) {

            Uri data = intent.getData();
            if(data != null)  {
                String scheme = data.getScheme();
                if(scheme != null && scheme.equals(TOPFACE_CHEME)) {
                    if (data.getHost().equals("offerwall")) {
                        listener.onOfferWall();
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
        Pattern topfacePattern = Pattern.compile(".*topface\\.ru|.*topface\\.com");
        return data != null && topfacePattern.matcher(data.getHost()).matches();
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

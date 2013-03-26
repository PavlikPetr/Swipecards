package com.topface.topface.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentwallActivity extends BaseFragmentActivity {
    public static final String SUCCESS_URL_PATTERN = "success_url=([^&]+)";
    public static final String USER_ID = "userId";
    public static final int ACTION_BUY = 100;
    private int mUid;
    private static final int RESULT_ERROR = 1;
    private String mUrl = "https://wallapi.com/api/subscription/?key=3b2e96bcaa32b23b34605dfbf51c4df5&uid=[USER_ID]&widget=m2_1&success_url=http://topface.com/paymentwall-success";
    private String mSuccessUrl;
    private View mProgressBar;

    public static Intent getIntent(Context context, int userId) {
        Intent intent = new Intent(context, PaymentwallActivity.class);
        intent.putExtra(USER_ID, userId);
        return intent;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUid = getIntent().getIntExtra(USER_ID, 0);
        mSuccessUrl = getSuccessUrl(mUrl);
        if (mUid == 0 || TextUtils.isEmpty(mSuccessUrl)) {
            Toast.makeText(this, R.string.general_data_error, Toast.LENGTH_SHORT);
            finishActivity(RESULT_ERROR);
            return;
        }

        setContentView(R.layout.ac_web_auth);

        // Progress
        mProgressBar = findViewById(R.id.prsWebLoading);

        // WebView
        WebView webView = (WebView) findViewById(R.id.wvWebFrame);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollbarOverlay(true);
        webView.setVerticalFadingEdgeEnabled(true);
        webView.setWebViewClient(new PaymentwallClient(webView));
    }

    private String getSuccessUrl(String url) {
        String result = null;
        Pattern pattern = Pattern.compile(SUCCESS_URL_PATTERN);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            result = matcher.group(1);
        }

        return result;
    }

    private class PaymentwallClient extends WebViewClient {

        public PaymentwallClient(WebView webView) {
            super();
            webView.loadUrl(getWidgetUrl());
            webView.setBackgroundColor(0x00000000);
            webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Debug.log(String.format("PW: error load page %s %d: %s", failingUrl, errorCode, description));
            finishActivity(RESULT_ERROR);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Debug.log("PW: start load page " + url);
            if (TextUtils.equals(url, mSuccessUrl)) {
                Debug.log("PW: buy is completed " + url);
                finishActivity(Activity.RESULT_OK);
            } else {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private String getWidgetUrl() {
        return mUrl.replace("[USER_ID]", Integer.toString(mUid));
    }


}

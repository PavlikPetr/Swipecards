package com.topface.topface.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentwallActivity extends BaseFragmentActivity {
    public static final String SUCCESS_URL_PATTERN = "success_url=([^&]+)";
    public static final int ACTION_BUY = 100;
    private static final int RESULT_ERROR = 1;
    private static final String TEST_PURCHASE = "testPurchase";
    public static final String PW_URL = "pw_url";
    private String mSuccessUrl;
    private View mProgressBar;
    private boolean mIsTestPurchase;
    private String mWidgetUrl;

    public static Intent getIntent(Context context, boolean testPurchasesEnabled) {
        Intent intent = new Intent(context, PaymentwallActivity.class);
        intent.putExtra(TEST_PURCHASE, testPurchasesEnabled);
        return intent;
    }

    public static Intent getIntent(Context context, boolean testPurchasesEnabled, String url) {
        Intent intent = new Intent(context, PaymentwallActivity.class);
        intent.putExtra(TEST_PURCHASE, testPurchasesEnabled);
        intent.putExtra(PW_URL, url);
        return intent;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsTestPurchase = getIntent().getBooleanExtra(TEST_PURCHASE, false);
        mWidgetUrl = getWidgetUrl();
        if (TextUtils.isEmpty(mWidgetUrl)) {
            onFatalError();
            return;
        }
        mSuccessUrl = getSuccessUrl(mWidgetUrl);
        getSupportActionBar().setTitle(R.string.buying_header_title);

        setContentView(R.layout.ac_web_auth);

        // Progress
        mProgressBar = findViewById(R.id.prsWebLoading);

        // WebView
        WebView webView = (WebView) findViewById(R.id.wvWebFrame);
        //noinspection AndroidLintSetJavaScriptEnabled
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollbarOverlay(true);
        webView.setVerticalFadingEdgeEnabled(true);
        webView.setWebViewClient(new PaymentwallClient(webView));
    }

    private void onFatalError() {
        Toast.makeText(this, R.string.general_data_error, Toast.LENGTH_SHORT).show();
        finishActivity(RESULT_ERROR);
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

    private String getWidgetUrl() {
        String url = getIntent().getStringExtra(PW_URL);
        if (url == null) {
            url = CacheProfile.getOptions().getPaymentwallLink();
        }
        if (!TextUtils.isEmpty(url) && mIsTestPurchase) {
            url += "&test_mode=1";
        }
        return url;
    }

    private class PaymentwallClient extends WebViewClient {

        public PaymentwallClient(WebView webView) {
            super();
            webView.loadUrl(mWidgetUrl);
            webView.setBackgroundColor(0x00000000);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Debug.log(String.format("PW: error load page %s %d: %s", failingUrl, errorCode, description));
            onFatalError();
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
            if (TextUtils.equals(url, mSuccessUrl)) {
                Debug.log("PW: finish buy is completed " + url);
                finishActivity(Activity.RESULT_OK);
            }
            mProgressBar.setVisibility(View.GONE);
        }


    }


}

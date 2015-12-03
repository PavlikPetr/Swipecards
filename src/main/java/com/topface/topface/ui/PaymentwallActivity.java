package com.topface.topface.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.BalanceData;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

public class PaymentwallActivity extends BaseFragmentActivity {
    public static final String SUCCESS_URL_PATTERN = "success_url=([^&]+)";
    public static final int ACTION_BUY = 100;
    public static final String PW_URL = "pw_url";
    private static final int RESULT_ERROR = 1;
    private String mSuccessUrl;
    private View mProgressBar;
    private String mWidgetUrl;
    private TextView mCurCoins;
    private TextView mCurLikes;
    @Inject
    TopfaceAppState mAppState;
    private BalanceData mBalance;
    private Subscription mBalanceSubscription;

    public static Intent getIntent(Context context) {
        return new Intent(context, PaymentwallActivity.class);
    }

    public static Intent getIntent(Context context, String url) {
        Intent intent = new Intent(context, PaymentwallActivity.class);
        intent.putExtra(PW_URL, url);
        return intent;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(this).inject(this);
        mBalanceSubscription = mAppState.getObservable(BalanceData.class).subscribe(new Action1<BalanceData>() {
            @Override
            public void call(BalanceData balanceData) {
                mBalance = balanceData;
                updateBalanceCounters();
            }
        });
        initBalanceCounters();
        mWidgetUrl = getWidgetUrl();
        if (TextUtils.isEmpty(mWidgetUrl)) {
            onFatalError();
            return;
        }
        mSuccessUrl = getSuccessUrl(mWidgetUrl);
        actionBarView.setActionBarTitle(R.string.purchase_header_title);

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

    @Override
    protected int getContentLayout() {
        return R.layout.ac_web_auth;
    }

    private void onFatalError() {
        Utils.showErrorMessage();
        setResult(RESULT_ERROR);
        finish();
    }

    private String getSuccessUrl(String url) {
        String result = null;
        Pattern pattern = Pattern.compile(SUCCESS_URL_PATTERN);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            result = matcher.group(1);
            try {
                //Декодим URL, что бы потом сравнивать с URL WebView
                result = URLDecoder.decode(result, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Debug.error(e);
            }
        }

        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBalanceSubscription != null) {
            mBalanceSubscription.unsubscribe();
        }
    }

    private String getWidgetUrl() {
        String url = getIntent().getStringExtra(PW_URL);
        if (url == null) {
            url = App.from(this).getOptions().getPaymentwallLink();
        }
        return url;
    }

    private class PaymentwallClient extends WebViewClient {

        public PaymentwallClient(WebView webView) {
            super();
            webView.loadUrl(mWidgetUrl);
            setLayerType(webView);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        private void setLayerType(WebView webView) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Debug.log(String.format(Locale.ENGLISH, "PW: error load page %s %d: %s", failingUrl, errorCode, description));
            onFatalError();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Debug.log("PW: start load page " + url);
            if (TextUtils.equals(url, mSuccessUrl)) {
                Debug.log("PW: buy is completed " + url);
                setResult(RESULT_OK);
                finish();

            } else {
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (TextUtils.equals(url, mSuccessUrl)) {
                Debug.log("PW: finish buy is completed " + url);
                setResult(RESULT_OK);
                finish();
            }
            mProgressBar.setVisibility(View.GONE);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBalanceCounters();
    }

    private void initBalanceCounters() {
        final LinearLayout containerView = (LinearLayout) findViewById(R.id.resources_layout);
        containerView.setVisibility(View.VISIBLE);
        containerView.post(new Runnable() {
            @Override
            public void run() {
                int containerWidth = containerView.getMeasuredWidth();
                if (mCurCoins != null && mCurLikes != null) {
                    mCurCoins.setMaxWidth(containerWidth / 2);
                    mCurLikes.setMaxWidth(containerWidth / 2);
                }
            }
        });
        mCurCoins = (TextView) findViewById(R.id.coins_textview);
        mCurLikes = (TextView) findViewById(R.id.likes_textview);
        mCurCoins.setSelected(true);
        mCurLikes.setSelected(true);
        updateBalanceCounters();
    }

    private void updateBalanceCounters() {
        updateBalanceCounters(mBalance);
    }

    private void updateBalanceCounters(BalanceData balance) {
        if (mCurCoins != null && mCurLikes != null && mBalance != null) {
            mCurCoins.setText(Integer.toString(balance.money));
            mCurLikes.setText(Integer.toString(balance.likes));
        }
    }
}

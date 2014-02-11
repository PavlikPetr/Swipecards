package com.topface.topface.utils.offerwalls.supersonicads;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.RelativeLayout;

import com.topface.topface.utils.CacheProfile;

public class SupersonicWallActivity extends Activity {

    private String url = "http://www.supersonicads.com/delivery/mobilePanel.php";
    private static final String API_KEY = "2cf0ad4d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView mWebView = new WebView(this);
        RelativeLayout mRelLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
        );
        mWebView.setLayoutParams(params);
        mRelLayout.addView(mWebView);
        setContentView(mRelLayout);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.postUrl(
                url + "?applicationUserId=" + CacheProfile.uid +
                        "&applicationKey=" + API_KEY +
                        "&deviceOs=android", null
        );
    }



}

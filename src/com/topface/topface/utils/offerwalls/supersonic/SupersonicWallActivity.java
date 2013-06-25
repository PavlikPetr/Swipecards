package com.topface.topface.utils.offerwalls.supersonic;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.offerwalls.clickky.ClickkyOfferWebview;

import java.util.UUID;

public class SupersonicWallActivity extends Activity{

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
        StringBuilder strBuilder = new StringBuilder(url);
        strBuilder.append("?applicationUserId=").append(CacheProfile.uid)
                .append("&applicationKey=").append(API_KEY)
                .append("&deviceOs=android");
        mWebView.postUrl(strBuilder.toString(), null);
    }



}

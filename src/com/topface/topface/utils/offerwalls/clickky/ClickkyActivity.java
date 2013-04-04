package com.topface.topface.utils.offerwalls.clickky;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.RelativeLayout;

public class ClickkyActivity extends Activity{

    private final static int SITE_ID = 3382;
    private final static String API_KEY = "dy70$d8q-x";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //создаем объект класса OfferWebview
        WebView mWebView = new ClickkyOfferWebview(this, SITE_ID, API_KEY);
        //создаем layout, на котором будет располагаться WebView
        RelativeLayout mRelLayout = new RelativeLayout(this);
        //задаем параметры нашему layout
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT
        );
        //устанавливаем параметры нашему объекту OfferWebview
        mWebView.setLayoutParams(params);
        //добавляем наш объект на layout
        mRelLayout.addView(mWebView);
        //устанавливаем UI
        setContentView(mRelLayout);
    }
}

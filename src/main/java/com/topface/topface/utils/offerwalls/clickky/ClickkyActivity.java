package com.topface.topface.utils.offerwalls.clickky;

import android.os.Bundle;
import android.widget.RelativeLayout;

import com.clickky.banner.library.OfferWebview;
import com.clickky.banner.library.WallType;
import com.topface.topface.ui.BaseFragmentActivity;

public class ClickkyActivity extends BaseFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OfferWebview mWebView = new OfferWebview(this, WallType.APP_WALL);
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

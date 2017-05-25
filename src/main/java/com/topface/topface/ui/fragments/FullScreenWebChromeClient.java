package com.topface.topface.ui.fragments;

/**
 * класс для взаимодействия с загруженной в вэбвью страницей
 */

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class FullScreenWebChromeClient extends WebChromeClient {

    private WebChromeClient.CustomViewCallback mFullscreenViewCallback;
    private View mFullScreenView;

    private FrameLayout mContainer;
    private WebView mWebView;

    public FullScreenWebChromeClient(FrameLayout container, WebView webView) {
        super();
        mContainer = container;
        mWebView = webView;
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        onShowCustomView(view, callback);
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        if (mFullScreenView != null) {
            callback.onCustomViewHidden();
        } else {
            mFullScreenView = view;
            mWebView.setVisibility(View.GONE);
            mContainer.setVisibility(View.VISIBLE);
            mContainer.addView(view);
            mFullscreenViewCallback = callback;
        }
    }

    @Override
    public void onHideCustomView() {
        super.onHideCustomView();
        if (mFullScreenView != null) {
            mWebView.setVisibility(View.VISIBLE);
            mFullScreenView.setVisibility(View.GONE);
            mContainer.setVisibility(View.GONE);
            mContainer.removeView(mFullScreenView);
            mFullscreenViewCallback.onCustomViewHidden();
            mFullScreenView = null;
        }
    }

    public void release() {
        mFullscreenViewCallback = null;
        mContainer = null;
        mWebView = null;
        mFullScreenView = null;
    }
}
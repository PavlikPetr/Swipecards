package com.topface.topface.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

import java.util.Locale;

abstract public class WebViewFragment extends BaseFragment {

    private FrameLayout mFullScreenContainer;
    private View mFullScreenView;
    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedTitles(isNeedTitles());
    }

    abstract String getIntegrationUrl();

    abstract boolean isNeedTitles();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return getView(inflater);
    }

    protected View getView(LayoutInflater inflater) {
        View root = inflater.inflate(R.layout.ac_web_auth, null);

        mWebView = (WebView) root.findViewById(R.id.wvWebFrame);
        mFullScreenContainer = (FrameLayout) root.findViewById(R.id.fullscreen_container);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setVerticalScrollbarOverlay(true);
        mWebView.setWebViewClient(new LoaderClient(mWebView));
        return root;
    }

    @Override
    public boolean onBackPressed() {
        if (mWebView != null) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @SuppressWarnings("unused")
    private Options.Offerwalls.Offer getFakeTfOfferwall() {
        Options.Offerwalls.Offer offer = new Options.Offerwalls.Offer();
        offer.action = OfferwallsManager.TFOFFERWALL;
        offer.text = "tf offerwall";
        return offer;
    }

    private final WebChromeClient mWebChromeClient = new WebChromeClient() {
        private WebChromeClient.CustomViewCallback mFullscreenViewCallback;

        @SuppressWarnings("deprecation")
        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            onShowCustomView(view, callback);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (mFullScreenView != null) {
                callback.onCustomViewHidden();
                return;
            }

            mFullScreenView = view;
            mWebView.setVisibility(View.GONE);

            mFullScreenContainer.setVisibility(View.VISIBLE);
            mFullScreenContainer.addView(view);
            mFullscreenViewCallback = callback;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mFullScreenView == null) {
                return;
            }
            mWebView.setVisibility(View.VISIBLE);
            mFullScreenView.setVisibility(View.GONE);
            mFullScreenContainer.setVisibility(View.GONE);
            mFullScreenContainer.removeView(mFullScreenView);
            mFullscreenViewCallback.onCustomViewHidden();
            mFullScreenView = null;
        }
    };

    private class LoaderClient extends WebViewClient {

        public LoaderClient(WebView webView) {
            super();
            webView.loadUrl(getIntegrationUrl());
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Debug.log(String.format(Locale.ENGLISH, "PW: error load page %s %d: %s", failingUrl, errorCode, description));
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Debug.log("PW: start load page " + url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }
}

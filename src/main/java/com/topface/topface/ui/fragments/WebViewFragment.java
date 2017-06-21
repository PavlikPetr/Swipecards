package com.topface.topface.ui.fragments;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.databinding.WebViewFragmentBinding;

import java.util.Locale;

abstract public class WebViewFragment extends BaseFragment {

    private WebViewFragmentBinding mBinding;
    private FullScreenWebChromeClient mFullScreenWebChromeClient;

    public abstract String getIntegrationUrl();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return getView(inflater);
    }

    protected View getView(LayoutInflater inflater) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.web_view_fragment, null, false);
        mFullScreenWebChromeClient = new FullScreenWebChromeClient(mBinding.fullscreenContainer, mBinding.wvWebFrame);
        WebView webView = mBinding.wvWebFrame;
        webView.setWebChromeClient(mFullScreenWebChromeClient);
        prepareWebView(webView, new LoaderClient(webView));
        return mBinding.getRoot();
    }

    public static void prepareWebView(WebView view, WebViewClient client) {
        WebSettings webSettings = view.getSettings();
        view.setInitialScale(1);
        view.setVerticalScrollbarOverlay(true);
        view.setWebViewClient(client);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);
    }

    @Override
    public boolean onBackPressed() {
        if (mBinding != null) {
            if (mBinding.wvWebFrame.canGoBack()) {
                mBinding.wvWebFrame.goBack();
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
        if (mBinding != null) {
            mBinding.wvWebFrame.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mBinding != null) {
            mBinding.wvWebFrame.onResume();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
        if (mFullScreenWebChromeClient != null) {
            mFullScreenWebChromeClient.release();
        }

    }

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

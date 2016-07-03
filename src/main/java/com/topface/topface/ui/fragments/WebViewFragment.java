package com.topface.topface.ui.fragments;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.databinding.WebViewFragmentBinding;

import java.util.Locale;

abstract public class WebViewFragment extends BaseFragment {

    private View mFullScreenView;
    private WebViewFragmentBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedTitles(isNeedTitles());
    }

    public abstract String getIntegrationUrl();

    public abstract boolean isNeedTitles();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return getView(inflater);
    }

    protected View getView(LayoutInflater inflater) {
        mBinding = DataBindingUtil.bind(inflater.inflate(R.layout.web_view_fragment, null));
        mBinding.wvWebFrame.setWebChromeClient(mWebChromeClient);
        mBinding.wvWebFrame.getSettings().setJavaScriptEnabled(true);
        mBinding.wvWebFrame.setVerticalScrollbarOverlay(true);
        mBinding.wvWebFrame.setWebViewClient(new LoaderClient(mBinding.wvWebFrame));
        return mBinding.getRoot();
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
            mBinding.wvWebFrame.setVisibility(View.GONE);
            mBinding.fullscreenContainer.setVisibility(View.VISIBLE);
            mBinding.fullscreenContainer.addView(view);
            mFullscreenViewCallback = callback;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mFullScreenView == null) {
                return;
            }
            mBinding.wvWebFrame.setVisibility(View.VISIBLE);
            mFullScreenView.setVisibility(View.GONE);
            mBinding.fullscreenContainer.setVisibility(View.GONE);
            mBinding.fullscreenContainer.removeView(mFullScreenView);
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

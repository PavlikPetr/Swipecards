package com.topface.topface.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

import java.util.Locale;

abstract public class WebViewFragment extends BaseFragment {

    private View mProgressBar;

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
        // Progress
        mProgressBar = root.findViewById(R.id.prsWebLoading);

        // WebView
        WebView webView = (WebView) root.findViewById(R.id.wvWebFrame);
        //noinspection AndroidLintSetJavaScriptEnabled
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVerticalScrollbarOverlay(true);
        webView.setVerticalFadingEdgeEnabled(true);
        webView.setWebViewClient(new LoaderClient(webView));

        return root;
    }

    @SuppressWarnings("unused")
    private Options.Offerwalls.Offer getFakeTfOfferwall() {
        Options.Offerwalls.Offer offer = new Options.Offerwalls.Offer();
        offer.action = OfferwallsManager.TFOFFERWALL;
        offer.text = "tf offerwall";
        return offer;
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
            mProgressBar.setVisibility(View.GONE);
        }


    }
}

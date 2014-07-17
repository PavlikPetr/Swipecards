package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.offerwall.TFOfferwallSDK;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

public class BonusFragment extends BaseFragment {

    public static final String NEED_SHOW_TITLE = "need_show_title";
    private View mProgressBar;
    private String mIntegrationUrl;

    public static BonusFragment newInstance(boolean needShowTitle) {
        BonusFragment fragment = new BonusFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(NEED_SHOW_TITLE, needShowTitle);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            setNeedTitles(args.getBoolean(NEED_SHOW_TITLE));
        }
        OfferwallsManager.init(getActivity());
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_bonus);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root;

        mIntegrationUrl = CacheProfile.getOptions().bonus.integrationUrl;
        if (!TextUtils.isEmpty(mIntegrationUrl)) {
            root = getIntegrationWebView(inflater);
        } else {
            root = getOfferwallView(inflater);
        }
        return root;
    }

    private View getIntegrationWebView(LayoutInflater inflater) {
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

    private View getOfferwallView(LayoutInflater inflater) {
        View root = inflater.inflate(R.layout.fragment_bonus, null);
        Options.Offerwalls offerwalls = CacheProfile.getOptions().offerwalls;
        // main offerwalls - blue buttons
        ((TextView) root.findViewById(R.id.tvOfferMain)).setText(offerwalls.mainText);
        ViewGroup mainOffersContainer = (ViewGroup) root.findViewById(R.id.loContainerMain);
        for (Options.Offerwalls.Offer offer : offerwalls.mainOffers) {
            mainOffersContainer.addView(createButton(getActivity(), offer));
        }
        // extra offerwalls - gray buttons
        TextView extraTitle = ((TextView) root.findViewById(R.id.tvOfferExtra));
        ViewGroup extraOffersContainer = (ViewGroup) root.findViewById(R.id.loContainerExtra);
        if (offerwalls.extraText != null) {
            extraTitle.setText(offerwalls.extraText);
        } else {
            extraTitle.setVisibility(View.GONE);
            extraOffersContainer.setVisibility(View.GONE);
        }
        for (Options.Offerwalls.Offer offer : offerwalls.extraOffers) {
            extraOffersContainer.addView(createButton(getActivity(), offer));
        }
        return root;
    }

    /**
     * Creates button specified by offer object: styled and with appropriate click listener
     *
     * @param activity current context
     * @param offer    offer from Options
     * @return button obj
     */
    private static Button createButton(final Activity activity, final Options.Offerwalls.Offer offer) {
        int style;
        switch (offer.type) {
            case Options.Offerwalls.Offer.TYPE_MAIN:
                style = R.attr.blueButtonStyle;
                break;
            case Options.Offerwalls.Offer.TYPE_EXTRA:
                style = R.attr.grayButtonStyle;
                break;
            default:
                style = R.attr.grayButtonStyle;
                break;
        }
        return createButton(activity, style, offer.text, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OfferwallsManager.startOfferwall(activity, OfferwallsManager.TFOFFERWALL);
            }
        });
    }

    /**
     * Creates styled button with appropriate margins
     *
     * @param context  current context
     * @param defStyle style to apply to button
     * @param text     will be set to button
     * @param listener click listener for button
     * @return button obj
     */
    private static Button createButton(Context context, int defStyle, String text, View.OnClickListener listener) {
        Button btn = new Button(context, null, defStyle);
        LinearLayout.LayoutParams params = new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, Utils.getPxFromDp(12));
        btn.setLayoutParams(params);
        btn.setText(text);
        btn.setOnClickListener(listener);
        return btn;
    }

    private class LoaderClient extends WebViewClient {

        public LoaderClient(WebView webView) {
            super();
            webView.loadUrl(mIntegrationUrl);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Debug.log(String.format("PW: error load page %s %d: %s", failingUrl, errorCode, description));
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

package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.AdsSettings;
import com.topface.topface.databinding.OwnFullscreenLayoutBinding;
import com.topface.topface.statistics.AdStatistics;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.buy.GpPurchaseActivity;
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.Nullable;

/**
 * Попап для наших фулскринов
 * Created by tiberal on 17.06.16.
 */
public class OwnFullscreenPopup extends BaseDialog implements View.OnClickListener {

    public static final String FULLSCREEN_OPTIONS = "fullscreen_options";
    public static final String IMPROVED_BANNER_TOPFACE = "own_improved_custom_topface_banner";
    public static final String TAG = "OwnFullscreenPopup";
    @Nullable
    private AdsSettings mAdsSettings;
    public static final String SCREEN_TYPE = "OwnFullscreenPopup";

    public static OwnFullscreenPopup newInstance(AdsSettings adsSettings) {
        Bundle args = new Bundle();
        args.putParcelable(FULLSCREEN_OPTIONS, adsSettings);
        OwnFullscreenPopup fragment = new OwnFullscreenPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void parseArgs(@Nullable Bundle bundle) {
        if (bundle != null) {
            mAdsSettings = bundle.getParcelable(FULLSCREEN_OPTIONS);
        }
    }

    @Override
    protected void initViews(View root) {
        OwnFullscreenLayoutBinding binding = DataBindingUtil.bind(root);
        View bodyView = createBodyView();
        if (bodyView != null) {
            binding.content.addView(bodyView);
        } else {
            getDialog().cancel();
        }
        binding.setClick(this);
    }

    @Override
    public void onViewStateRestored(@android.support.annotation.Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mAdsSettings = savedInstanceState.getParcelable(FULLSCREEN_OPTIONS);
        }
    }

    @Nullable
    private View createBodyView() {
        if (mAdsSettings != null && !mAdsSettings.isEmpty()) {
            switch (mAdsSettings.banner.type) {
                case AdsSettings.IMG:
                    View view = new ImageViewRemote(getContext().getApplicationContext());
                    view.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    ((ImageViewRemote) view).setRemoteSrc(Utils.prepareUrl(mAdsSettings.banner.url));
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            clickImgBannerSettings(mAdsSettings);
                            AdStatistics.sendFullscreenClicked(OwnFullscreenPopup.IMPROVED_BANNER_TOPFACE);
                            OwnFullscreenPopup.this.cancel();
                        }
                    });
                    return view;
                case AdsSettings.WEB:
                    WebView webView = new WebView(getContext().getApplicationContext());
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadUrl(Utils.prepareUrl(mAdsSettings.banner.url));
                    return webView;
            }
        }
        return null;
    }

    private void clickImgBannerSettings(AdsSettings settings) {
        switch (settings.banner.action) {
            case AdsSettings.PAGE:
                switch (settings.banner.parameter) {
                    case AdsSettings.PURCHASE:
                        startActivity(PurchasesActivity.createBuyingIntent(SCREEN_TYPE, App.get().getOptions().topfaceOfferwallRedirect));
                        break;
                    case AdsSettings.VIP:
                        startActivity(PurchasesActivity.createVipBuyIntent(null, SCREEN_TYPE));
                        break;
                }
                break;
            case AdsSettings.URL:
                Utils.goToUrl(getActivity(), settings.banner.parameter);
                break;
            case AdsSettings.PRODUCT:
                FeedNavigator mFeedNavigator = new FeedNavigator((IActivityDelegate) getActivity());
                mFeedNavigator.showPurchaseProduct(settings.banner.parameter, SCREEN_TYPE);
                break;
            case AdsSettings.METHOD:
                //прост
                break;
            case AdsSettings.OFFERWALL:
                //прост
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FULLSCREEN_OPTIONS, mAdsSettings);
    }

    @Override
    protected int getDialogLayoutRes() {
        return R.layout.own_fullscreen_layout;
    }

    @Override
    protected int getDialogStyleResId() {
        return R.style.Theme_Topface_NoActionBar;
    }

    @Override
    public void onClick(View v) {
        cancel();
    }

    private void cancel() {
        getDialog().cancel();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GpPurchaseActivity.ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            dismiss();
        }
    }
}

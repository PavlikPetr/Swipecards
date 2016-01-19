package com.topface.topface.banners.ad_providers;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.data.Banner;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.statistics.TopfaceAdStatistics;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.buy.PurchasesConstants;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Device;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.offerwalls.OfferwallsManager;

/**
 * Created by kirussell on 12/01/15.
 * TF banners
 */
class TopfaceBannerProvider extends AbstractAdsProvider {

    public static final String CLICK = "click";
    public static final String VIEW = "view";

    @Override
    public final boolean injectBannerInner(final IPageWithAds page, final IAdProviderCallbacks callbacks) {
        if (!isCorrectResolution(page)) {
            return false;
        }
        ViewGroup container = page.getContainerForAd();
        final ImageViewRemote adView = (ImageViewRemote) View
                .inflate(container.getContext(), R.layout.banner_topface, container)
                .findViewById(R.id.tfBannerView);
        createTopfaceBannerRequest(page, adView, callbacks).exec();
        return true;
    }

    /**
     * Показываем баннер на всех устройствах, кроме устройств с маленьким экраном
     */
    private boolean isCorrectResolution(IPageWithAds page) {
        int screenSize = (page.getActivity().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK);
        return screenSize != Configuration.SCREENLAYOUT_SIZE_SMALL;
    }

    private BannerRequest createTopfaceBannerRequest(final IPageWithAds page, final ImageViewRemote adView,
                                                     final IAdProviderCallbacks callbacks) {
        BannerRequest bannerRequest = new BannerRequest(page.getActivity());
        bannerRequest.place = page.getPageName().getName();
        bannerRequest.callback(new DataApiHandler<Banner>() {

            @Override
            protected void success(Banner topfaceBanner, IApiResponse response) {
                if (adView != null) {
                    try {
                        callbacks.onAdLoadSuccess(adView);
                        displayBanner(adView, page, topfaceBanner);
                        adView.setOnClickListener(new ActionsOnClickListener(topfaceBanner, page));
                        sendStat(topfaceBanner, VIEW);
                    } catch (Exception e) {
                        Debug.error(e);
                    }
                }
            }

            @Override
            protected Banner parseResponse(ApiResponse response) {
                return new Banner(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                callbacks.onFailedToLoadAd();
            }
        });
        return bannerRequest;
    }

    private void displayBanner(final ImageViewRemote adView, final IPageWithAds mPage,
                               final Banner topfaceBanner) {
        // Resets ImageView size and drawable from previous banner
        ViewGroup.LayoutParams params = adView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        adView.setLayoutParams(params);
        adView.setImageDrawable(null);
        // Loading image with given url
        adView.setRemoteSrc(topfaceBanner.url, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == ImageViewRemote.LOADING_COMPLETE && mPage.isAdded()) {
                    ViewGroup.LayoutParams newParams = getNewLayoutParams(msg.arg1, msg.arg2,
                            adView.getImageMaxHeight(), adView.getLayoutParams());
                    if (newParams != null) {
                        adView.setLayoutParams(newParams);
                        adView.invalidate();
                    }
                }
            }
        });
    }

    private ViewGroup.LayoutParams getNewLayoutParams(float imageW, float imageH, int maxH,
                                                      ViewGroup.LayoutParams adViewParams) {
        float deviceWidth = Device.getDisplayMetrics(App.getContext()).widthPixels;
        // Если ширина экрана больше, чем у нашего баннера, то пропорционально
        // увеличиваем высоту imageView
        if (deviceWidth > imageW) {
            int scaledHeight = (int) ((deviceWidth / imageW) * imageH);
            if (maxH > scaledHeight) {
                adViewParams.height = scaledHeight;
            } else {
                adViewParams.height = maxH;
                adViewParams.width = (int) ((maxH * imageW) / imageH);
            }
            return adViewParams;
        }
        return null;
    }

    private void sendStat(Banner banner, String label) {
        String action = banner.name;
        if (TextUtils.equals(label, CLICK)) {
            EasyTracker.sendEvent("Banner", action, label, 1L);
            TopfaceAdStatistics.sendBannerClicked(banner);
        } else {
            EasyTracker.sendEvent("Banner", action, label, 0L);
            TopfaceAdStatistics.sendBannerShown(banner);
        }
    }

    private class ActionsOnClickListener implements View.OnClickListener {

        private final Banner mBanner;
        private final IPageWithAds mPage;

        ActionsOnClickListener(Banner banner, IPageWithAds page) {
            mBanner = banner;
            mPage = page;
        }

        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (mBanner.action) {
                case Banner.ACTION_PAGE:
                    EasyTracker.sendEvent("Purchase", "Banner", "", 0L);
                    intent = new Intent(mPage.getActivity(), PurchasesActivity.class);
                    if (mBanner.parameter.equals("VIP")) {
                        intent.putExtra(App.INTENT_REQUEST_KEY, PurchasesActivity.INTENT_BUY_VIP);
                    } else {
                        intent.putExtra(App.INTENT_REQUEST_KEY, PurchasesActivity.INTENT_BUY);
                    }
                    intent.putExtra(PurchasesConstants.ARG_TAG_SOURCE, "Banner_" + mBanner.name);
                    break;
                case Banner.ACTION_URL:
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mBanner.parameter));
                    break;
                case Banner.ACTION_OFFERWALL:
                    switch (mBanner.parameter) {
                        case OfferwallsManager.SPONSORPAY:
                            OfferwallsManager.startSponsorpay(mPage.getActivity());
                            break;
                        default:
                            OfferwallsManager.startOfferwall(mPage.getActivity());
                            break;
                    }
                    break;
            }
            sendStat(mBanner, CLICK);
            if (intent != null) {
                mPage.getActivity().startActivity(intent);
            }
        }
    }
}

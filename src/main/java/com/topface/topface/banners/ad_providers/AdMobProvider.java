package com.topface.topface.banners.ad_providers;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.banners.RefreshablePageWithAds;
import com.topface.topface.data.Profile;

import java.util.Calendar;

/**
 * Created by kirussell on 12/01/15.
 * Admob.com sdk through GP Services
 */
class AdMobProvider extends AbstractAdsProvider {
    private AdView adView;
    private Context mContext;

    @Override
    public final boolean injectBannerInner(IPageWithAds page, IAdProviderCallbacks callbacks) {
        mContext = page.getActivity().getApplicationContext();
        createView(page);
        setCallback(callbacks);
        loadAdMob();
        if (page instanceof RefreshablePageWithAds) {
            ((RefreshablePageWithAds) page).setRefresher(new IRefresher() {
                @Override
                public void refreshBanner() {
                    loadAdMob();
                }
            });
        }
        return true;
    }

    @Override
    public String getBannerName() {
        return AdProvidersFactory.BANNER_ADMOB;
    }

    public Context getContext() {
        return mContext;
    }

    private Calendar getUserAge() {
        Calendar rightNow = Calendar.getInstance();
        int year = rightNow.get(Calendar.YEAR);
        rightNow.set(Calendar.YEAR, year - App.from(mContext).getProfile().age);
        return rightNow;
    }

    protected int getLayout() {
        return R.layout.banner_admob;
    }

    protected void createView(IPageWithAds page) {
        ViewGroup container = page.getContainerForAd();
        adView = (AdView) View
                .inflate(container.getContext(), getLayout(), container)
                .findViewById(R.id.adMobView);
    }

    public void setCallback(final IAdProviderCallbacks callbacks) {
        adView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
                if (callbacks != null) {
                    callbacks.onAdLoadSuccess(adView);
                }
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                if (callbacks != null) {
                    callbacks.onAdShow();
                }
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                if (callbacks != null) {
                    callbacks.onAdClick();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                if (callbacks != null) {
                    callbacks.onFailedToLoadAd(errorCode);
                }
            }
        });
    }

    protected void loadAdMob() {
//        Если нужно, то можно указать id девайса (например эмулятор) для запроса тестовой рекламы
//        adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
//        или id свего девайса
//        adRequest.addTestDevice("hex id твоего девайса");
        adView.loadAd(getAdRequest().build());
    }

    public AdRequest.Builder getAdRequest() {
        return new AdRequest.Builder()
                .setGender(
                        App.from(mContext).getProfile().sex == Profile.BOY ?
                                AdRequest.GENDER_MALE :
                                AdRequest.GENDER_FEMALE
                ).setBirthday(getUserAge().getTime());
    }
}

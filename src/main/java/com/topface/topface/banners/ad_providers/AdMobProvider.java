package com.topface.topface.banners.ad_providers;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.utils.CacheProfile;

import java.util.Calendar;

/**
 * Created by kirussell on 12/01/15.
 * Admob.com sdk through GP Services
 */
class AdMobProvider extends AbstractAdsProvider {

    @Override
    public boolean injectBannerInner(IPageWithAds page, IAdProviderCallbacks callbacks) {
        beforeInjectBannerInner();
        AdView adView = createView(page);
        setCallback(callbacks, adView);
        loadAdMob(adView);
        return true;
    }

    private Calendar getUserAge() {
        Calendar rightNow = Calendar.getInstance();
        int year = rightNow.get(Calendar.YEAR);
        rightNow.set(Calendar.YEAR, year - CacheProfile.getProfile().age);
        return rightNow;
    }

    protected int getLayout() {
        return R.layout.banner_admob;
    }

    protected AdView createView(IPageWithAds page) {
        ViewGroup container = page.getContainerForAd();
        return (AdView) View
                .inflate(container.getContext(), getLayout(), container)
                .findViewById(R.id.adMobView);
    }

    public void setCallback(final IAdProviderCallbacks callbacks, final AdView adView) {
        adView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adView.setVisibility(View.VISIBLE);
                callbacks.onAdLoadSuccess(adView);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                callbacks.onFailedToLoadAd();
            }
        });
    }

    public void loadAdMob(AdView adView) {
        AdRequest.Builder adRequest = new AdRequest.Builder()
                .setGender(
                        CacheProfile.getProfile().sex == Static.BOY ?
                                AdRequest.GENDER_MALE :
                                AdRequest.GENDER_FEMALE
                )
                .setBirthday(getUserAge().getTime());
//        Если нужно, то можно указать id девайса (например эмулятор) для запроса тестовой рекламы
//        adRequest.addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
//        или id свего девайса
//        adRequest.addTestDevice("hex id твоего девайса");
        adView.loadAd(adRequest.build());
    }

    protected void beforeInjectBannerInner(){

    }

}

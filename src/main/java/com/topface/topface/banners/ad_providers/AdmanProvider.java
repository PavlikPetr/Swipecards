package com.topface.topface.banners.ad_providers;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.utils.CacheProfile;

import ru.mail.android.adman.CustomParams;
import ru.mail.android.adman.ads.AdmanView;

// на данный момент не используется, невалидный slot_id, реализовано для версии библиотеки 2.2.7
class AdmanProvider extends AbstractAdsProvider {

    @Override
    public boolean injectBannerInner(IPageWithAds page, final IAdProviderCallbacks callbacks) {
        ViewGroup container = page.getContainerForAd();
        AdmanView adman = (AdmanView) View
                .inflate(container.getContext(), R.layout.banner_adman, container)
                .findViewById(R.id.admanView);
        CustomParams customParams = new CustomParams();
        customParams.setAge(CacheProfile.getProfile().age);
        customParams.setGender(CacheProfile.getProfile().sex == Static.BOY ? AdRequest.GENDER_MALE :
                AdRequest.GENDER_FEMALE);
        adman.init(App.getContext().getResources().getInteger(R.integer.adman_slot_id), customParams);
        adman.setListener(new AdmanView.AdmanViewListener() {
            @Override
            public void onLoad(AdmanView admanView) {
                admanView.setVisibility(View.VISIBLE);
                callbacks.onAdLoadSuccess(admanView);
            }

            @Override
            public void onNoAd(String s, AdmanView admanView) {
                callbacks.onFailedToLoadAd();
            }

            @Override
            public void onClick(AdmanView admanView) {
            }
        });
        adman.load();
        return true;
    }
}

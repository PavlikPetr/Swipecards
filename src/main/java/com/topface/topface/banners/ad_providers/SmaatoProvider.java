package com.topface.topface.banners.ad_providers;

import android.view.View;
import android.view.ViewGroup;

import com.smaato.soma.AdDownloaderInterface;
import com.smaato.soma.AdListenerInterface;
import com.smaato.soma.AdSettings;
import com.smaato.soma.AdType;
import com.smaato.soma.BannerView;
import com.smaato.soma.ReceivedBannerInterface;
import com.smaato.soma.bannerutilities.constant.BannerStatus;
import com.smaato.soma.exception.AdReceiveFailed;
import com.smaato.soma.internal.requests.settings.UserSettings;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.utils.CacheProfile;

class SmaatoProvider extends AbstractAdsProvider {

    @Override
    public boolean injectBannerInner(IPageWithAds page, final IAdProviderCallbacks callbacks) {
        ViewGroup container = page.getContainerForAd();
        final BannerView smaatoBanner = (BannerView) View
                .inflate(container.getContext(), R.layout.banner_smaato, container)
                .findViewById(R.id.smaatoView);
        AdSettings adSettings = new AdSettings();
        adSettings.setAdType(AdType.RICHMEDIA);
        adSettings.setAdspaceId(page.getActivity().getApplicationContext().getResources().getInteger(R.integer.smaato_space_id));
        smaatoBanner.setAdSettings(adSettings);
        UserSettings userSettings = new UserSettings();
        userSettings.setAge(CacheProfile.getProfile().age);
        userSettings.setUserGender(CacheProfile.getProfile().sex == Static.BOY ?
                UserSettings.Gender.MALE :
                UserSettings.Gender.FEMALE);
        if (App.getLastKnownLocation() != null) {
            userSettings.setLatitude(App.getLastKnownLocation().getLatitude());
            userSettings.setLongitude(App.getLastKnownLocation().getLongitude());
        }
        smaatoBanner.setUserSettings(userSettings);
        smaatoBanner.setAutoReloadEnabled(true);
        smaatoBanner.setAutoReloadFrequency(page.getActivity().getApplicationContext().getResources().getInteger(R.integer.smaato_autoreload_time));
        adSettings.setPublisherId(page.getActivity().getApplicationContext().getResources().getInteger(R.integer.smaato_publisher_id));
        smaatoBanner.setAdSettings(adSettings);
        smaatoBanner.addAdListener(new AdListenerInterface() {
            @Override
            public void onReceiveAd(AdDownloaderInterface adDownloaderInterface, ReceivedBannerInterface receivedBannerInterface) throws AdReceiveFailed {
                if (receivedBannerInterface.getStatus() == BannerStatus.ERROR) {
                    callbacks.onFailedToLoadAd();
                } else {
                    smaatoBanner.setVisibility(View.VISIBLE);
                    callbacks.onAdLoadSuccess(smaatoBanner);
                }
            }
        });
        smaatoBanner.asyncLoadNewBanner();
        return true;
    }
}

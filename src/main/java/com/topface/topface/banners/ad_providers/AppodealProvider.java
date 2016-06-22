package com.topface.topface.banners.ad_providers;

import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.StringRes;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.BannerView;
import com.appodeal.ads.UserSettings;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.banners.AppodealUserSettingsRules;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.data.Profile;
import com.topface.topface.utils.FormItem;

public class AppodealProvider extends AbstractAdsProvider {

    public static final String APPODEAL_APP_KEY = "2f48418b677cf24a3fa37eacfc7a4e76d385db08b51bd328";
    private static final String YANDEX_NETWORK = "yandex";

    @Override
    boolean injectBannerInner(final IPageWithAds page, final IAdProviderCallbacks callbacks) {
        Activity activity = page.getActivity();
        Appodeal.setLogging(Debug.isDebugLogsEnabled());
        Appodeal.disableNetwork(activity.getApplicationContext(), YANDEX_NETWORK, Appodeal.BANNER_VIEW);
        Appodeal.initialize(activity, APPODEAL_APP_KEY, Appodeal.BANNER_VIEW);
        final BannerView adView = Appodeal.getBannerView(page.getActivity());
        page.getContainerForAd().addView(adView);
        UserSettings userSettings = Appodeal.getUserSettings(activity);
        userSettings.setGender(
                App.get().getProfile().sex == Profile.BOY ?
                        UserSettings.Gender.MALE :
                        UserSettings.Gender.FEMALE)
                .setAge(App.get().getProfile().age);
        fillAdditionalUserSettings(userSettings);
        if (Appodeal.isLoaded(Appodeal.BANNER_VIEW)) {
            bannerLoaded(page, callbacks, adView);
        }
        Appodeal.setBannerCallbacks(new BannerCallbacks() {

            @Override
            public void onBannerLoaded(int i) {
                bannerLoaded(page, callbacks, adView);
            }

            @Override
            public void onBannerFailedToLoad() {
                if (callbacks != null) {
                    callbacks.onFailedToLoadAd(null);
                }
            }

            @Override
            public void onBannerShown() {
                if (callbacks != null) {
                    callbacks.onAdShow();
                }
            }

            @Override
            public void onBannerClicked() {
                if (callbacks != null) {
                    callbacks.onAdClick();
                }
            }
        });
        return true;
    }

    @Override
    public String getBannerName() {
        return AdProvidersFactory.BANNER_APPODEAL;
    }

    private void bannerLoaded(IPageWithAds page, IAdProviderCallbacks callbacks, BannerView adView) {
        Appodeal.show(page.getActivity(), Appodeal.BANNER_VIEW);
        if (callbacks != null) {
            callbacks.onAdLoadSuccess(adView);
        }
    }

    private void fillAdditionalUserSettings(UserSettings userSettings) {
        setUserSettingsSmoking(userSettings);
        setUserSettingsAlcohol(userSettings);
        setUserSettingsOcupation(userSettings);
    }

    private void setUserSettingsAlcohol(UserSettings userSettings) {
        if (userSettings != null) {
            FormItem formItem = App.get().getProfile().getFormByType(FormItem.DATA_TYPE.ALCOHOL);
            if (formItem != null) {
                String currentValue = formItem.value;
                UserSettings.Alcohol alcohol = UserSettings.Alcohol.NEGATIVE;
                for (AppodealUserSettingsRules.Alcohol item : AppodealUserSettingsRules.Alcohol.values()) {
                    if (isContainedEquals(currentValue, item.getIdsArray())) {
                        alcohol = item.getAlcohol();
                    }
                }
                userSettings.setAlcohol(alcohol);
            }
        }
    }

    private void setUserSettingsSmoking(UserSettings userSettings) {
        if (userSettings != null) {
            FormItem formItem = App.get().getProfile().getFormByType(FormItem.DATA_TYPE.SMOKING);
            if (formItem != null) {
                String currentValue = formItem.value;
                UserSettings.Smoking smoking = UserSettings.Smoking.NEGATIVE;
                for (AppodealUserSettingsRules.Smoking item : AppodealUserSettingsRules.Smoking.values()) {
                    if (isContainedEquals(currentValue, item.getIdsArray())) {
                        smoking = item.getSmoking();
                    }
                }
                userSettings.setSmoking(smoking);
            }
        }
    }

    private void setUserSettingsOcupation(UserSettings userSettings) {
        if (userSettings != null) {
            FormItem formItem = App.get().getProfile().getFormByType(FormItem.DATA_TYPE.EDUCATION);
            if (formItem != null) {
                String currentValue = formItem.value;
                Resources res = App.getContext().getResources();
                userSettings.setOccupation(
                        res.getString(R.string.profile_form_education_female_1).equals(currentValue)
                                || res.getString(R.string.profile_form_education_male_1).equals(currentValue)
                                ? UserSettings.Occupation.UNIVERSITY
                                : UserSettings.Occupation.OTHER);
            }
        }
    }

    private boolean isContainedEquals(String currentValue, @StringRes int... idsArray) {
        Resources res = App.getContext().getResources();
        for (int id : idsArray) {
            if (res.getString(id).equals(currentValue)) {
                return true;
            }
        }
        return false;
    }
}

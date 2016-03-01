package com.topface.topface.banners.ad_providers;

import com.topface.topface.R;

class AdMobMediationProvider extends AdMobProvider {

    @Override
    protected int getLayout() {
        return R.layout.banner_admob_mediation;
    }

    @Override
    public String getBannerName() {
        return AdProvidersFactory.BANNER_ADMOB_MEDIATION;
    }
}

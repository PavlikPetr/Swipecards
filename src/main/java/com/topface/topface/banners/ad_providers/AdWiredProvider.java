package com.topface.topface.banners.ad_providers;

import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.banners.PageInfo;

import java.util.HashMap;
import java.util.Map;

import ru.ideast.adwired.AWView;
import ru.ideast.adwired.events.OnNoBannerListener;
import ru.ideast.adwired.events.OnStartListener;

/**
 * Created by kirussell on 12/01/15.
 * Adwired.net/ru/developers/
 */
class AdWiredProvider extends AbstractAdsProvider {

    private static Map<PageInfo.PageName, Character> ADWIRED_MAP = new HashMap<>();
    static {
        ADWIRED_MAP.put(PageInfo.PageName.LIKES_TABS, '8');
        ADWIRED_MAP.put(PageInfo.PageName.MESSAGES_TABS, '9');
        ADWIRED_MAP.put(PageInfo.PageName.VISITORS_TABS, '4');
    }

    @Override
    public final boolean injectBannerInner(IPageWithAds page, final IAdProviderCallbacks callbacks) {
        ViewGroup container = page.getContainerForAd();
        final AWView adView = (AWView) View
                .inflate(container.getContext(), R.layout.banner_adwired, container)
                .findViewById(R.id.adwireView);
        adView.setOnStartListener(new OnStartListener() {
            @Override
            public void onStart() {
                callbacks.onAdLoadSuccess(adView);
            }
        });
        adView.setOnNoBannerListener(new OnNoBannerListener() {
            @Override
            public void onNoBanner() {
                callbacks.onFailedToLoadAd();
            }
        });
        adView.request(ADWIRED_MAP.get(page.getPageName()));
        return true;
    }


}

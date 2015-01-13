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

    private static Map<String, Character> ADWIRED_MAP = new HashMap<>();
    static {
        ADWIRED_MAP.put(PageInfo.PAGE_LIKES, '1');
        ADWIRED_MAP.put(PageInfo.PAGE_TABBED_LIKES, '1');
        ADWIRED_MAP.put(PageInfo.PAGE_MUTUAL, '2');
        ADWIRED_MAP.put(PageInfo.PAGE_DIALOGS, '3');
        ADWIRED_MAP.put(PageInfo.PAGE_TABBED_MESSAGES, '3');
        ADWIRED_MAP.put(PageInfo.PAGE_VISITORS, '5');
        ADWIRED_MAP.put(PageInfo.PAGE_BOOKMARKS, '6');
        ADWIRED_MAP.put(PageInfo.PAGE_FANS, '7');
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

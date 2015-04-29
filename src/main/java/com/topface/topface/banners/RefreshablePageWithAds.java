package com.topface.topface.banners;

import com.topface.topface.banners.ad_providers.IRefresher;

/**
 * Добавляем возможность
 */
public interface RefreshablePageWithAds extends IPageWithAds {

    void setRefresh(IRefresher refresher);


}

package com.topface.topface.banners;

import com.topface.topface.banners.ad_providers.IRefresher;

public interface RefreshablePageWithAds extends IPageWithAds {

    void setRefresh(IRefresher refresher);


}

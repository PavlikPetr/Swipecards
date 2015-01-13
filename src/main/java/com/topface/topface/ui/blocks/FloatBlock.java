package com.topface.topface.ui.blocks;

import android.view.ViewGroup;

import com.topface.topface.banners.BannerInjector;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.banners.ad_providers.AdProvidersFactory;
import com.topface.topface.utils.CacheProfile;

import java.util.Map;

/**
 * Блок для страниц, где нужно показывать баннеры или лидеров
 */
public class FloatBlock {
    /**
     * Идентификаторы для типов блоков (лидеры, баннеры, не показывать блоки)
     */
    public final static String FLOAT_TYPE_BANNER = "BANNER";
    public final static String FLOAT_TYPE_LEADERS = "LEADERS";
    public final static String FLOAT_TYPE_NONE = "NONE";
    public final static String[] FLOAT_TYPES = new String[]{
            FLOAT_TYPE_BANNER,
            FLOAT_TYPE_LEADERS,
            FLOAT_TYPE_NONE
    };

    private IPageWithAds mPage;
    private final ViewGroup mLayout;
    private LeadersBlock mLeaders;
    private BannerInjector mFeedBannerController;

    public FloatBlock(IPageWithAds page, ViewGroup layoutView) {
        super();
        mPage = page;
        mLayout = layoutView;
        initBlock();
    }

    private void initBlock() {
        String pageName = mPage.getPageName();
        Map<String, PageInfo> pagesInfo = CacheProfile.getOptions().getPagesInfo();
        if (pagesInfo.containsKey(pageName)) {
            String floatType = pagesInfo.get(pageName).floatType;
            if (floatType.equals(FLOAT_TYPE_BANNER)) {
                if (!CacheProfile.show_ad) return;
                getFeedBannerController().injectBanner(mPage);
            } else if (floatType.equals(FLOAT_TYPE_LEADERS)) {
                mLeaders = new LeadersBlock(mPage, mLayout);
            }
        }
        //Если переданого активити нет в карте, то не инициализируем ни один блок
    }

    public void onDestroy() {
        if (mFeedBannerController != null) mFeedBannerController.cleanUp();
    }

    public void onResume() {
        if (mLeaders != null) mLeaders.loadLeaders();
    }

    public BannerInjector getFeedBannerController() {
        if (mFeedBannerController != null) {
            mFeedBannerController = new BannerInjector(new AdProvidersFactory());
        }
        return mFeedBannerController;
    }
}

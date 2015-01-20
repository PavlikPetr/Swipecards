package com.topface.topface.banners;

import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.banners.ad_providers.AdProvidersFactory;
import com.topface.topface.banners.ad_providers.IAdsProvider;
import com.topface.topface.data.Options;
import com.topface.topface.utils.CacheProfile;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.topface.topface.banners.PageInfo.PageName;

/**
 * Created by kirussell on 11/01/15.
 * Controller over ads' sdks
 * Inject banner into provided {@link com.topface.topface.banners.IPageWithAds} with
 * {@link #injectBanner(IPageWithAds)} according to page information
 * </p>
 * When page with banner is destroyed call {@link #cleanUp()} to remove banner and clean container
 * (for example, in onDestroy method of Activity or Fragment)
 */
class BannerInjector implements IBannerInjector {

    private final AdProvidersFactory mProvidersFactory;
    private final List<WeakReference<IPageWithAds>> mUsedPages = new ArrayList<>();

    public BannerInjector(AdProvidersFactory providersFactory) {
        mProvidersFactory = providersFactory;
    }

    @Override
    public void injectBanner(final IPageWithAds page) {
        if (canInject(page)) {
            String banner = lookupBannerFor(page);
            IAdsProvider provider = getProvider(banner);
            showAd(page, provider);
        }
    }

    private boolean canInject(IPageWithAds page) {
        if (!CacheProfile.show_ad || page == null) {
            return false;
        }
        PageInfo.PageName pageId = page.getPageName();
        String pageName = pageId.getName();
        Options options = CacheProfile.getOptions();
        Map<String, PageInfo> pagesInfo = options.getPagesInfo();
        if (pagesInfo.containsKey(pageName)) {
            String floatType = pagesInfo.get(pageName).floatType;
            if (floatType.equals(PageInfo.FLOAT_TYPE_BANNER)) {
                // use feed banners only if they are not in tabbed layout
                if (pageId == PageName.LIKE || pageId == PageName.MUTUAL) {
                    return !options.likesWithThreeTabs.isEnabled();
                } else if (pageId == PageName.DIALOGS || pageId == PageName.BOOKMARKS) {
                    return !options.messagesWithTabs.isEnabled();
                }
                return true;
            }
        }
        return false;
    }

    private void showAd(final IPageWithAds page, IAdsProvider provider) {
        showAd(page, provider, false);
    }

    private void showAd(final IPageWithAds page, IAdsProvider provider, final boolean isFallbackAd) {
        if (provider != null) {
            final boolean injectInitiated = provider.injectBanner(page,
                    new IAdsProvider.IAdProviderCallbacks() {
                        @Override
                        public void onAdLoadSuccess(View adView) {
                            mUsedPages.add(new WeakReference<>(page));
                        }

                        @Override
                        public void onFailedToLoadAd() {
                            cleanUp(page);
                            if (!isFallbackAd) {
                                injectGag(page);
                            }
                        }
                    });
            if (!injectInitiated) {
                injectGag(page);
            }
        }
    }

    private void injectGag(IPageWithAds page) {
        showAd(page, getProvider(CacheProfile.getOptions().fallbackTypeBanner), true);
    }

    private IAdsProvider getProvider(String banner) {
        return mProvidersFactory != null ? mProvidersFactory.createProvider(banner) : null;
    }

    private String lookupBannerFor(IPageWithAds page) {
        Options options = CacheProfile.getOptions();
        Map<String, PageInfo> pagesInfo = options != null ? options.getPagesInfo() : null;
        String pageName = page.getPageName().getName();
        if (pagesInfo != null && pagesInfo.containsKey(pageName)) {
            PageInfo pageInfo = pagesInfo.get(pageName);
            if (pageInfo != null) {
                return pageInfo.getBanner();
            }
        }
        return null;
    }

    public void cleanUp() {
        for (WeakReference<IPageWithAds> page : mUsedPages) {
            IPageWithAds pageWithAds = page.get();
            if (pageWithAds != null) {
                cleanUp(pageWithAds);
            }
        }
    }

    private void cleanUp(IPageWithAds page) {
        ViewGroup container = page.getContainerForAd();
        if (container != null) {
            unbindDrawables(container);
            container.removeAllViews();
        }
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        }
    }
}

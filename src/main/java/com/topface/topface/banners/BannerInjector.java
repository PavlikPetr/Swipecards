package com.topface.topface.banners;

import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.banners.ad_providers.AdProvidersFactory;
import com.topface.topface.banners.ad_providers.IAdsProvider;
import com.topface.topface.data.Options;
import com.topface.topface.state.OptionsProvider;
import com.topface.topface.utils.CacheProfile;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by kirussell on 11/01/15.
 * Controller over ads' sdks
 * Inject banner into provided {@link com.topface.topface.banners.IPageWithAds} with
 * {@link #injectBanner(IPageWithAds)} according to page information
 * </p>
 * When page with banner is destroyed call {@link #cleanUp()} to remove banner and clean container
 * (for example, in onDestroy method of Activity or Fragment)
 */
class BannerInjector implements IBannerInjector, OptionsProvider.IOptionsUpdater {

    private final AdProvidersFactory mProvidersFactory;
    private final List<WeakReference<IPageWithAds>> mUsedPages = new ArrayList<>();
    private Options mOptions;
    private OptionsProvider mOptionsProvider = new OptionsProvider(this);

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
        Map<String, PageInfo> pagesInfo = getOptions().getPagesInfo();
        if (pagesInfo.containsKey(pageName)) {
            String floatType = pagesInfo.get(pageName).floatType;
            if (floatType.equals(PageInfo.FLOAT_TYPE_BANNER)) {
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
            if (!injectInitiated && !isFallbackAd) {
                injectGag(page);
            }
        }
    }

    private void injectGag(IPageWithAds page) {
        showAd(page, getProvider(getOptions().fallbackTypeBanner), true);
    }

    private IAdsProvider getProvider(String banner) {
        return mProvidersFactory != null ? mProvidersFactory.createProvider(banner, getOptions()) : null;
    }

    private String lookupBannerFor(IPageWithAds page) {
        Map<String, PageInfo> pagesInfo = getOptions() != null ? getOptions().getPagesInfo() : null;
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
        mOptionsProvider.unsubscribe();
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

    @Override
    public void onOptionsUpdate(Options options) {
        mOptions = options;
    }

    @Override
    public Options getOptions() {
        return mOptions;
    }
}

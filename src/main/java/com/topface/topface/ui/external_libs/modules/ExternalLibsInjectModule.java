package com.topface.topface.ui.external_libs.modules;

import com.topface.billing.OpenIabFragment;
import com.topface.topface.App;
import com.topface.topface.ui.external_libs.AdjustManager;
import com.topface.topface.ui.fragments.BaseAuthFragment;
import com.topface.topface.ui.fragments.TopfaceAuthFragment;
import com.topface.topface.ui.fragments.buy.AmazonBuyingFragment;
import com.topface.topface.ui.fragments.buy.CoinsBuyingFragment;
import com.topface.topface.ui.fragments.buy.GoogleMarketBuyingFragment;
import com.topface.topface.ui.fragments.buy.MarketBuyingFragment;
import com.topface.topface.ui.fragments.buy.PaymentWallBuyingFragment;
import com.topface.topface.ui.fragments.buy.TransparentMarketFragment;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.ui.fragments.buy.VipPaymentWallBuyFragment;
import com.topface.topface.utils.ads.AdToAppController;
import com.topface.topface.utils.ads.AdToAppHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppetr on 04/04/16.
 * Create singletone for different wrappers/managers of external libs
 */
@Module(library = true,
        complete = false,
        injects = {
                AdToAppHelper.class,
                BaseAuthFragment.class,
                OpenIabFragment.class,
                CoinsBuyingFragment.class,
                GoogleMarketBuyingFragment.class,
                VipBuyFragment.class,
                MarketBuyingFragment.class,
                PaymentWallBuyingFragment.class,
                AmazonBuyingFragment.class,
                TransparentMarketFragment.class,
                VipPaymentWallBuyFragment.class,
                TopfaceAuthFragment.class,
                App.class
        }
)
public class ExternalLibsInjectModule {

    @Provides
    @Singleton
    AdToAppController providesAdToAppController() {
        return new AdToAppController();
    }

    @Provides
    @Singleton
    AdjustManager providerAdjustManager() {
        return new AdjustManager();
    }
}
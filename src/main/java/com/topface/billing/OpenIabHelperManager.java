package com.topface.billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.data.Products;
import com.topface.topface.data.ProductsDetails;
import com.topface.topface.utils.CacheProfile;

import org.jetbrains.annotations.NotNull;
import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.AmazonAppstore;
import org.onepf.oms.appstore.GooglePlay;
import org.onepf.oms.appstore.NokiaStore;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;
import org.onepf.oms.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class OpenIabHelperManager implements IabHelper.OnIabSetupFinishedListener, IabHelper.QueryInventoryFinishedListener {
    private OpenIabHelper mHelper;
    private OpenIabHelper.Options.Builder mOptsBuilder;
    private boolean mIabSetupFinished = false;
    private boolean mIsInventoryReady = false;
    private boolean mIsStarted = false;
    private ArrayList<IInventoryReceiver> mInventoryReceivers = new ArrayList<>();
    private Inventory mLastInventory;

    public interface IInventoryReceiver {
        public void receiveInventory(Inventory inventory);
    }

    public void init() {
        if (!mIsStarted) {
            mIsStarted = true;
            if (mOptsBuilder == null) {
                mOptsBuilder = new OpenIabHelper.Options.Builder();
                //Проверять локально покупку мы не будем, пускай сервер проверит
                mOptsBuilder.setVerifyMode(OpenIabHelper.Options.VERIFY_ONLY_KNOWN);
                //Мы сами выбираем какой маркет у нас используется
                mOptsBuilder.setStoreSearchStrategy(OpenIabHelper.Options.SEARCH_STRATEGY_BEST_FIT);
                addAvailableStores(App.getContext(), mOptsBuilder);
                //Включаем/выключаем логи
                Logger.setLoggable(Debug.isDebugLogsEnabled());
            }
            //Создаем хелпер
            mHelper = new OpenIabHelper(App.getContext(), mOptsBuilder.build());
            mHelper.startSetup(this);
        }
    }

    /**
     * this _should_ be called
     */
    public void dispose() {
        if (mHelper != null) {
            mHelper.dispose();
        }

        mInventoryReceivers.clear();

        mHelper = null;
    }

    /**
     * Adds standart markets
     * Override this for fortumo, например
     *
     * @param context     context
     * @param optsBuilder options builder to modify
     */
    protected void addAvailableStores(Context context, OpenIabHelper.Options.Builder optsBuilder) {
        //Нам нужен конкретный AppStore, т.к. у каждого типа сборки свои продукты и поддержка других маркетов все равно не нужна
        switch (BuildConfig.MARKET_API_TYPE) {
            case GOOGLE_PLAY:
                optsBuilder.addAvailableStores(new GooglePlay(context, null));
                optsBuilder.addPreferredStoreName(OpenIabHelper.NAME_GOOGLE);
                break;
            case AMAZON:
                //Нужно для тестирования покупок в Amazon
                if (BuildConfig.DEBUG) {
                    optsBuilder.addAvailableStores(new AmazonAppstore(context) {
                        public boolean isBillingAvailable(String packageName) {
                            return true;
                        }
                    });
                } else {
                    optsBuilder.addAvailableStores(new AmazonAppstore(context));
                }
                optsBuilder.addPreferredStoreName(OpenIabHelper.NAME_AMAZON);
                break;
            case NOKIA_STORE:
                optsBuilder.addAvailableStores(new NokiaStore(context));
                optsBuilder.addPreferredStoreName(OpenIabHelper.NAME_NOKIA);
                break;
        }
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        return (mHelper != null && !mHelper.handleActivityResult(requestCode, resultCode, data));
    }

    public boolean isReadyToBuyNow() {
        return mHelper != null && mHelper.getSetupState() == OpenIabHelper.SETUP_RESULT_SUCCESSFUL ||
                mHelper.getSetupState() == OpenIabHelper.SETUP_RESULT_FAILED;
    }

    public void consumeAsync(final Purchase purchase, IabHelper.OnConsumeFinishedListener listener) {
        if (mHelper != null) {
            mHelper.consumeAsync(purchase, listener);
        }
    }

    public void launchPurchaseFlow(Activity act, @NotNull String sku, int requestCode,
                                   IabHelper.OnIabPurchaseFinishedListener listener, String extraData) {
        if (mHelper != null) {
            mHelper.launchPurchaseFlow(act, sku, requestCode, listener, extraData);
        }
    }

    public void launchSubscriptionPurchaseFlow(Activity act, @NotNull String sku, int requestCode,
                                               IabHelper.OnIabPurchaseFinishedListener listener, String extraData) {
        if (mHelper != null) {
            mHelper.launchSubscriptionPurchaseFlow(act, sku, requestCode, listener, extraData);
        }
    }

    @Override
    public void onIabSetupFinished(IabResult result) {
        Debug.log("OpenIabHelperManager: onIabSetupFinished");

        if (result.isFailure()) {
            //При инциализации произошла ошибка!
            Debug.error("OpenIabHelperManager: IAB setup is not success: " + result);
            return;
        }

        mIabSetupFinished = true;

        Debug.log("OpenIabHelperManager: Setup successful");

        requestInventory();
    }

    public Inventory getInvetory() {
        return mLastInventory;
    }

    public boolean isSetupFinished() {
        return mIabSetupFinished;
    }

    public boolean isSubscriptionsSupported() {
        return mHelper != null && mHelper.subscriptionsSupported();
    }

    public void updateInventory() {
        requestInventory();
    }

    private void requestInventory() {
        if (mHelper != null) {
            mIsInventoryReady = false;
            List<String> skuList = getMarketSkuList();
            mHelper.queryInventoryAsync(true, skuList, skuList, this);
        }
    }

    private List<String> getMarketSkuList() {
        ArrayList<String> skuList = new ArrayList<>();
        Products products = CacheProfile.getMarketProducts();
        if (products != null) {
            for (Products.BuyButton buyButton : products.coins) {
                skuList.add(buyButton.id);
            }
            for (Products.BuyButton buyButton : products.likes) {
                skuList.add(buyButton.id);
            }
            for (Products.BuyButton buyButton : products.coinsSubscriptions) {
                skuList.add(buyButton.id);
            }
            for (Products.BuyButton buyButton : products.coinsSubscriptionsMasked) {
                skuList.add(buyButton.id);
            }
            for (Products.BuyButton buyButton : products.premium) {
                skuList.add(buyButton.id);
            }
            for (Products.BuyButton buyButton : products.others) {
                skuList.add(buyButton.id);
            }
        }
        return skuList;
    }

    /**
     * Событие получения списка покупок пользователя
     */
    @Override
    public void onQueryInventoryFinished(IabResult iabResult, Inventory inventory) {
        if (iabResult.isSuccess()) {
            if (inventory != null) {
                mIsInventoryReady = true;
                mLastInventory = inventory;
                CacheProfile.setMarketProductsDetails(ProductsDetails.createFromInventory(mLastInventory));
                notifyInventoryReceivers();
            }
        } else {
            Debug.error("OpenIabHelperManager: onQueryInventoryFinished error: " + iabResult);
        }
    }

    public void addInventoryReceiver(IInventoryReceiver receiver) {
        if (!mInventoryReceivers.contains(receiver)) {
            mInventoryReceivers.add(receiver);
            if (mIsInventoryReady) {
                notityInventoryReceiver(receiver);
            }
        }

    }

    public void removeInventoryReceiver(IInventoryReceiver receiver) {
        if (mInventoryReceivers.contains(receiver)) {
            mInventoryReceivers.remove(receiver);
        }
    }

    private void notityInventoryReceiver(IInventoryReceiver receiver) {
        receiver.receiveInventory(getInvetory());
    }

    private void notifyInventoryReceivers() {
        for (IInventoryReceiver receiver : mInventoryReceivers) {
            notityInventoryReceiver(receiver);
        }
    }
}

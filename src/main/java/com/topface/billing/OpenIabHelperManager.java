package com.topface.billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.data.BuyButtonData;
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
    private boolean mIabAvailable = false;
    private boolean mIsInventoryReady = false;
    private boolean mIsSetupStarted = false;
    private boolean mIsSetupDone = false;
    private boolean mIsInventoryChecked;
    private ArrayList<IOpenIabEventListener> mInventoryReceivers = new ArrayList<>();
    private Inventory mLastInventory;

    public interface IOpenIabEventListener {
        void receiveInventory(Inventory inventory);

        void onOpenIabSetupFinished(boolean normaly);
    }

    private void init(Context context) {
        if (!mIsSetupStarted) {
            mIsSetupStarted = true;
            mIsSetupDone = false;
            if (mOptsBuilder == null) {
                mOptsBuilder = new OpenIabHelper.Options.Builder();
                //Проверять локально покупку мы не будем, пускай сервер проверит
                mOptsBuilder.setVerifyMode(OpenIabHelper.Options.VERIFY_ONLY_KNOWN);
                //Мы сами выбираем какой маркет у нас используется
                mOptsBuilder.setStoreSearchStrategy(OpenIabHelper.Options.SEARCH_STRATEGY_BEST_FIT);
                addAvailableStores(context, mOptsBuilder);
                //Включаем/выключаем логи
                Logger.setLoggable(Debug.isDebugLogsEnabled());
            }
            //Создаем хелпер
            mHelper = new OpenIabHelper(context, mOptsBuilder.build());
            mHelper.startSetup(this);
        }
    }

    /**
     * Clear mHelper, when its not needed to anybody
     */
    public void freeHelper() {
        if (mHelper != null) {
            mHelper.dispose();
        }

        mHelper = null;
        mIsSetupStarted = false;
        mIsSetupDone = false;
        mIabAvailable = false;
        mIsInventoryReady = false;
        mLastInventory = null;
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

        mIsSetupDone = true;

        if (result.isFailure()) {
            //При инциализации произошла ошибка!
            Debug.error("OpenIabHelperManager: IAB setup is not success: " + result);
            notifySetupFinished();
            freeHelper();
            return;
        }

        mIabAvailable = true;

        notifySetupFinished();
        Debug.log("OpenIabHelperManager: Setup successful");

        requestInventory();
    }

    public Inventory getInvetory() {
        return mLastInventory;
    }

    public boolean isIabAvailable() {
        return mIabAvailable;
    }

    public boolean isSubscriptionsSupported() {
        return mHelper != null && mHelper.subscriptionsSupported();
    }

    public void updateInventory() {
        requestInventory();
    }

    private void requestInventory() {
        if (mHelper != null) {
            if (mIsSetupDone) {
                mIsInventoryReady = false;
                List<String> skuList = getMarketSkuList();
                mHelper.queryInventoryAsync(true, skuList, skuList, this);
            }
        } else {
            init(App.getContext());
        }
    }

    private List<String> getMarketSkuList() {
        ArrayList<String> skuList = new ArrayList<>();
        Products products = CacheProfile.getMarketProducts();
        if (products != null) {
            for (BuyButtonData buyButton : products.coins) {
                skuList.add(buyButton.id);
            }
            for (BuyButtonData buyButton : products.likes) {
                skuList.add(buyButton.id);
            }
            for (BuyButtonData buyButton : products.coinsSubscriptions) {
                skuList.add(buyButton.id);
            }
            for (BuyButtonData buyButton : products.coinsSubscriptionsMasked) {
                skuList.add(buyButton.id);
            }
            for (BuyButtonData buyButton : products.premium) {
                skuList.add(buyButton.id);
            }
            for (BuyButtonData buyButton : products.others) {
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
                setInventoryChecked(false);
                CacheProfile.setMarketProductsDetails(ProductsDetails.createFromInventory(mLastInventory));
                notifyInventoryReceivers();
            }
        } else {
            Debug.error("OpenIabHelperManager: onQueryInventoryFinished error: " + iabResult);
        }
    }

    public void setInventoryChecked(boolean isChecked) {
        mIsInventoryChecked = isChecked;
    }

    public boolean isInventoryChecked() {
        return mIsInventoryChecked;
    }

    public void addOpenIabEventListener(Context context, IOpenIabEventListener receiver) {
        // context should be activity
        // for correct work of OpenIabHelper
        if (!(context instanceof Activity)) {
            throw new IllegalStateException("OpenIabHelperManager:: context for helper must be Activity!");
        }
        if (!mInventoryReceivers.contains(receiver)) {
            mInventoryReceivers.add(receiver);
            if (mIsSetupDone) {
                notitySetupFinished(receiver);
            }
            if (mIsInventoryReady) {
                notityInventoryReceiver(receiver);
            }
        }

        if (mHelper == null) {
            init(context);
        }
    }

    public void removeOpenIabEventListener(IOpenIabEventListener receiver) {
        if (mInventoryReceivers.contains(receiver)) {
            mInventoryReceivers.remove(receiver);
        }
        // free helper, when nobody need it
        if (mInventoryReceivers.isEmpty()) {
            freeHelper();
        }
    }

    private void notityInventoryReceiver(IOpenIabEventListener receiver) {
        receiver.receiveInventory(getInvetory());
    }

    private void notifyInventoryReceivers() {
        // if there are no active listeners - free helper
        // don't do it when notifing about setup finish,
        // because it will broke inventory update
        if (mInventoryReceivers.isEmpty()) {
            freeHelper();
        } else {
            for (IOpenIabEventListener receiver : mInventoryReceivers) {
                notityInventoryReceiver(receiver);
            }
        }
    }

    private void notitySetupFinished(IOpenIabEventListener receiver) {
        receiver.onOpenIabSetupFinished(isIabAvailable());
    }

    private void notifySetupFinished() {
        // we must not check for active listeners here,
        // because at next step we need to update inventory
        // and products details in cache
        for (IOpenIabEventListener receiver : mInventoryReceivers) {
            notitySetupFinished(receiver);
        }
    }
}

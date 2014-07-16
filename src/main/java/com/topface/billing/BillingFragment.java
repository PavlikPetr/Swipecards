package com.topface.billing;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.appsflyer.AppsFlyerLib;
import com.topface.framework.utils.Debug;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.data.Products;
import com.topface.topface.data.Verify;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PurchaseRequest;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.AmazonAppstore;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.ArrayList;
import java.util.List;

/**
 * Абстрактный фрагмент с интегрированным In-App billing
 */
public abstract class BillingFragment extends BaseFragment implements IabHelper.QueryInventoryFinishedListener,
        IabHelper.OnIabPurchaseFinishedListener,
        IabHelper.OnConsumeFinishedListener,
        IabHelper.OnIabSetupFinishedListener {

    public static final String ARG_TAG_SOURCE = "from_value";
    private static final int BUYING_REQUEST = 1001;
    public static final String TEST_PURCHASED_PRODUCT_ID = "android.test.purchased";
    public static final int PURCHASE_CANCEL = 1;
    public static final int PURCHASE_ERROR_ITEM_ALREADY_OWNED = 7;
    private OpenIabHelper mHelper;
    private static boolean mIsTestPayments = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initOpenIabHelper();
    }

    private void initOpenIabHelper() {
        OpenIabHelper.Options opts = new OpenIabHelper.Options();
        //Проверять локально покупку мы не будем, пускай сервер проверит
        opts.verifyMode = OpenIabHelper.Options.VERIFY_SKIP;
        initAmazonDebug(opts);
        //Создаем хелпер
        mHelper = new OpenIabHelper(getActivity(), opts);
        //Включаем/выключаем логи
        OpenIabHelper.enableDebugLogging(Debug.isDebugLogsEnabled());
        mHelper.startSetup(this);
    }

    /**
     * Нужно для тестирования покупок в Amazon
     */
    private void initAmazonDebug(OpenIabHelper.Options opts) {
        if (BuildConfig.DEBUG && BuildConfig.BILLING_TYPE == BillingType.AMAZON) {
            opts.availableStores = new ArrayList<>();
            opts.availableStores.add(new AmazonAppstore(getActivity()) {
                public boolean isBillingAvailable(String packageName) {
                    return true;
                }
            });
        }
    }

    @Override
    public void onIabSetupFinished(IabResult result) {
        Debug.log("BillingFragment: onIabSetupFinished");

        if (result.isFailure()) {
            //При инциализации произошла ошибка!
            Debug.error("BillingFragment: IAB setup is not success: " + result);
            onInAppBillingUnsupported();
            return;
        }

        Debug.log("BillingFragment: Setup successful");

        //Запрашиваем список покупок
        requestInventory();

        //Вызываем колбэки, оповещая, что покупки доступны
        onInAppBillingSupported();

        if (mHelper.subscriptionsSupported()) {
            onSubscriptionSupported();
        } else {
            onSubscriptionUnsupported();
        }

        requestInventory();
    }


    /**
     * Колбэк окончания "использования" (consume) продукта.
     */
    @Override
    public void onConsumeFinished(Purchase purchase, IabResult iabResult) {
        Debug.log("BillingFragment: onConsumeFinished " + iabResult + purchase);
        //Перезапрашиваем список покупок
        requestInventory();
    }

    private void requestInventory() {
        mHelper.queryInventoryAsync(true, this);
    }

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
        Debug.log("BillingFragment: Purchase finished: " + result + ", purchase: " + purchase);
        if (result.isFailure()) {
            //Если пользователь пытается купить еще не потраченый продукт
            switch (result.getResponse()) {
                case PURCHASE_ERROR_ITEM_ALREADY_OWNED:
                    setWaitScreen(true);
                    onError("У вас есть купленная, но не начисленная покупка, пожалуйста подождите и попробуйте еще раз");
                    Debug.error("BillingFragment: " + result + ". Try verify purchase");
                    //Перезапрашиваем покупки и начислеяем при необходимости
                    requestInventory();
                    break;
                case PURCHASE_CANCEL:
                    Debug.log("BillingFragment: User cancel purchase");
                    break;
                default:
                    onError("Error purchasing: " + result);
                    setWaitScreen(false);
            }
            return;
        }

        Debug.log("BillingFragment: Purchase successful with payloads " + purchase.getDeveloperPayload());

        verifyPurchase(purchase, getActivity());

    }

    /**
     * Событие получения списка покупок пользователя
     */
    @Override
    public void onQueryInventoryFinished(IabResult iabResult, Inventory inventory) {
        if (iabResult.isSuccess()) {
            if (inventory != null) {
                //Запрашиваем покупки, что бы их потратить
                List<String> allOwnedSkus = inventory.getAllOwnedSkus();
                for (String sku : allOwnedSkus) {
                    verifyPurchase(inventory.getPurchase(sku), getActivity());
                }

                //Проверяем VIP
                List<String> marketSubs = inventory.getAllOwnedSkus(IabHelper.ITEM_TYPE_SUBS);
                Products.ProductsInventory serverSubs = CacheProfile.getMarketProducts().inventory;
                for (String sku : marketSubs) {
                    //Если на сервере нет какой то подписки, которая есть в маркете, то отправляем ее повторно
                    if (serverSubs == null || !serverSubs.containsSku(sku)) {
                        verifyPurchase(inventory.getPurchase(sku), getActivity());
                    }
                }
            }
        } else {
            Debug.error("BillingFragment: onQueryInventoryFinished error: " + iabResult);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //После того как View создано проверяем, нужно ли показывать переключатель тестовых покупок
        if (isTestPurchasesAvailable()) {
            ViewStub stub = (ViewStub) getView().findViewById(R.id.EditorTestStub);
            if (stub != null) {
                View layout = stub.inflate();
                //Инициализируем красивый переключатель
                final EditSwitcher checkBox = new EditSwitcher(
                        (ViewGroup) layout.findViewById(R.id.EditorTestBuyCheckBox),
                        getResources().getString(R.string.editor_test_buy)
                );
                //Выставляем значение по умолчанию
                checkBox.setChecked(isTestPurchasesEnabled());

                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Переключаем режим тестовых покупок
                        setTestPaymentsState(
                                checkBox.doSwitch()
                        );
                    }
                });
            }
        }
    }

    /**
     * Chooses buyItem or buySubscription based on btn class type
     * (@link(Products.SubscriptionBuyButton) or Products.BuyButton)
     *
     * @param btn BuyButton object to determine which type of buy is processing
     */
    public void buy(Products.BuyButton btn) {
        if (btn.id != null) {
            buy(btn.id);
        }
    }

    /**
     * Chooses buyItem or buySubscription based on btn class type
     * (@link(Products.SubscriptionBuyButton) or Products.BuyButton)
     *
     * @param id for buy
     */                    //Дополнительная информация о покупке
    public void buy(final String id) {
        if (id != null) {
            mHelper.launchPurchaseFlow(
                    getActivity(),
                    //Если тестовые покупки, то подменяем id продукта на тестовый
                    isTestPurchasesEnabled() ? TEST_PURCHASED_PRODUCT_ID : id,
                    BUYING_REQUEST,
                    this,
                    getDeveloperPayload(id)
            );
        }
    }

    private String getSourceValue() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            return arguments.getString(ARG_TAG_SOURCE);
        }
        return null;
    }

    protected String getDeveloperPayload(String productId) {
        DeveloperPayload payload = new DeveloperPayload(
                CacheProfile.uid,
                productId,
                getSourceValue()
        );
        return payload.toJson();
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    protected void editSubscriptions() {
        Utils.goToMarket(getActivity());
    }

    protected void onError(String message) {
        Debug.log("BillingFragment: Error! " + message);

        AlertDialog.Builder bld = new AlertDialog.Builder(getActivity());
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        bld.create().show();
    }

    abstract public void onSubscriptionSupported();

    abstract public void onSubscriptionUnsupported();

    abstract public void onInAppBillingSupported();

    abstract public void onInAppBillingUnsupported();

    /**
     * Метод вызывается когда мы хотим заблокировать или разблокировать интерфейс для пользователя
     *
     * @param enableWait включить ли блокировку интерфейса
     */
    public void setWaitScreen(boolean enableWait) {
        setSupportProgressBarIndeterminateVisibility(enableWait);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Debug.log("BillingFragment: onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode + " data: " + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Debug.log("BillingFragment: onActivityResult handled by OpenIAB");
        }
    }

    /**
     * Проверка платежа на сервере
     * Этот метод может вызываться как при покупке, так и при разборе очереди,
     *
     * @param purchase объект с данными покупки
     */
    public void verifyPurchase(final Purchase purchase, final Context context) {
        if (purchase == null) {
            Debug.error("BillingFragment: purchase is empty");
        }
        // Отправлем покупку на сервер для проверки и начисления
        PurchaseRequest.getValidateRequest(purchase, context).callback(new DataApiHandler<Verify>() {
            @Override
            protected void success(Verify verify, IApiResponse response) {
                //Послу удачной покупки (не подписки), которая была проверена сервером,
                //нужно "потратить" элемент, что бы можно было купить следующий
                if (TextUtils.equals(purchase.getItemType(), IabHelper.ITEM_TYPE_INAPP)) {
                    mHelper.consumeAsync(purchase, BillingFragment.this);
                }
                onPurchased(purchase.getSku());
                //Статистика AppsFlyer
                if (verify.revenue > 0) {
                    try {
                        AppsFlyerLib.sendTrackingWithEvent(
                                context,
                                "purchase",
                                Double.toString(verify.revenue)
                        );
                    } catch (Exception e) {
                        Debug.error("AppsFlyer exception", e);
                    }
                }
            }

            @Override
            protected Verify parseResponse(ApiResponse response) {
                return new Verify(response);
            }

            @Override
            public void fail(int codeError, final IApiResponse response) {
                Debug.error("BillindFragment: verify error: " + response);
            }

        }).exec();
    }

    abstract public void onPurchased(final String productId);

    /**
     * Включен ли режим тестовых покупок
     */
    public boolean isTestPurchasesEnabled() {
        //На всякий случай проверяем, что доступны тестовые платежи
        return isTestPurchasesAvailable() && mIsTestPayments;
    }

    /**
     * Доступны ли тестовые платежи
     */
    public boolean isTestPurchasesAvailable() {
        return CacheProfile.isEditor();
    }

    public static void setTestPaymentsState(boolean testPaymentsState) {
        mIsTestPayments = testPaymentsState;
    }
}

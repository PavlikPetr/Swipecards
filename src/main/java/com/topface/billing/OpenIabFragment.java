package com.topface.billing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.analytics.HitBuilders;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Products;
import com.topface.topface.data.Verify;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.PurchaseRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.UserConfig;

import org.onepf.oms.OpenIabHelper;
import org.onepf.oms.appstore.googleUtils.IabHelper;
import org.onepf.oms.appstore.googleUtils.IabResult;
import org.onepf.oms.appstore.googleUtils.Inventory;
import org.onepf.oms.appstore.googleUtils.Purchase;

import java.util.List;

/**
 * Абстрактный фрагмент, реализующий процесс покупки черес библиотеку OpenIAB
 * Если вам нужны покупки через GP, Amazon, Nokia и все остальное, что поддерживает OpenIAB, то
 * это именно тот фрагмент, который вам нужен.
 * Наследуемся, подписываемся на коллбэки и просто вызываем метод buy для покупки
 * https://github.com/onepf/OpenIAB
 */
public abstract class OpenIabFragment extends AbstractBillingFragment implements
        IabHelper.OnIabPurchaseFinishedListener,
        IabHelper.OnConsumeFinishedListener, OpenIabHelperManager.IInventoryReceiver {

    public static final String ARG_TAG_SOURCE = "from_value";
    public static final int BUYING_REQUEST = 1001;
    public static final String TEST_PURCHASED_PRODUCT_ID = "android.test.purchased";
    private static final String APP_STORE_NAME = "&storename";
    /**
     * Результат запроса из OpenIAB: Пользователь отменил покупку
     */
    public static final int PURCHASE_CANCEL = 1;
    /**
     * Результат запроса из Google Play: Пользователь отменил покупку
     */
    public static final int PURCHASE_CANCEL_GP = -1005;
    /**
     * Результат запроса из OpenIAB: Товар уже куплен, но не потрачен
     */
    public static final int PURCHASE_ERROR_ITEM_ALREADY_OWNED = 7;
    public static final int PURCHASE_CANCEL_FORTUMO = 5;
    public static final int PURCHASE_IMPOSSIBLE_FORTUMO = 6;

    private boolean mHasDeferredPurchase = false;
    private View mDeferredPurchaseButton;
    private UserConfig mUserConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserConfig = App.getUserConfig();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        checkIabSetupFinished();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        App.getOpenIabHelperManager().removeInventoryReceiver(this);
    }

    public void checkIabSetupFinished() {
        if (!App.getOpenIabHelperManager().isSetupFinished()) {
            if (isAdded()) {
                onInAppBillingUnsupported();
                onSubscriptionUnsupported();
            }
            return;
        }

        App.getOpenIabHelperManager().addInventoryReceiver(this);

        if (isAdded()) {
            //Вызываем колбэки, оповещая, что покупки доступны
            onInAppBillingSupported();

            if (App.getOpenIabHelperManager().isSubscriptionsSupported()) {
                onSubscriptionSupported();
            } else {
                onSubscriptionUnsupported();
            }

            if (mHasDeferredPurchase) {
                stopWaiting();
                buyNow((Products.BuyButton) mDeferredPurchaseButton.getTag());
                mHasDeferredPurchase = false;
                mDeferredPurchaseButton = null;
            }
        }

    }

    private void startWaiting() {
        if (mDeferredPurchaseButton != null) {
            mDeferredPurchaseButton.findViewById(R.id.itText).setVisibility(View.INVISIBLE);
            mDeferredPurchaseButton.findViewById(R.id.marketWaiter).setVisibility(View.VISIBLE);
            mDeferredPurchaseButton.findViewById(R.id.itContainer).setEnabled(false);
        }
    }

    private void stopWaiting() {
        if (mDeferredPurchaseButton != null) {
            mDeferredPurchaseButton.findViewById(R.id.itText).setVisibility(View.VISIBLE);
            mDeferredPurchaseButton.findViewById(R.id.marketWaiter).setVisibility(View.GONE);
            mDeferredPurchaseButton.findViewById(R.id.itContainer).setEnabled(true);
        }
    }


    /**
     * Колбэк окончания "использования" (consume) продукта.
     */
    @Override
    public void onConsumeFinished(Purchase purchase, IabResult iabResult) {
        Debug.log("BillingFragment: onConsumeFinished " + iabResult + purchase);
        if (isAdded()) {
            //Запрашиваем список покупок с небольшой задержкой,
            //что бы успели обновится данные в Google Play
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    App.getOpenIabHelperManager().updateInventory();
                }
            }, 2000);
        }
    }

    @Override
    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
        Debug.log("BillingFragment: Purchase finished: " + result + ", purchase: " + purchase);
        if (result.isFailure()) {
            //Если пользователь пытается купить еще не потраченый продукт
            switch (result.getResponse()) {
                case PURCHASE_IMPOSSIBLE_FORTUMO:
                    if (!result.getMessage().contains("pending")) {
                        Debug.log("BillingFragment: Fortumo purchase error");
                        break;
                    }
                case PURCHASE_ERROR_ITEM_ALREADY_OWNED:
                    if (isAdded()) {
                        onError(getActivity().getString(R.string.billing_item_already_owned));
                    }
                    Debug.error("BillingFragment: " + result + ". Try verify purchase");
                    //Перезапрашиваем покупки и начислеяем при необходимости
                    App.getOpenIabHelperManager().updateInventory();
                    break;
                case PURCHASE_CANCEL:
                case PURCHASE_CANCEL_GP:
                case PURCHASE_CANCEL_FORTUMO:
                    Debug.log("BillingFragment: User cancel purchase");
                    break;
                default:
                    if (isAdded()) {
                        onError(getActivity().getString(R.string.general_buying_disabled) + " " + result);
                        setWaitScreen(false);
                    }
            }
            return;
        }

        Debug.log("BillingFragment: Purchase successful with payloads " + purchase.getDeveloperPayload());

        verifyPurchase(purchase, getActivity());

    }

    public void checkInventory(Inventory inventory) {
        if (inventory != null) {
            //Запрашиваем покупки, что бы их потратить
            List<String> allOwnedSkus = inventory.getAllOwnedSkus();
            Debug.log("BillingFragment: inventory " + allOwnedSkus);
            //И подписки, что бы их проверить
            Products marketProducts = getProducts();
            //Покупки юзера на сервере
            Products.ProductsInventory serverSubs = marketProducts != null ? marketProducts.inventory : null;
            for (String sku : allOwnedSkus) {
                Purchase purchase = inventory.getPurchase(sku);
                if (OpenIabHelper.ITEM_TYPE_SUBS.equals(purchase.getItemType())) {
                    //Если на сервере нет какой то подписки, которая есть в маркете и сервер не является стейджем, то отправляем ее повторно
                    if (isMainApi() && serverSubs != null && !serverSubs.containsSku(sku)) {
                        Debug.log("BillingFragment: restore subscription: " + sku);
                        verifyPurchase(purchase, getActivity());
                    }
                } else {
                    //Если это не использованный продукт, то валидируем его на сервере
                    Debug.log("BillingFragment: restore in-app item: " + sku);
                    verifyPurchase(purchase, getActivity());
                }
            }
        }
    }

    private boolean isMainApi() {
        return App.getAppConfig().getApiDomain().equals(Static.API_URL);
    }

    protected abstract Products getProducts();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mIsTestPurchasesAvailable = savedInstanceState.getBoolean(IS_TEST_PURCHASES_AVAILABLE, false);
            mIsTestPayments = savedInstanceState.getBoolean(IS_TEST_PURCHASES_ENABLED, false);
        }
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
                        setTestPaymentsState(checkBox.isChecked());
                    }
                });
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_TEST_PURCHASES_AVAILABLE, isTestPurchasesAvailable());
        outState.putBoolean(IS_TEST_PURCHASES_ENABLED, isTestPurchasesEnabled());
    }

    /**
     * Chooses buyItem or buySubscription based on btn class type
     * (@link(Products.SubscriptionBuyButton) or Products.BuyButton)
     *
     * @param btn BuyButton object to determine which type of buy is processing
     */
    public void buy(Products.BuyButton btn) {
        if (App.getOpenIabHelperManager().isReadyToBuyNow()) {
            buyNow(btn);
        } else {
            buyDeferred(btn);
        }
    }

    private void buyNow(Products.BuyButton btn) {
        if (btn.id != null) {
            if (btn.type.isSubscription() && !isTestPurchasesEnabled()) {
                buySubscription(btn.id);
            } else {
                buyItem(btn.id);
            }
        }
    }

    private void buyDeferred(final Products.BuyButton btn) {
        if (mDeferredPurchaseButton != null) {
            stopWaiting();
        }
        mHasDeferredPurchase = true;
        mDeferredPurchaseButton = getView().findViewWithTag(btn);
        startWaiting();
    }

    /**
     * Покупка обычного продукта (не подписки)
     *
     * @param id sku продукта
     */
    public void buyItem(final String id) {
        Debug.log("BillingFragment: buyItem " + id + " test: " + isTestPurchasesEnabled());
        if (id != null && App.getOpenIabHelperManager().isSetupFinished()) {
            try {
                App.getOpenIabHelperManager().launchPurchaseFlow(
                        getActivity(),
                        //Если тестовые покупки, то подменяем id продукта на тестовый
                        isTestPurchasesEnabled() ? TEST_PURCHASED_PRODUCT_ID : id,
                        getRequestCode(),
                        this,
                        getDeveloperPayload(id)
                );
            } catch (NullPointerException e) {
                Debug.log("OMG! Misterious NPE in OpenIabHelper");
            }
        }
    }

    /**
     * Покупка подписки
     *
     * @param id sku продукта
     */
    public void buySubscription(final String id) {
        Debug.log("BillingFragment: buySubscription " + id + " test: " + isTestPurchasesEnabled());
        if (id != null && App.getOpenIabHelperManager().isSetupFinished()) {
            App.getOpenIabHelperManager().launchSubscriptionPurchaseFlow(
                    getActivity(),
                    //Если тестовые покупки, то подменяем id продукта на тестовый
                    isTestPurchasesEnabled() ? TEST_PURCHASED_PRODUCT_ID : id,
                    getRequestCode(),
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
        Debug.log("BillingFragment(" + ((Object) this).getClass().getSimpleName() + "): onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode + " data: " + data);

        // Pass on the activity result to the helper for handling
        if (App.getOpenIabHelperManager().handleActivityResult(requestCode, resultCode, data)) {
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
            return;
        }
        Debug.log("BillingFragment: try verify purchase " + purchase);
        if (mUserConfig.getPurchasedSubscriptions().contains(purchase.getOrderId())) {
            Debug.log("BillingFragment: subscription with order id " + purchase.getOrderId() +
                    " already purchased on this google account.");
            return;
        }

        // Отправлем покупку на сервер для проверки и начисления
        final PurchaseRequest validateRequest = PurchaseRequest.getValidateRequest(purchase, context);
        validateRequest.callback(new DataApiHandler<Verify>() {
            @Override
            protected void success(Verify verify, IApiResponse response) {
                //Послу удачной покупки (не подписки), которая была проверена сервером,
                //нужно "потратить" элемент, что бы можно было купить следующий
                if (TextUtils.equals(purchase.getItemType(), OpenIabHelper.ITEM_TYPE_INAPP)) {
                    App.getOpenIabHelperManager().consumeAsync(purchase, OpenIabFragment.this);
                }
                onPurchased(purchase);
                if (isNeedSendPurchasesStatistics()) {
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

                    // Google analytics statisctics
                    EasyTracker.getTracker().send(new HitBuilders.ItemBuilder()
                            .setTransactionId(purchase.getOrderId())
                            .setName(purchase.getSku())
                            .setSku(purchase.getSku())
                            .setCategory(purchase.getItemType())
                            .setPrice(verify.revenue)
                            .setQuantity(1l)
                            .set(APP_STORE_NAME, purchase.getAppstoreName()).build());
                }
            }

            @Override
            protected Verify parseResponse(ApiResponse response) {
                return new Verify(response);
            }

            @Override
            public void fail(int codeError, final IApiResponse response) {
                boolean consumed = consumeTestPurchase(purchase, validateRequest);
                consumed |= consumeDuplicatePurchase(codeError, purchase);
                if (!consumed) {
                    Debug.error("BillindFragment: verify error: " + response);
                }
            }

        }).exec();
    }

    private boolean isNeedSendPurchasesStatistics() {
        return !CacheProfile.isEditor() && !BuildConfig.DEBUG;
    }

    private boolean consumeTestPurchase(Purchase purchase, PurchaseRequest validateRequest) {
        // Если кто-то попытался купить android.test.purchased вне тестового режима,
        // то возникнет ситуация, что сервер не может валидировать покупку.
        // Поэтому мы тратим такую покупку после ошибки, если это тестовая покупка
        DeveloperPayload developerPayload = validateRequest.getDeveloperPayload();
        if (developerPayload != null && TextUtils.equals(developerPayload.sku, TEST_PURCHASED_PRODUCT_ID)) {
            App.getOpenIabHelperManager().consumeAsync(purchase, OpenIabFragment.this);
            return true;
        }
        return false;
    }

    private boolean consumeDuplicatePurchase(int codeError, Purchase purchase) {
        if (codeError == ErrorCodes.DUPLICATE_TRANSACTION) {
            if (purchase.getItemType().equals(OpenIabHelper.ITEM_TYPE_SUBS)) {
                mUserConfig.addPurchasedSubscription(purchase.getOrderId());
                mUserConfig.saveConfig();
                Debug.log("BillingFragment: subscription with order id " + purchase.getOrderId() +
                        " added to purchesed subscription for this google acount.");
                return false;
            } else {
                App.getOpenIabHelperManager().consumeAsync(purchase, OpenIabFragment.this);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPurchased(Purchase product) {
        if (isAdded()) {
            Toast.makeText(getActivity(), R.string.buying_store_ok, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Дада, это супер грязный хак для работы Google Play In-App Billing внутри фрагмента
     * см. http://stackoverflow.com/questions/23238360/implementing-android-in-app-purchase-with-fragments
     * <p/>
     * Если вы хотите добавить в вашу активити BillingFragment, то нужно вызывать этот метод внутри onActivityResult,
     * передав все параметры из него.
     * <p/>
     * Если же вы хотите использовать BillingFragment вложенным в другой фрагмент, то придется еще и прокинуть
     * onActivityResult в фрагмент предка, но данный метод умеет это делать автоматически, если вы
     * вызовите этот метод в активити и последним параметром parentFragmentClass
     * укажите родительский фрагмент, внутри которого находится BillingFragment
     * см. пример в PurchaseActivity.onActivityResult
     *
     * @param manager             FragmentManager, в котором нужно найти фрагмент, для поиска фрагментов нужно передавать getChildFragmentManager
     * @param requestCode         параметр requestCode из onActivityResult
     * @param resultCode          параметр resultCode из onActivityResult
     * @param data                параметр data из onActivityResult
     * @param parentFragmentClass клас родительского фрагмента, где содержится BillingFragment, если не задан,
     *                            то будем искать в manager именно BillinFragment. Если же задан, то сперва найдем
     *                            соответсвующий параметру parentFragment фрагмент и будем искать в его ChildFragmentManager
     */
    public static boolean processRequestCode(FragmentManager manager, int requestCode, int resultCode, Intent data, Class<? extends Fragment> parentFragmentClass) {
        List<Fragment> fragments = manager.getFragments();
        for (Fragment fragment : fragments) {
            if (parentFragmentClass != null && parentFragmentClass.isInstance(fragment)) {
                //Да, вам не показалось, это рекурсивный вызов, но с пустым последним парметром
                processRequestCode(fragment.getChildFragmentManager(), requestCode, resultCode, data, null);
                return true;
            } else if (fragment instanceof OpenIabFragment && ((OpenIabFragment) fragment).getRequestCode() == requestCode) {
                fragment.onActivityResult(requestCode, resultCode, data);
                return true;
            }
        }
        return false;
    }

    protected int getRequestCode() {
        return OpenIabFragment.BUYING_REQUEST;
    }

    @Override
    public void receiveInventory(Inventory inventory) {
        checkInventory(inventory);
    }
}


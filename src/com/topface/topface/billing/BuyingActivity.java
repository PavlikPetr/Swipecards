package com.topface.topface.billing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.topface.R;
import com.topface.topface.billing.BillingService.RequestPurchase;
import com.topface.topface.billing.BillingService.RestoreTransactions;
import com.topface.topface.billing.Consts.PurchaseState;
import com.topface.topface.billing.Consts.ResponseCode;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

public class BuyingActivity extends Activity implements View.OnClickListener {

    private ViewGroup mMoney6;
    private ViewGroup mMoney40;
    private ViewGroup mMoney100;
    private ViewGroup mMoney300;
    private ViewGroup mPower;
    private TextView mResourcesPower;
    private TextView mResourcesMoney;
    private BillingService mBillingService;

    public static final String INTENT_USER_COINS = "user_coins";

    public static final String BROADCAST_PURCHASE_ACTION = "com.topface.topface.PURCHASE_NOTIFICATION";

    /*
      * class NotificationReceiver
      */
    public BroadcastReceiver mPurchaseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BROADCAST_PURCHASE_ACTION)) {
                mResourcesPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.power));
                mResourcesPower.setText("" + CacheProfile.power + "%");
                mResourcesMoney.setText("" + CacheProfile.money);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_buying);
        Debug.log(this, "+onCreate");

        // Title Header
        ((TextView) findViewById(R.id.tvNavigationTitle))
                .setText(getString(R.string.buying_header_title));
        findViewById(R.id.btnNavigationHome).setVisibility(View.INVISIBLE);
        ImageButton backButton = ((ImageButton) findViewById(R.id.btnNavigationBack));
        backButton.setVisibility(View.VISIBLE);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        // Resources
        mResourcesMoney = (TextView) findViewById(R.id.tvResourcesMoney);
        mResourcesMoney.setText(getString(R.string.buying_you_have) + " " + CacheProfile.money);

        Matrix matrix = new Matrix();
        matrix.postScale(0.7f, 0.7f);

        // Drawable battery =
        // getResources().getDrawable(Utils.getBatteryResource(CacheProfile.power));
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                Utils.getBatteryResource(CacheProfile.power));
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        BitmapDrawable battery = new BitmapDrawable(getResources(), scaledBitmap);

        mResourcesPower = (TextView) findViewById(R.id.tvResourcesPower);
        mResourcesPower.setCompoundDrawablesWithIntrinsicBounds(null, null, battery, null);
        mResourcesPower.setText("" + CacheProfile.power + "%");

        int coins = getIntent().getIntExtra(INTENT_USER_COINS, 0);

        // Info
        TextView mResourcesInfo = (TextView) findViewById(R.id.tvResourcesInfo);
        if (coins > 0) {
            mResourcesInfo.setText(String.format(
                    getResources().getString(R.string.buying_you_have_no_coins_for_gift), coins));

        } else {
            mResourcesInfo.setText(getResources().getString(R.string.buying_default_message));
        }

        // Progress Bar
        new ProgressDialog(this).setMessage(getString(R.string.general_dialog_loading));

        mPower = (ViewGroup) findViewById(R.id.btnBuyingPower);
        mPower.setOnClickListener(this);

        mMoney6 = (ViewGroup) findViewById(R.id.btnBuyingMoney6);
        mMoney6.setOnClickListener(this);

        mMoney40 = (ViewGroup) findViewById(R.id.btnBuyingMoney40);
        mMoney40.setOnClickListener(this);

        mMoney100 = (ViewGroup) findViewById(R.id.btnBuyingMoney100);
        mMoney100.setOnClickListener(this);

        mMoney300 = (ViewGroup) findViewById(R.id.btnBuyingMoney300);
        mMoney300.setOnClickListener(this);

        mBillingService = new BillingService();
        mBillingService.setContext(this);

        ResponseHandler.register(new TopfacePurchaseObserver(new Handler()));

        if (!mBillingService.checkBillingSupported(Consts.ITEM_TYPE_INAPP)) {
            Toast.makeText(getApplicationContext(), "Play Market not available", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mPurchaseReceiver, new IntentFilter(BROADCAST_PURCHASE_ACTION));
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mPurchaseReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mBillingService.unbind();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        requestPurchase(view);
        //requestTestPurchase(view);
    }

    private void requestPurchase(View view) {
        switch (view.getId()) {
            case R.id.btnBuyingMoney6:
                mBillingService.requestPurchase("topface.coins.6", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingMoney40:
                mBillingService.requestPurchase("topface.coins.40", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingMoney100:
                mBillingService.requestPurchase("topface.coins.100", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingMoney300:
                mBillingService.requestPurchase("topface.coins.300", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingPower:
                mBillingService.requestPurchase("topface.energy.10000", Consts.ITEM_TYPE_INAPP, null);
                break;
        }
    }

    /**
     * Тестовые товары для отладки покупок
     * NOTE: Применяется только для тестирования!
     *
     * @param view кнопка покупки
     */
    private void requestTestPurchase(View view) {
        switch (view.getId()) {
            case R.id.btnBuyingMoney6:
                mBillingService.requestPurchase("android.test.purchased", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingMoney40:
                mBillingService.requestPurchase("android.test.canceled", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingMoney100:
                mBillingService.requestPurchase("android.test.refunded", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingMoney300:
                mBillingService.requestPurchase("android.test.item_unavailable", Consts.ITEM_TYPE_INAPP, null);
                break;
            case R.id.btnBuyingPower:
                mBillingService.requestPurchase("android.test.purchased", Consts.ITEM_TYPE_INAPP, null);
                break;
        }
    }

    private class TopfacePurchaseObserver extends PurchaseObserver {
        public TopfacePurchaseObserver(Handler handler) {
            super(BuyingActivity.this, handler);
        }

        @Override
        public void onBillingSupported(boolean supported, String type) {
            Debug.log("Buying: supported: " + supported);

            if (type == null || type.equals(Consts.ITEM_TYPE_INAPP)) {
                if (supported) {
                    mMoney6.setEnabled(true);
                    mMoney40.setEnabled(true);
                    mMoney100.setEnabled(true);
                    mMoney300.setEnabled(true);
                    mPower.setEnabled(true);
                } else {
                    mMoney6.setEnabled(false);
                    mMoney40.setEnabled(false);
                    mMoney100.setEnabled(false);
                    mMoney300.setEnabled(false);
                    mPower.setEnabled(false);
                    Toast.makeText(getApplicationContext(), getString(R.string.buy_play_market_not_available), Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        public void onPurchaseStateChange(PurchaseState purchaseState, String itemId,
                                          int quantity, long purchaseTime, String developerPayload,
                                          String signedData, String signature) {

            if (purchaseState == PurchaseState.PURCHASED) {
                Debug.log("Вот мы и купили, нужно понять что делать с интерфейсом");
            }
        }

        @Override
        public void onRequestPurchaseResponse(RequestPurchase request,
                                              ResponseCode responseCode) {
            Debug.log("Buying " + request.mProductId + ": " + responseCode);
            if (responseCode == ResponseCode.RESULT_OK) {
                Debug.log("Buying: purchase was successfully sent to server");
            } else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
                Debug.log("Buying: user canceled purchase");
            } else {
                Debug.log("Buying: purchase failed");
            }
        }

        @Override
        public void onRestoreTransactionsResponse(RestoreTransactions request,
                                                  ResponseCode responseCode) {
            if (responseCode == ResponseCode.RESULT_OK) {
                //Нам восстанавливать транзакции при переустановки приложения не нужно, нам об это и так сервер скажет
                Debug.log("Buying: completed RestoreTransactions request");
            } else {
                Debug.log("Buying: RestoreTransactions error: " + responseCode);
            }
        }
    }

}// BuyingActivity


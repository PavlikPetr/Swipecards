package com.topface.topface.billing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.billing.BillingDriver;
import com.topface.billing.BillingListener;
import com.topface.billing.BillingSupportListener;
import com.topface.billing.BillingTypeManager;
import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

public class BuyingActivity extends Activity implements View.OnClickListener, BillingSupportListener {

    private ViewGroup mMoney6;
    private ViewGroup mMoney40;
    private ViewGroup mMoney100;
    private ViewGroup mMoney300;
    private ViewGroup mPower;
    private TextView mResourcesPower;
    private TextView mResourcesMoney;

    public static final String INTENT_USER_COINS = "user_coins";

    private BillingDriver mBillingDriver;

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
                Utils.getBatteryResource(CacheProfile.likes));
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        BitmapDrawable battery = new BitmapDrawable(getResources(), scaledBitmap);

        mResourcesPower = (TextView) findViewById(R.id.tvResourcesPower);
        mResourcesPower.setCompoundDrawablesWithIntrinsicBounds(null, null, battery, null);
        mResourcesPower.setText("" + CacheProfile.likes + "%");

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

        setButtonLiseners();

        mBillingDriver = BillingTypeManager.getInstance().createMainBillingDriver(this, new BillingListener() {
            @Override
            public void onPurchased() {
                mResourcesPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.likes));
                mResourcesPower.setText("" + CacheProfile.likes + "%");
                mResourcesMoney.setText("" + CacheProfile.money);
            }

            @Override
            public void onError() {
                Debug.error("Billing error");
            }

            @Override
            public void onCancel() {
                Debug.error("User cancel purchase");
            }
        }, this);


    }

    private void setButtonLiseners() {
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
    }

    @Override
    protected void onDestroy() {
        mBillingDriver.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        requestPurchase(view);
//        requestTestPurchase(view);
    }

    private void requestPurchase(View view) {
        switch (view.getId()) {
            case R.id.btnBuyingMoney6:
                mBillingDriver.buyItem("topface.coins.6");
                break;
            case R.id.btnBuyingMoney40:
                mBillingDriver.buyItem("topface.coins.40");
                break;
            case R.id.btnBuyingMoney100:
                mBillingDriver.buyItem("topface.coins.100");
                break;
            case R.id.btnBuyingMoney300:
                mBillingDriver.buyItem("topface.coins.300");
                break;
            case R.id.btnBuyingPower:
                mBillingDriver.buyItem("topface.energy.10000");
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
                mBillingDriver.buyItem("android.test.purchased");
                break;
            case R.id.btnBuyingMoney40:
                mBillingDriver.buyItem("android.test.canceled");
                break;
            case R.id.btnBuyingMoney100:
                mBillingDriver.buyItem("android.test.refunded");
                break;
            case R.id.btnBuyingMoney300:
                mBillingDriver.buyItem("android.test.item_unavailable");
                break;
            case R.id.btnBuyingPower:
                mBillingDriver.buyItem("android.test.purchased");
                break;
        }
    }

    @Override
    public void onInAppBillingSupported() {
        mMoney6.setEnabled(true);
        mMoney40.setEnabled(true);
        mMoney100.setEnabled(true);
        mMoney300.setEnabled(true);
        mPower.setEnabled(true);
    }

    @Override
    public void onInAppBillingUnsupported() {
        mMoney6.setEnabled(false);
        mMoney40.setEnabled(false);
        mMoney100.setEnabled(false);
        mMoney300.setEnabled(false);
        mPower.setEnabled(false);
        Toast.makeText(getApplicationContext(), getString(R.string.buy_play_market_not_available), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubscritionSupported() {
    }

    @Override
    public void onSubscritionUnsupported() {
    }


}


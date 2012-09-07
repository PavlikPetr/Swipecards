package com.topface.topface.billing;

import com.google.android.apps.analytics.easytracking.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.billing.BillingService.RequestPurchase;
import com.topface.topface.billing.BillingService.RestoreTransactions;
import com.topface.topface.billing.Consts.PurchaseState;
import com.topface.topface.billing.Consts.ResponseCode;
import com.topface.topface.services.NotificationService;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class BuyingActivity extends Activity implements View.OnClickListener {
  // Data
  private Handler mHandler;
  private ViewGroup mMoney6;
  private ViewGroup mMoney40;
  private ViewGroup mMoney100;
  private ViewGroup mPower;
  private TextView mResourcesPower;
  private TextView mResourcesMoney;
  private BillingService mBillingService;
  private TopfacePurchaseObserver mTopfacePurchaseObserver;
  private ProgressDialog mProgressDialog;
  // Constants
  /*
  private static final int PRICE_COINS_6   = 6;
  private static final int PRICE_COINS_40  = 40;
  private static final int PRICE_COINS_100 = 100;
  private static final int PRICE_ENERGY    = 10000;
  */
  public static final String BROADCAST_PURCHASE_ACTION = "com.topface.topface.PURCHASE_NOTIFICATION";
  //---------------------------------------------------------------------------
  // class NotificationReceiver
  //---------------------------------------------------------------------------
  public BroadcastReceiver mPurchaseReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(BROADCAST_PURCHASE_ACTION)) {
        mResourcesPower.setBackgroundResource(Utils.getBatteryResource());
        mResourcesPower.setText(""+CacheProfile.power+"%");
        mResourcesMoney.setText(""+CacheProfile.money);
      }
    }
  };
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_buying);
    Debug.log(this,"+onCreate");

    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.buying_header_title));
    
    // Resources
    mResourcesPower = (TextView)findViewById(R.id.tvResourcesPower);
    mResourcesPower.setBackgroundResource(Utils.getBatteryResource());
    mResourcesPower.setText(""+CacheProfile.power+"%");
    mResourcesMoney = (TextView)findViewById(R.id.tvResourcesMoney);
    mResourcesMoney.setText(""+CacheProfile.money);

    // Progress Bar
    mProgressDialog = new ProgressDialog(this); // getApplicationContext() падает
    mProgressDialog.setMessage(getString(R.string.general_dialog_loading));

    //Drawable drwbl_energy = getResources().getDrawable(R.drawable.dating_power);
    //Drawable drwbl_coins = getResources().getDrawable(R.drawable.dating_money);
    {
      mMoney6 = (ViewGroup)findViewById(R.id.btnBuyingMoney6);
      mMoney6.setOnClickListener(this);
      //TextView tvTitle = (TextView)findViewById(R.id.tvBuyingTitle6);
      //tvTitle.setText(getString(R.string.buying_buy) + " " + PRICE_COINS_6);
      //tvTitle.setCompoundDrawablePadding(5);
      //tvTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_coins,null);
    }
    {
      mMoney40 = (ViewGroup)findViewById(R.id.btnBuyingMoney40);
      mMoney40.setOnClickListener(this);
      //TextView tvTitle = (TextView)findViewById(R.id.tvBuyingTitle40);
      //tvTitle.setText(getString(R.string.buying_buy) + " " + PRICE_COINS_40);
      //tvTitle.setCompoundDrawablePadding(5);
      //tvTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_coins,null);
    }
    {
      mMoney100 = (ViewGroup)findViewById(R.id.btnBuyingMoney100);
      mMoney100.setOnClickListener(this);
      //TextView tvTitle = (TextView)findViewById(R.id.tvBuyingTitle100);
      //tvTitle.setText(getString(R.string.buying_buy) + " " + PRICE_COINS_100);
      //tvTitle.setCompoundDrawablePadding(5);
      //tvTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_coins,null);
    }
    {
      mPower = (ViewGroup)findViewById(R.id.btnBuyingPower);
      mPower.setOnClickListener(this);
      //TextView tvTitle = (TextView)findViewById(R.id.tvBuyingTitlePower);
      //tvTitle.setText(getString(R.string.buying_buy) + " " + PRICE_ENERGY);
      //tvTitle.setCompoundDrawablePadding(5);
      //tvTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_energy,null);
    }
    
    mHandler = new Handler();
    mTopfacePurchaseObserver = new TopfacePurchaseObserver(mHandler);

    mBillingService = new BillingService();
    mBillingService.setContext(this);

    ResponseHandler.register(mTopfacePurchaseObserver);

    if(!mBillingService.checkBillingSupported()) {
      Toast.makeText(getApplicationContext(),"Play Market not available",Toast.LENGTH_SHORT).show();
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onStart() {
    super.onStart();
    registerReceiver(mPurchaseReceiver, new IntentFilter(BROADCAST_PURCHASE_ACTION));
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onStop() {
    unregisterReceiver(mPurchaseReceiver);
    super.onStop();
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    mBillingService.unbind();
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.btnBuyingMoney6:
        EasyTracker.getTracker().trackEvent("Purchase", "PurchaseStarted", "com.topface.topface.buy.coins.6", 0);
        mBillingService.requestPurchase("topface.coins.6", null); // topface.coins.6 // android.test.purchased
        break;
      case R.id.btnBuyingMoney40:
        EasyTracker.getTracker().trackEvent("Purchase", "PurchaseStarted", "com.topface.topface.buy.coins.40", 0);
        mBillingService.requestPurchase("topface.coins.40", null); // topface.coins.40
        break;
      case R.id.btnBuyingMoney100:
        EasyTracker.getTracker().trackEvent("Purchase", "PurchaseStarted", "com.topface.topface.buy.coins.100", 0);
        mBillingService.requestPurchase("topface.coins.100", null); // topface.coins.100
        break;
      case R.id.btnBuyingPower:
        EasyTracker.getTracker().trackEvent("Purchase", "PurchaseStarted", "com.topface.topface.buy.energy.10000", 0);
        mBillingService.requestPurchase("topface.energy.10000", null); // topface.energy.10000
        break;
    }
  }
  //---------------------------------------------------------------------------
  // class TopfacePurchaseObserver
  //---------------------------------------------------------------------------
  private class TopfacePurchaseObserver extends PurchaseObserver {
    public TopfacePurchaseObserver(Handler handler) {
      super(BuyingActivity.this,handler);
    }

    @Override
    public void onBillingSupported(boolean supported) {
      if(supported) {
        mMoney6.setEnabled(true);
        mMoney40.setEnabled(true);
        mMoney100.setEnabled(true);
        mPower.setEnabled(true);
      } else {
        //showDialog(2);
        EasyTracker.getTracker().trackEvent("Purchase", "PlayMarketNotAvailable", "", 0);
        mMoney6.setEnabled(false);
        mMoney40.setEnabled(false);
        mMoney100.setEnabled(false);
        mPower.setEnabled(false);
        Toast.makeText(getApplicationContext(),"Play Market not available",Toast.LENGTH_SHORT).show();
      }
    }
    @Override
    public void onPurchaseStateChange(PurchaseState purchaseState,String data,String signature) {
      if(purchaseState != PurchaseState.PURCHASED)
        return;
      NotificationService.purchase(getApplicationContext(),data,signature);
    }
    @Override
    public void onRequestPurchaseResponse(RequestPurchase request,ResponseCode responseCode) {
      Debug.log("BuyingActivity","onRequestPurchaseResponse");
    }
    @Override
    public void onRestoreTransactionsResponse(RestoreTransactions request,ResponseCode responseCode) {
      Debug.log("BuyingActivity","onRestoreTransactionsResponse");
    }
  }// TopfacePurchaseObserver
   //---------------------------------------------------------------------------
}// BuyingActivity


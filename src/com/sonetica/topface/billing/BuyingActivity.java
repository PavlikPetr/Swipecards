package com.sonetica.topface.billing;

import com.sonetica.topface.R;
import com.sonetica.topface.billing.BillingService.RequestPurchase;
import com.sonetica.topface.billing.BillingService.RestoreTransactions;
import com.sonetica.topface.billing.Consts.PurchaseState;
import com.sonetica.topface.billing.Consts.ResponseCode;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class BuyingActivity extends Activity implements View.OnClickListener {
  // Data
  private ViewGroup mMoney6;
  private ViewGroup mMoney40;
  private ViewGroup mMoney100;
  private ViewGroup mPower;
  private TopfacePurchaseObserver mTopfacePurchaseObserver;
  private Handler mHandler;
  private BillingService mBillingService;
  // Constants
  private static final int PRICE_COINS_6   = 6;
  private static final int PRICE_COINS_40  = 40;
  private static final int PRICE_COINS_100 = 100;
  private static final int PRICE_ENERGY    = 10000;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_buying);
    Debug.log(this,"+onCreate");

    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.buying_header_title));

    Drawable drwbl_energy = getResources().getDrawable(R.drawable.dating_power);
    Drawable drwbl_coins = getResources().getDrawable(R.drawable.dating_money);
    {
      mMoney6 = (ViewGroup)findViewById(R.id.btnBuyingMoney6);
      mMoney6.setOnClickListener(this);
      TextView tvTitle = (TextView)findViewById(R.id.tvBuyingTitle6);
      tvTitle.setText(getString(R.string.buying_buy) + " " + PRICE_COINS_6);
      tvTitle.setCompoundDrawablePadding(5);
      tvTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_coins,null);
    }
    {
      mMoney40 = (ViewGroup)findViewById(R.id.btnBuyingMoney40);
      mMoney40.setOnClickListener(this);
      TextView tvTitle = (TextView)findViewById(R.id.tvBuyingTitle40);
      tvTitle.setText(getString(R.string.buying_buy) + " " + PRICE_COINS_40);
      tvTitle.setCompoundDrawablePadding(5);
      tvTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_coins,null);
    }
    {
      mMoney100 = (ViewGroup)findViewById(R.id.btnBuyingMoney100);
      mMoney100.setOnClickListener(this);
      TextView tvTitle = (TextView)findViewById(R.id.tvBuyingTitle100);
      tvTitle.setText(getString(R.string.buying_buy) + " " + PRICE_COINS_100);
      tvTitle.setCompoundDrawablePadding(5);
      tvTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_coins,null);
    }
    {
      mPower = (ViewGroup)findViewById(R.id.btnBuyingPower);
      mPower.setOnClickListener(this);
      TextView tvTitle = (TextView)findViewById(R.id.tvBuyingTitlePower);
      tvTitle.setText(getString(R.string.buying_buy) + " " + PRICE_ENERGY);
      tvTitle.setCompoundDrawablePadding(5);
      tvTitle.setCompoundDrawablesWithIntrinsicBounds(null,null,drwbl_energy,null);
    }

    mHandler = new Handler();
    mTopfacePurchaseObserver = new TopfacePurchaseObserver(mHandler);

    mBillingService = new BillingService();
    mBillingService.setContext(this);

    ResponseHandler.register(mTopfacePurchaseObserver);

    if(!mBillingService.checkBillingSupported()) {
      ;//Toast.makeText(getApplicationContext(),"no",Toast.LENGTH_SHORT).show();
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onStart() {
    super.onStart();
    ResponseHandler.register(mTopfacePurchaseObserver);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onStop() {
    super.onStop();
    ResponseHandler.unregister(mTopfacePurchaseObserver);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    super.onDestroy();
    mBillingService.unbind();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.btnBuyingMoney6:
        if(!mBillingService.requestPurchase("android.test.purchased",null)) // topface.coins.6
          ; //showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
        break;
      case R.id.btnBuyingMoney40:
        if(!mBillingService.requestPurchase("android.test.canceled",null)) // topface.coins.40
          ; //showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
        break;
      case R.id.btnBuyingMoney100:
        if(!mBillingService.requestPurchase("android.test.refunded",null)) // topface.coins.100
          ; //showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
        break;
      case R.id.btnBuyingPower: {
        if(!mBillingService.requestPurchase("android.test.item_unavailable",null)) // topface.energy
          ; //showDialog(DIALOG_BILLING_NOT_SUPPORTED_ID);
      }
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
      } else
        showDialog(2);
    }

    @Override
    public void onPurchaseStateChange(PurchaseState purchaseState,String itemId,int quantity,long purchaseTime,String developerPayload) {
      if(purchaseState == PurchaseState.PURCHASED)
        ;
      else
        ;
    }

    @Override
    public void onRequestPurchaseResponse(RequestPurchase request,ResponseCode responseCode) {
      if(responseCode == ResponseCode.RESULT_OK)
        ;
      else if(responseCode == ResponseCode.RESULT_USER_CANCELED)
        ;
      else
        ;
    }

    @Override
    public void onRestoreTransactionsResponse(RestoreTransactions request,ResponseCode responseCode) {
      if(responseCode == ResponseCode.RESULT_OK)
        ;
      else
        ;
    }
  }// TopfacePurchaseObserver
   //---------------------------------------------------------------------------
}// BuyingActivity


package com.sonetica.topface.billing;

import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.billing.BillingService.RequestPurchase;
import com.sonetica.topface.billing.BillingService.RestoreTransactions;
import com.sonetica.topface.billing.Consts.PurchaseState;
import com.sonetica.topface.billing.Consts.ResponseCode;
import com.sonetica.topface.data.Verify;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.net.VerifyRequest;
import com.sonetica.topface.ui.dating.ResourcesView;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class BuyingActivity extends Activity implements View.OnClickListener {
  // Data
  private ResourcesView mResources;
  private ViewGroup mMoney6;
  private ViewGroup mMoney40;
  private ViewGroup mMoney100;
  private ViewGroup mPower;
  private Handler mHandler;
  private BillingService mBillingService;
  private TopfacePurchaseObserver mTopfacePurchaseObserver;
  private ProgressDialog mProgressDialog;
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
    
    // Resources
    mResources = (ResourcesView)findViewById(R.id.datingRes);

    // Progress Bar
    mProgressDialog = new ProgressDialog(this); // getApplicationContext() падает
    mProgressDialog.setMessage(getString(R.string.dialog_loading));

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
      Toast.makeText(getApplicationContext(),"Play Market not available",Toast.LENGTH_SHORT).show();
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    super.onDestroy();
    mBillingService.unbind();
  }
  //---------------------------------------------------------------------------
  private void sendPerchaseData(String data,String signature) {
    VerifyRequest verifyRequest = new VerifyRequest(getApplicationContext());
    verifyRequest.data = data;
    verifyRequest.signature = signature;
    verifyRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        Verify verify = Verify.parse(response);
        Data.s_Money = verify.money;
        Data.s_Power = verify.power;
        mResources.setResources(verify.power,verify.money);
        mResources.invalidate();
        Debug.log("BuyingActivity","success:"+response);
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        Debug.log("BuyingActivity","fail:"+response);
        mProgressDialog.cancel();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.btnBuyingMoney6:
        mBillingService.requestPurchase("android.test.purchased",null); // topface.coins.6
        break;
      case R.id.btnBuyingMoney40:
        mBillingService.requestPurchase("android.test.canceled",null); // topface.coins.40
        break;
      case R.id.btnBuyingMoney100:
        mBillingService.requestPurchase("android.test.refunded",null); // topface.coins.100
        break;
      case R.id.btnBuyingPower:
        mBillingService.requestPurchase("android.test.item_unavailable",null); // topface.energy
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
        showDialog(2);
        mMoney6.setEnabled(false);
        mMoney40.setEnabled(false);
        mMoney100.setEnabled(false);
        mPower.setEnabled(false);
        Toast.makeText(getApplicationContext(),"Play Market not available",Toast.LENGTH_SHORT).show();
      }
    }
    /*
    @Override
    public void onPurchaseStateChange(PurchaseState purchaseState,String itemId,int quantity,long purchaseTime,String developerPayload) {
      if(purchaseState == PurchaseState.PURCHASED)
        ;
      else
        ;
    }
    */    
    @Override
    public void onPurchaseStateChange(PurchaseState purchaseState,String data,String signature) {
      if(purchaseState != PurchaseState.PURCHASED)
        return;
        mProgressDialog.show();
        sendPerchaseData(data,signature);
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


package com.sonetica.topface.billing;

import com.sonetica.topface.R;
import com.sonetica.topface.billing.BillingService.RequestPurchase;
import com.sonetica.topface.billing.BillingService.RestoreTransactions;
import com.sonetica.topface.billing.Consts.PurchaseState;
import com.sonetica.topface.billing.Consts.ResponseCode;
import com.sonetica.topface.services.NotificationService;
import com.sonetica.topface.ui.dating.ResourcesView;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class BuyingActivity extends Activity implements ServiceConnection, View.OnClickListener {
  // Data
  private ResourcesView mResources;
  private ViewGroup mMoney6;
  private ViewGroup mMoney40;
  private ViewGroup mMoney100;
  private ViewGroup mPower;
  private Handler mHandler;
  private Messenger mNotificationService;
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
    
    bindService(new Intent(this,NotificationService.class),this,Context.BIND_AUTO_CREATE);

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
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    super.onActivityResult(requestCode,resultCode,data);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    unbindService(this);
    mNotificationService=null;
    mBillingService.unbind();
    super.onDestroy();
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
    //mProgressDialog.show();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onServiceConnected(ComponentName name,IBinder service) {
    try {
      mNotificationService = new Messenger(service);
    } catch (Exception e) {
      Debug.log("BuyingActivity","onServiceConnected:"+e);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  public void onServiceDisconnected(ComponentName name) {
    try {
      mNotificationService = null;
    } catch (Exception e) {
      Debug.log("BuyingActivity","onServiceDisconnected:"+e);
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
    @Override
    public void onPurchaseStateChange(PurchaseState purchaseState,String data,String signature) {
      if(purchaseState != PurchaseState.PURCHASED)
        return;
      try {
        Bundle boundle = new Bundle();
        boundle.putString(NotificationService.INTENT_DATA,data);
        boundle.putString(NotificationService.INTENT_SIGNATURE,signature);
        Message msg = Message.obtain(null, NotificationService.MSG_PURCHASE,0,0);
        msg.setData(boundle);
        mNotificationService.send(msg);
      } catch(RemoteException e) {
        Debug.log("BuyingActivity","message sending error");
      }
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


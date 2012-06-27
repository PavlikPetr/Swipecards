package com.topface.topface.services;

import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Verify;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.VerifyRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class NotificationService extends Service {
  // Data
  // Constants
  public static final int MSG_PURCHASE = 104;
  // Intents
  public static final String PURCHASE_DATA = "data";
  public static final String PURCHASE_SIGNATURE = "signature";
  // Actions
  private static final String ACTION_PURCHASE = "com.topface.topface.PURCHASE";
  //---------------------------------------------------------------------------
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  public int onStartCommand(Intent intent,int flags,int startId) {
    Debug.log("NotifyService","onStartCommand");
    if(intent == null)
      return START_STICKY;
    
    String action = intent.getAction();
    
    if(action!=null) {
      if(action.equals(ACTION_PURCHASE)) {
        String data = intent.getStringExtra(PURCHASE_DATA);
        String signature = intent.getStringExtra(PURCHASE_SIGNATURE);
        verifyPurchase(data,signature);
      }
    }
    
    return START_STICKY;
  }
  //---------------------------------------------------------------------------
  @Override
  public void onDestroy() {
    Debug.log("notifyService","onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  public static void purchase(Context context, String data, String signature) {
    Intent intent = new Intent(context, NotificationService.class);
    intent.setAction(ACTION_PURCHASE);
    intent.putExtra(PURCHASE_DATA, data);
    intent.putExtra(PURCHASE_SIGNATURE, signature);
    context.startService(intent);
  }
  //---------------------------------------------------------------------------
  private void verifyPurchase(final String data, final String signature) {
    // сохранить ордер
    final VerifyRequest verifyRequest = new VerifyRequest(getApplicationContext());
    verifyRequest.data = data;
    verifyRequest.signature = signature;
    verifyRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        Verify verify = Verify.parse(response);
        CacheProfile.power = verify.power;
        CacheProfile.money = verify.money;
        // затереть ордер
        post(new Runnable() {
          @Override
          public void run() {
            sendBroadcast(new Intent(BuyingActivity.BROADCAST_PURCHASE_ACTION));
          }
        });
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        post(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(getApplicationContext(),getString(R.string.general_purchasing_error),Toast.LENGTH_LONG).show();
          }
        });
        // обратитесь в суппорт, ваш ордер
      }
    }).exec();
  }
 //---------------------------------------------------------------------------
}

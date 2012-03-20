/* Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package com.sonetica.topface.billing;

import com.sonetica.topface.billing.Consts.ResponseCode;
import com.sonetica.topface.utils.Debug;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BillingReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context,Intent intent) {
    String action = intent.getAction();
    if(Consts.ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
      String signedData = intent.getStringExtra(Consts.INAPP_SIGNED_DATA);
      String signature = intent.getStringExtra(Consts.INAPP_SIGNATURE);
      purchaseStateChanged(context,signedData,signature);
    } else if(Consts.ACTION_NOTIFY.equals(action)) {
      String notifyId = intent.getStringExtra(Consts.NOTIFICATION_ID);
      notify(context,notifyId);
    } else if(Consts.ACTION_RESPONSE_CODE.equals(action)) {
      long requestId = intent.getLongExtra(Consts.INAPP_REQUEST_ID,-1);
      int responseCodeIndex = intent.getIntExtra(Consts.INAPP_RESPONSE_CODE,ResponseCode.RESULT_ERROR.ordinal());
      checkResponseCode(context,requestId,responseCodeIndex);
    }
  }

  private void purchaseStateChanged(Context context,String signedData,String signature) {
    Debug.log("MARKET","receiver 3:"+signedData);
    Intent intent = new Intent(Consts.ACTION_PURCHASE_STATE_CHANGED);
    intent.setClass(context,BillingService.class);
    intent.putExtra(Consts.INAPP_SIGNED_DATA,signedData);
    intent.putExtra(Consts.INAPP_SIGNATURE,signature);
    context.startService(intent);
  }

  private void notify(Context context,String notifyId) {
    Debug.log("MARKET","receiver 2:"+notifyId);
    Intent intent = new Intent(Consts.ACTION_GET_PURCHASE_INFORMATION);
    intent.setClass(context,BillingService.class);
    intent.putExtra(Consts.NOTIFICATION_ID,notifyId);
    context.startService(intent);
  }

  private void checkResponseCode(Context context,long requestId,int responseCodeIndex) {
    Debug.log("MARKET","receiver 1: req:"+requestId+",resp:"+responseCodeIndex);
    Intent intent = new Intent(Consts.ACTION_RESPONSE_CODE);
    intent.setClass(context,BillingService.class);
    intent.putExtra(Consts.INAPP_REQUEST_ID,requestId);
    intent.putExtra(Consts.INAPP_RESPONSE_CODE,responseCodeIndex);
    context.startService(intent);
  }
}

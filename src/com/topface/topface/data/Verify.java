package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Verify extends AbstractData {
  // Data
  public boolean completed; // результат выполнения команды. В случае успешного выполнения, возвращает true
  public int money;         // количество монет пользователя
  public int power;         // количество энергии пользователя
  public String order;      // идентификатор верифицированного заказа
  //---------------------------------------------------------------------------
  public static Verify parse(ApiResponse response) {
    Verify verify = new Verify();
    
    try {
      verify.completed = response.mJSONResult.optBoolean("completed");
      verify.money     = response.mJSONResult.optInt("money");
      verify.power     = response.mJSONResult.optInt("power");
      verify.order     = response.mJSONResult.optString("order");
    } catch(Exception e) {
      Debug.log("Verify.class","Wrong response parsing: " + e);
    }
    
    return verify;
  }
  //---------------------------------------------------------------------------
}

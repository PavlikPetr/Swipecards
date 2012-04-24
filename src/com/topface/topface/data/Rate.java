package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class Rate extends AbstractData {
  // Data
  public int money;  // количество монет текущего пользователя
  public int power;   // текущее значение энергии пользователя
  public int average;  // средняя оценка пользователя
  //---------------------------------------------------------------------------
  public static Rate parse(ApiResponse response) {
    Rate doRate = new Rate();
    
    try {
      doRate.money = response.mJSONResult.optInt("money");
      doRate.power = response.mJSONResult.optInt("power");
      doRate.average = response.mJSONResult.optInt("average");
    } catch(Exception e) {
      Debug.log("DoRate.class","Wrong response parsing: " + e);
    }
    
    return doRate;
  }
  //---------------------------------------------------------------------------
}

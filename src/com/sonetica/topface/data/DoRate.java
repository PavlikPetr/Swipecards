package com.sonetica.topface.data;

import com.sonetica.topface.net.ApiResponse;
import com.sonetica.topface.utils.Debug;

public class DoRate extends AbstractData {
  // Data
  public int money;  // количество монет текущего пользователя
  public int power;   // текущее значение энергии пользователя
  public int average;  // средняя оценка пользователя
  //---------------------------------------------------------------------------
  public static DoRate parse(ApiResponse response) {
    DoRate doRate = new DoRate();
    
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
  @Override
  public String getBigLink() {
    return null;
  }
  //---------------------------------------------------------------------------
  @Override
  public String getSmallLink() {
    return null;
  }
  //---------------------------------------------------------------------------
}

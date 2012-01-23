package com.sonetica.topface.data;

import org.json.JSONException;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class DoRate extends AbstractData {
  // Data
  public int money;  // количество монет текущего пользователя
  public int power;  // текущее значение энергии пользователя
  //---------------------------------------------------------------------------
  public static DoRate parse(Response response) {
    DoRate doRate = new DoRate();
    try {
      doRate.money = response.mJSONResult.getInt("money");
      doRate.power = response.mJSONResult.getInt("power");
    } catch(JSONException e) {
      Debug.log("DoRate.class","Wrong response parsing: " + e);
    }
    return doRate;
  }
  //---------------------------------------------------------------------------
}

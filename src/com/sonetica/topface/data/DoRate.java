package com.sonetica.topface.data;

import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Debug;

public class DoRate extends AbstractData {
  // Data
  public int money;  // количество монет текущего пользователя
  public int power;  // текущее значение энергии пользователя
  //---------------------------------------------------------------------------
  public static DoRate parse(JSONObject response) {
    DoRate doRate = new DoRate();
    try {
      doRate.money = response.getInt("money");
      doRate.power = response.getInt("power");
    } catch(JSONException e) {
      Debug.log(null,"Wrong response parsing: " + e);
    }
    return doRate;
  }
  //---------------------------------------------------------------------------
}

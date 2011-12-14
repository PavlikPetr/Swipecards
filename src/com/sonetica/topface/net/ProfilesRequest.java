package com.sonetica.topface.net;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfilesRequest extends Request {
  // Data
  public String service = "profiles";
  public ArrayList<Integer> uids = new ArrayList<Integer>();
  // Methods
  @Override
  public String toString() {
    JSONObject root = new JSONObject();
    try {
      root.put("service",service);
      root.put("ssid",ssid);
      root.put("data",new JSONObject().put("uids",new JSONArray(uids)));
    } catch(JSONException e) {}
    return root.toString();
  }
}
/*
{"result":
  {"profiles":
    {"3246948":{"uid":3246948,
                "platform":"st",
                "first_name":"\u0415\u043a\u0430\u0442\u0435\u0440\u0438\u043d\u0430",
                "first_name_translit":"Ekaterina",
                "age":"22",
                "sex":"0",
                "last_visit":"2011-12-14 03:45:38",
                "status":"\u041c\u0430\u043c\u0435 \u0437\u044f\u0442\u044c \u043d\u0435 \u043d\u0443\u0436\u0435\u043d. \u0422\u0435\u043b\u0435\u0444\u043e\u043d \u043f\u043e\u0442\u0435\u0440\u044f\u043b\u0430. \u0414\u043e\u043c\u0430\u0448\u043d\u0435\u0433\u043e \u043d\u0435\u0442. \u041a\u043e\u0444\u0435 \u043d\u0435 \u043f\u044c\u044e. \u041c\u0430\u0448\u0438\u043d\u043e\u0439 \u043d\u0435 \u0443\u0434\u0438\u0432\u0438\u0448\u044c. \u0412\u0434\u0443\u0432\u0430\u0442\u0435\u043b\u044f\u043c - \u0432\u0434\u0443\u0432\u0430\u0439\u0442\u0435 \u0441\u0435\u0431\u0435 \u0432 \u043a\u0443\u043b\u0430\u0447\u043e\u043a))",
                "online":false,
                "avatars":{"big":"http:\/\/cs1696.vkontakte.ru\/u51993064\/96762451\/m_0c0b8072.jpg",
                "small":"http:\/\/cs1696.vkontakte.ru\/u51993064\/96762451\/s_a13a7cd6.jpg"},
                "geo":{"city":"\u041c\u043e\u0441\u043a\u0432\u0430","city_id":"1","distance":null,"coordinates":{"lat":null,"lng":null}}}}}}
*/
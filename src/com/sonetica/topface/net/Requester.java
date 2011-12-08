package com.sonetica.topface.net;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.utils.Utils;
import android.os.Handler;
import android.os.Message;

/*

“service”: “serviceName”,
“data”: {},
“ssid”: “0204fbb2774b070a2df0351ea8849831”

*/
public class Requester {
  public static String url = "http://api.topface.ru/?v=1";
  // Requester Response Types
  public static final int OK = 0;
  public static final int ERROR = 1;
  // Data
  //---------------------------------------------------------------------------
  private static String sendRequest(String data) {

    String response = null;
    try {
      response = Http.httpSendTpRequest(url,data);
      Utils.log(null,"response:"+response);
      JSONObject obj = new JSONObject(response);
      obj = obj.getJSONObject("result");
      if(obj==null)
        return null;
      response = obj.getString("ssid");
    } catch(Exception e) {
      e.printStackTrace();
    }

    return response;
  }
  //---------------------------------------------------------------------------
  public static void sendAuth(Auth packet,Handler handler) {
    try {
      JSONObject packetJson = new JSONObject();
      packetJson.put("service","auth");
      packetJson.put("data",new JSONObject()
                            .put("sid",packet.sid)
                            .put("token",packet.token)
                            .put("platform",packet.platform));
      String ssid = sendRequest(packetJson.toString());
      Message message = new Message();
      message.arg1 = OK;
      message.obj  = ssid;
      handler.sendMessage(message);
    } catch(JSONException e) {
      Message message = new Message();
      message.arg1 = ERROR;
      message.obj  = null;
      handler.sendMessage(null);
    }
  }
  //---------------------------------------------------------------------------
  public static void sendTop(Top packet,Handler handler) {
    try {
      JSONObject packetJson = new JSONObject();
      packetJson.put("service","top");
      packetJson.put("data",new JSONObject()
                            .put("sex",packet.sex)
                            .put("city",packet.city));
      packetJson.put("ssid",packet.ssid);
      String ssid = sendRequest(packetJson.toString());
      Message message = new Message();
      message.arg1 = OK;
      message.obj  = ssid;
      handler.sendMessage(message);
    } catch(JSONException e) {
      Message message = new Message();
      message.arg1 = ERROR;
      message.obj  = null;
      handler.sendMessage(null);
    }
  }
  //---------------------------------------------------------------------------
  public static void sendProfile(Profile packet,Handler handler) {
    try {
      JSONObject packetJson = new JSONObject();
      packetJson.put("service","profile");
      packetJson.put("data",new JSONObject());
      packetJson.put("ssid",packet.ssid);
      String ssid = sendRequest(packetJson.toString());
      Message message = new Message();
      message.arg1 = OK;
      message.obj  = ssid;
      handler.sendMessage(message);
    } catch(JSONException e) {
      Message message = new Message();
      message.arg1 = ERROR;
      message.obj  = null;
      handler.sendMessage(null);
    }
  }
  //---------------------------------------------------------------------------
  public static void sendProfiles(Profiles packet,Handler handler) {
    try {
      JSONObject packetJson = new JSONObject();
      packetJson.put("service","profiles");
      packetJson.put("data",new JSONObject().put("uids",new JSONArray(packet.uids)));
      packetJson.put("ssid",packet.ssid);
      String ssid = sendRequest(packetJson.toString());
      Message message = new Message();
      message.arg1 = OK;
      message.obj  = ssid;
      handler.sendMessage(message);
    } catch(JSONException e) {
      Message message = new Message();
      message.arg1 = ERROR;
      message.obj  = null;
      handler.sendMessage(null);
    }
  }
  //---------------------------------------------------------------------------
}

/*
//регистрация через сервер topface
String url = "http://api.topface.ru/?v=1";
String request = "";

AuthToken.Token token = new AuthToken(this).getToken(); 

JSONObject obj = new JSONObject();
try {
obj.put("service","auth");
 JSONObject data = new JSONObject();
 data.put("sid",token.getUserId());
 data.put("token",token.getTokenKey());
 data.put("platform",token.getSocialNet());
obj.put("data",data);
} catch(JSONException e) {
e.printStackTrace();
}

try {
request=request+obj.toString();
String response = Http.httpSendTpRequest(url,request);
response+="";
} catch(Exception e) { 
Utils.log(null,">>>> "+e.getMessage()); }
*/

// Tops
/*
{"result":{"top":[
{"uid":"9626403","photo":"2","liked":"98"},
{"uid":"1504738","photo":"14","liked":"98"},
{"uid":"7756978","photo":"3","liked":"97"},
{"uid":"5963313","photo":"2","liked":"97"},
{"uid":"17201895","photo":"5","liked":"97"},
{"uid":"9691018","photo":"1","liked":"96"},
{"uid":"8092917","photo":"1","liked":"96"},
{"uid":"27244101","photo":"3","liked":"96"},
{"uid":"23892788","photo":"9","liked":"96"},
{"uid":"1868848","photo":"26","liked":"96"},
{"uid":"13338700","photo":"3","liked":"96"},
{"uid":"9187791","photo":"5","liked":"95"},
{"uid":"9116726","photo":"118","liked":"95"},
{"uid":"3676656","photo":"6","liked":"95"},
{"uid":"28329573","photo":"3","liked":"95"},
{"uid":"28273641","photo":"1","liked":"95"},
{"uid":"23892788","photo":"12","liked":"95"},
{"uid":"2361772","photo":"1","liked":"95"},
{"uid":"22944403","photo":"1","liked":"95"},
{"uid":"16390517","photo":"11","liked":"95"},
{"uid":"13926250","photo":"7","liked":"95"},
{"uid":"13338700","photo":"10","liked":"95"},
{"uid":"12679436","photo":"1","liked":"95"},
{"uid":"9375012","photo":"11","liked":"94"},
{"uid":"8852760","photo":"1","liked":"94"},
{"uid":"8093715","photo":"10","liked":"94"},
{"uid":"7788516","photo":"3","liked":"94"},
{"uid":"7119927","photo":"4","liked":"94"},
{"uid":"6574073","photo":"1","liked":"94"},
{"uid":"6029086","photo":"1","liked":"94"},
{"uid":"3700117","photo":"1","liked":"94"},
{"uid":"28952887","photo":"1","liked":"94"},
{"uid":"28944051","photo":"1","liked":"94"},
{"uid":"6728626","photo":"9","liked":"89"}]}}

*/
// Profile
/*

{"result":{
"first_name":"\u041f\u0430\u0432\u0435\u043b",
"age":"27",
"sex":"1",
"unread_rates":18,
"unread_likes":11,
"unread_messages":1,
"photo_url":"http:\/\/cs146.vkontakte.ru\/u500235\/15561066\/s_ec4ab6d9.jpg",
"city":"\u0421\u0430\u043d\u043a\u0442-\u041f\u0435\u0442\u0435\u0440\u0431\u0443\u0440\u0433",
"money":6,
"power":0}}

*/

// Profiles
/*
{"result":{"profiles":
{"9626403":{"uid":9626403,"platform":"st","first_name":"\u042e\u043b\u044c\u0447\u0438\u043a","first_name_translit":"YUlchik","age":"23","sex":"0","last_visit":"2011-12-02 10:02:37","status":"\u0437\u0430\u043c\u0443\u0436\u0435\u043c!","online":true,"avatars":{"big":"http:\/\/cs10564.vkontakte.ru\/u100164185\/119470777\/m_cf38468c.jpg","small":"http:\/\/cs10564.vkontakte.ru\/u100164185\/119470777\/s_fd7c2418.jpg"},"geo":{"city":"\u0421\u0430\u043d\u043a\u0442-\u041f\u0435\u0442\u0435\u0440\u0431\u0443\u0440\u0433","city_id":"2","distance":null,"coordinates":{"lat":null,"lng":null}}},
"1504738":{"uid":1504738,"platform":"st","first_name":"\u041e\u043b\u0435\u043d\u044c\u043a\u0430","first_name_translit":"Olga","age":"23","sex":"0","last_visit":"2011-12-08 13:13:21","status":"If I look you in the eye, I swear I'll die, 'cos you kill everything you love...","online":false,"avatars":{"big":"http:\/\/cs11387.vkontakte.ru\/u3796633\/143916321\/m_c9e7ff63.jpg","small":"http:\/\/cs11387.vkontakte.ru\/u3796633\/143916321\/s_893c0835.jpg"},"geo":{"city":"\u0421\u0430\u043d\u043a\u0442-\u041f\u0435\u0442\u0435\u0440\u0431\u0443\u0440\u0433","city_id":"2","distance":null,"coordinates":{"lat":59.859831,"lng":30.290158}}},
"7756978":{"uid":7756978,"platform":"st","first_name":"\u041a\u0440\u0438\u0441\u0442\u0438\u043d\u0430","first_name_translit":"Kristina","age":"17","sex":"0","last_visit":"2011-11-26 01:27:47","status":"","online":true,"avatars":{"big":"http:\/\/cs10675.vkontakte.ru\/u1939474\/93125899\/m_34a0191e.jpg","small":"http:\/\/cs10675.vkontakte.ru\/u1939474\/93125899\/s_bf836555.jpg"},"geo":{"city":"\u0421\u0430\u043d\u043a\u0442-\u041f\u0435\u0442\u0435\u0440\u0431\u0443\u0440\u0433","city_id":"2","distance":null,"coordinates":{"lat":null,"lng":null}}},
"5963313":{"uid":5963313,"platform":"st","first_name":"\u042e\u043b\u0438\u044f","first_name_translit":"JL","age":"22","sex":"0","last_visit":"2011-12-08 10:48:41","status":"\u043e_\u041e","online":false,"avatars":{"big":"http:\/\/cs9411.vkontakte.ru\/u3087352\/95653730\/m_5ac365df.jpg","small":"http:\/\/cs9411.vkontakte.ru\/u3087352\/95653730\/s_2a356e89.jpg"},"geo":{"city":"\u0421\u0430\u043d\u043a\u0442-\u041f\u0435\u0442\u0435\u0440\u0431\u0443\u0440\u0433","city_id":"2","distance":null,"coordinates":{"lat":null,"lng":null}}},
"17201895":{"uid":17201895,"platform":"st","first_name":"Dasha","first_name_translit":"Dasha","age":"25","sex":"0","last_visit":"2011-12-08 13:43:24","status":"","online":false,"avatars":{"big":"http:\/\/cs9297.vkontakte.ru\/u612857\/b_dce9f7dc.jpg","small":"http:\/\/cs9297.vkontakte.ru\/u612857\/e_5f0b120e.jpg"},"geo":{"city":"\u0421\u0430\u043d\u043a\u0442-\u041f\u0435\u0442\u0435\u0440\u0431\u0443\u0440\u0433","city_id":"2","distance":null,"coordinates":{"lat":60.0470229852,"lng":30.3598770685}}}}}}
*/
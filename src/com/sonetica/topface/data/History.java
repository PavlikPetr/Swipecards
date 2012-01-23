package com.sonetica.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.utils.Debug;

public class History extends AbstractData {
  // Data
  public int created;  // время создания сообщения
  public int owner_id; // идентификатор пользователя, отправившего сообщение
  public int gift;     // идентификатор подарка. Если сообщение является подарком
  public int code;     // код входящего уведомления. Если сообщение является уведомлением
  public String text;  // текст сообщения. Если входящее сообщение является текстовым
  //---------------------------------------------------------------------------
  public static LinkedList<History> parse(Response response) {
    LinkedList<History> historyList = new LinkedList<History>();
    try {
      JSONArray array = response.mJSONResult.getJSONArray("history");
      if(array.length()>0)
        for(int i=0;i<array.length();i++) {
          JSONObject item = array.getJSONObject(i);
          History history  = new History();
          history.created  = item.getInt("created");
          history.owner_id = item.getInt("owner_id");
          history.gift     = item.isNull("gift") ? 0 : item.getInt("gift");
          history.code     = item.isNull("code") ? 0 : item.getInt("code");
          history.text     = item.isNull("text") ? null : item.getString("text");
          historyList.add(history);
        }
    } catch(JSONException e) {
      Debug.log("History.class","Wrong response parsing: " + e);
    }
    return historyList; 
  }
  //---------------------------------------------------------------------------
}

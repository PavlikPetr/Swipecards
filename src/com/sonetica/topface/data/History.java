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
  public int type;     // тип сообщения
  public int gift;     // идентификатор подарка. Если сообщение является подарком
  public int code;     // код входящего уведомления. Если сообщение является уведомлением
  public String text;  // текст сообщения. Если входящее сообщение является текстовым
  // Constants
  public static final int DEFAULT = 0;           // По-умолчанию. Нигде не используется. Если возникает, наверное, надо что-то сделать
  public static final int PHOTO   = 1;           // Рекламное уведомление
  public static final int GIFT    = 2;           // Подарок
  public static final int MESSAGE = 3;           // Текстовое сообщение
  public static final int MESSAGE_WISH = 4;      // Тайное желание
  public static final int MESSAGE_SEXUALITY = 5; // Оценка сексуальности
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
          history.type     = item.getInt("type");
          
          switch(history.type) {
            case DEFAULT:
              history.text = item.getString("text");
              break;
            case PHOTO:
              history.code = item.getInt("code");
              break;
            case GIFT:
              history.gift = item.getInt("gift");
              break;
            case MESSAGE:
              history.text = item.getString("text");
              break;
            case MESSAGE_WISH:
              break;
            case MESSAGE_SEXUALITY:
              break;
            default:
              break;
          }
          historyList.add(history);
        }
    } catch(JSONException e) {
      Debug.log("History.class","Wrong response parsing: " + e);
    }
    return historyList; 
  }
  //---------------------------------------------------------------------------
}

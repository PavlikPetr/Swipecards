package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class History extends AbstractData {
  // Data
  public int owner_id; // идентификатор пользователя, отправившего сообщение
  public int type;     // тип сообщения
  public int gift;     // идентификатор подарка. Если сообщение является подарком
  public int code;     // код входящего уведомления. Если сообщение является уведомлением
  public long created; // время создания сообщения
  public String text;  // текст сообщения. Если входящее сообщение является текстовым
  // Constants
  public static final int DEFAULT = 0;           // По-умолчанию. Нигде не используется. Если возникает, наверное, надо что-то сделать
  public static final int PHOTO   = 1;           // Рекламное уведомление
  public static final int GIFT    = 2;           // Подарок
  public static final int MESSAGE = 3;           // Текстовое сообщение
  public static final int MESSAGE_WISH = 4;      // Тайное желание
  public static final int MESSAGE_SEXUALITY = 5; // Оценка сексуальности
  //---------------------------------------------------------------------------
  public static LinkedList<History> parse(ApiResponse response) {
    LinkedList<History> historyList = new LinkedList<History>();
    
    try {
      JSONArray array = response.mJSONResult.getJSONArray("history");
      if(array.length()>0)
        for(int i=0;i<array.length();i++) {
          JSONObject item = array.getJSONObject(i);
          History history  = new History();
          history.created  = item.optLong("created"); // время приходит в секундах *1000
          history.owner_id = item.optInt("owner_id");
          history.type     = item.optInt("type");
          
          switch(history.type) {
            case DEFAULT:
              history.text = item.optString("text");
              break;
            case PHOTO:
              history.code = item.optInt("code");
              break;
            case GIFT:
              history.gift = item.optInt("gift");
              break;
            case MESSAGE:
              history.text = item.optString("text");
              break;
            case MESSAGE_WISH:
              break;
            case MESSAGE_SEXUALITY:
              break;
            default:
              break;
          }
          historyList.addFirst(history);
        }
    } catch(Exception e) {
      Debug.log("History.class","Wrong response parsing: " + e);
    }
    
    return historyList; 
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

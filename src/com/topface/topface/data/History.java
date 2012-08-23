package com.topface.topface.data;

import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;

public class History extends AbstractData {
    // Data
    public int id;       // идентификатор сообщения
    public int uid; // идентификатор пользователя, отправившего сообщение
    public int target; // 1 для входящих сообщений, 0 для исходящих
    public int type;     // тип сообщения
    public int gift;     // идентификатор подарка. Если сообщение является подарком
    public String link;  // ссылка на изображение подарка. Поле устанавливается, если сообщение является подарком
    public int code;     // код входящего уведомления. Если сообщение является уведомлением
    public long created; // время создания сообщения
    public String text;  // текст сообщения. Если входящее сообщение является текстовым
    public double longitude; // координаты - долгота
    public double latitude; // координаты - широта
    public boolean currentLocation = false; // флаг , указывающий на тип карты (true - текущее местоположение, false - указанное на карте)
    
    // Constants
    public static final int DEFAULT = 0; // По-умолчанию. Нигде не используется. Если возникает, наверное, надо что-то сделать
    public static final int PHOTO = 1; // Рекламное уведомление
    public static final int GIFT = 2; // Подарок
    public static final int MESSAGE = 3; // Текстовое сообщение
    public static final int MESSAGE_WISH = 4; // Тайное желание
    public static final int MESSAGE_SEXUALITY = 5; // Оценка сексуальности
    public static final int LIKE = 6; // Событие “понравилось”
    public static final int SYMPHATHY = 7; // Событие “симпатия”
    public static final int MESSAGE_WINK = 8; // подмигивание
    public static final int RATE = 9; // Оценка
    
    public static final int MAP = 11; // Текущее местоположение    
        
    public static final int USER_MESSAGE = 0;
    public static final int FRIEND_MESSAGE = 1;
    //---------------------------------------------------------------------------
    public static LinkedList<History> parse(ApiResponse response) {
        LinkedList<History> historyList = new LinkedList<History>();

        try {
            JSONArray array = response.mJSONResult.getJSONArray("feed");
            if (array.length() > 0)
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);
                    History history = new History();
                    history.id = item.optInt("id");
                    history.created = item.optLong("created") * 1000; // время приходит в секундах *1000
                    history.uid = item.optInt("uid");
                    history.type = item.optInt("type");
                    history.target = item.optInt("target");
                    
                    history.text = item.optString("text");
                    history.code = item.optInt("code");
                    history.gift = item.optInt("gift");
                    history.link = item.optString("link");
                    /*
                    switch (history.type) {
                        case DEFAULT:
                            history.text = item.optString("text");
                            break;
                        case PHOTO:
                            history.code = item.optInt("code");
                            break;
                        case GIFT:
                            history.gift = item.optInt("gift");
                            history.link = item.optString("link");
                            break;
                        case MESSAGE:
                            history.text = item.optString("text");
                            break;
                        case WISH:
                        case SEXUALITY:
                        case WINK:
                        default:
                            history.text = item.optString("text");
                            break;
                    }
                    */
                    historyList.addFirst(history);
                }
        } catch(Exception e) {
            Debug.log("History.class", "Wrong response parsing: " + e);
        }

        return historyList;
    }
    //---------------------------------------------------------------------------
    public int getUid() {
        return uid;
    };
    //---------------------------------------------------------------------------

}

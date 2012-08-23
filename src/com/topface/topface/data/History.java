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

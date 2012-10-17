package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;

public class History extends AbstractDataWithPhotos {
    // Data
    public static boolean more; // имеются ли в ленте ещё симпатии для пользователя
    public int type; // тип сообщения
    public int id;   // идентификатор сообщения
    public int uid; // идентификатор пользователя, отправившего сообщение
    public long created; // время создания сообщения
    public int target; // 1 для входящих сообщений, 0 для исходящих
    public boolean unread; // флаг показывающий, является ли элемент ленты просмотренным
    public String first_name; // имя отправителя в локали пользователя
    public int age;  // возраст отправителя
    public boolean online; // флаг показывающий, находится ли отправитель онлайн
    public String text;  // текст сообщения. Если входящее сообщение является текстовым

    public int gift;     // идентификатор подарка. Если сообщение является подарком
    public String link;  // ссылка на изображение подарка. Поле устанавливается, если сообщение является подарком
//    public int code;     // код входящего уведомления. Если сообщение является уведомлением

    public int city_id; // идентификатор города отправителя оценки
    public String city_name; // название города пользователя
    public String city_full; // полное название города пользвоателя

    public double longitude; // координаты - долгота
    public double latitude; // координаты - широта
    public boolean currentLocation = false; // флаг , указывающий на тип карты (true - текущее местоположение, false - указанное на карте)

    public static LinkedList<History> parse(ApiResponse response) {
        LinkedList<History> historyList = new LinkedList<History>();

        try {
            History.more = response.jsonResult.optBoolean("more");

            JSONArray array = response.jsonResult.getJSONArray("feed");
            if (array.length() > 0)
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);

                    History history = new History();
                    history.id = item.optInt("id");
                    history.created = item.optLong("created") * 1000; // время приходит в секундах *1000
                    history.uid = item.optInt("uid");
                    history.type = item.optInt("type");
                    history.target = item.optInt("target");
                    history.unread = item.optBoolean("unread");
                    history.first_name = item.optString("first_name");
                    history.age = item.optInt("age");
                    history.online = item.optBoolean("online ");
                    history.text = item.optString("text");

//                    history.code = item.optInt("code");
                    history.gift = item.optInt("gift");
                    history.link = item.optString("link");

                    // city  
                    JSONObject city = item.getJSONObject("city");
                    history.city_id = city.optInt("id");
                    history.city_name = city.optString("name");
                    history.city_full = city.optString("full");

                    initPhotos(item, history);

                    historyList.addFirst(history);
                }
        } catch (Exception e) {
            Debug.log("History.class", "Wrong response parsing: " + e);
        }

        return historyList;
    }

}

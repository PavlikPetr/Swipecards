package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Данные при запросе сервиса leaders содержащий лидеров приложения
 */
public class Leaders extends AbstractData {

    /**
     * Массив экземпляров лидеров приложения
     */
    public ArrayList<LeaderUser> leaders;

    /**
     * Парсит ответ от сервера
     * @param response ответ сервера
     * @return объект типа Leaders, реализующий ответ от сервиса leaders
     */
    public static Leaders parse(ApiResponse response) {
        Leaders leadersResponse = new Leaders();
        leadersResponse.leaders = new ArrayList<LeaderUser>();
        try {
            JSONArray leadersArray = response.mJSONResult.getJSONArray("leaders");
            for (int i = 0; i < leadersArray.length(); i++) {
                JSONObject item = leadersArray.getJSONObject(i);
                LeaderUser user = new LeaderUser();

                user.user_id = item.getInt("user_id");
                user.name = item.getString("name");
                user.status = item.getString("status");
                user.city = parseCity(item.getJSONObject("city"));
                user.photo = parsePhoto(item.getJSONObject("photo"));

                leadersResponse.leaders.add(user);
            }
        } catch(Exception e) {
            Debug.error("Leaders wrong response parsing", e);
        }

        return leadersResponse;
    }

    /**
     * Парсит массив с фотографиями лидера
     * @param photosItem объект JSON который нужно распарсить
     * @return объект UserPhotos с фотографиями пользователя
     * @throws JSONException
     */
    private static UserPhotos parsePhoto(JSONObject photosItem) throws JSONException {
        JSONObject linksItem = photosItem.getJSONObject("links");
        Iterator photoKeys = linksItem.keys();

        int id = photosItem.getInt("id");
        HashMap<String, String> links = new HashMap<String, String>();

        while (photoKeys.hasNext()) {
            String key = photoKeys.next().toString();
            links.put(key, linksItem.getString(key));
        }

        return new UserPhotos(id, links);
    }

    /**
     *
     * @param cityItem объект JSON, который будем парсить
     * @return объект города лидера LeaderUser.LeaderCity
     * @throws JSONException
     */
    private static LeaderUser.LeaderCity parseCity(JSONObject cityItem) throws JSONException {
        LeaderUser.LeaderCity city = new LeaderUser.LeaderCity();

        city.id = cityItem.getInt("id");
        city.name = cityItem.getString("name");
        city.full = cityItem.getString("full");

        return city;
    }

    /**
     * Класс реализующий структуру данных лидера
     */
    public static class LeaderUser {
        /**
         * id пользователя
         */
        public int user_id;
        /**
         * имя лидера в текущей локали пользователя
         */
        public String name;

        /**
         * экземпляр города лидера. Экземпляр имеет следующие поля:
         */
        public LeaderCity city;

        /**
         * экземпляр фотографии пользователя, с которой он встал в лидеры
         */
        public UserPhotos photo;

        /**
         * статус пользователя
         */
        public String status;

        /**
         * Класс реализующий данные с информацией о городе лидера
         */
        public static class LeaderCity {
            /**
             * идентификатор города
             */
            public int id;

            /**
             * наименование города в локали указанной при авторизации
             */
            public String name;

            /**
             * полное наименование города с указанием региона, если он определен. Отдается в локали пользователя, указанной при авторизации
             */
            public String full;
        }

    }
}

package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileRequest extends AbstractApiRequest {
    // Data
    public static final String PROFILE_UPDATE_ACTION = "com.topface.topface.UPDATE_PROFILE";
    public static final String SERVICE_NAME = "profile";
    public int part; // часть профиля, необходимая для загрузки
    //public String  fields;  //массив интересующих полей профиля
    // Constants
    public static final int P_ALL = 0;
    public static final int P_NOTIFICATION = 1;
    public static final int P_FILTER = 2;
    public static final int P_QUESTIONARY = 3;
    public static final int P_ALBUM = 4;
    public static final int P_INFO = 5;
    public static final int P_DASHBOARD = 6;

    public ProfileRequest(Context context) {
        super(context);
        doNeedAlert(false); //чтобы не предупреждать пользователя алертом о пропаже инета
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {

        JSONArray fields;
        switch (part) {
            case P_NOTIFICATION:
                fields = getNotification();
                break;
            case P_FILTER:
                fields = getFilter();
                break;
            case P_QUESTIONARY:
                fields = getQuestionary();
                break;
            case P_ALBUM:
                fields = getAlbum();
                break;
            case P_DASHBOARD:
                fields = getDashboard();
                break;
            case P_INFO:
                fields = getInfo();
                break;
            case P_ALL:
                fields = null;
                break;
            default:
                fields = new JSONArray();
                break;
        }
        if (fields != null) {
            return new JSONObject()
                    .put("fields", fields)
                            //При запросе профиля считаем текущего пользователя "гостем"
                    .put("visitor", true);
        }
        return new JSONObject()
                //При запросе профиля считаем текущего пользователя "гостем"
                .put("visitor", true);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    private JSONArray getNotification() {
        JSONArray array = new JSONArray();
        array.put("money").put("power").put("average_rate").put("unread_rates").put("unread_likes").put("unread_messages").put("unread_symphaties");

        return array;
    }

    private JSONArray getFilter() {
        JSONArray array = new JSONArray();
        array.put("dating");

        return array;
    }

    private JSONArray getQuestionary() {
        JSONArray array = new JSONArray();
        array.put("questionary");

        return array;
    }

    private JSONArray getAlbum() {
        JSONArray array = new JSONArray();
        array.put("album");

        return array;
    }

    private JSONArray getInfo() {
        JSONArray array = getNotification();
        array.put("uid").put("first_name").put("age").put("sex").put("city").put("avatars").put("status");

        return array;
    }

    private JSONArray getDashboard() {
        JSONArray array = getNotification();
        array.put("city").put("dating").put("avatars").put("flags");

        return array;
    }

}

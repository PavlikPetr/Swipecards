package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionaryRequest extends ApiRequest {
    // Data
    public static final String SERVICE = "user.setQuestionary";
    public int jobId = -1; // идентификатор рабочей партии пользователя
    public String job; // описание оригинальной работы пользователя
    public int statusId = -1; // идентификатор предопределенного статуса
    // пользователя
    public String status; // описание оригинального статуса пользователя
    public int educationId = -1; // идентификатор предопределенного уровня
    // образования пользователя
    public int marriageId = -1; // идентификатор предопределенного семейного
    // положения пользователя
    public int financesId = -1; // идентификатор предопределенного финансового
    // положения пользователя
    public int characterId = -1; // идентификатор предопределенной
    // характеристики пользователя
    public int smokingId = -1; // идентификатор предопределенного отношения к
    // курению пользователя
    public int alcoholId = -1; // идентификатор предопределенного отношения к
    // алкоголю пользователя
    public int fitnessId = -1; // идентификатор предопределенного Fотношения к
    // спорту пользователя
    public int communicationId = -1; // идентификатор предопределенного
    // отношения к коммуникациям
    // пользователя
    public int weight = -1; // вес пользователя
    public int height = -1; // рост пользователя

    public int hairId = -1; // идентификатор цвета воло пользователя
    public int eyeId = -1; // идентификатор цвета глаз пользователя
    public int breastId = -1; // идентификатор рамера груди пользовательницы
    public int childrenId = -1; // идентификатор количества детей пользователя
    public int residenceId = -1; // идентификатор условий проживания
    // пользователя
    public int carId = -1; // идентификатор наличия автомобиля у пользователя
    public String car; // текстовое описание присутствующего автомобиля у
    // пользователя
    public String firstDating; // текстовое описание свидания пользователя
    public String achievements; // текстовое описание достижений пользователя
    // {Array} countries - массив идентификаторов стран, в которых бывал
    // пользователь
    public String restaurants; // описание предпочитаемых ресторанов
    // пользователя
    public String valuables; // описание ценностей пользователя
    public String aspirations; // описание достижений пользователя

    public QuestionaryRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("car", car).put("firstDating", firstDating)
                .put("achievements", achievements)
                .put("restaurants", restaurants)
                .put("valuables", valuables)
                .put("aspirations", aspirations)
                .put("status", status);

        if (statusId != -1)
            data.put("statusId", statusId);
        if (weight != -1)
            data.put("weight", weight);
        if (height != -1)
            data.put("height", height);
        if (educationId != -1)
            data.put("educationId", educationId);
        if (marriageId != -1)
            data.put("marriageId", marriageId);
        if (financesId != -1)
            data.put("financesId", financesId);
        if (characterId != -1)
            data.put("characterId", characterId);
        if (smokingId != -1)
            data.put("smokingId", smokingId);
        if (alcoholId != -1)
            data.put("alcoholId", alcoholId);
        if (fitnessId != -1)
            data.put("fitnessId", fitnessId);
        if (communicationId != -1)
            data.put("communicationId", communicationId);
        if (hairId != -1)
            data.put("hairId", hairId);
        if (eyeId != -1)
            data.put("eyeId", eyeId);
        if (childrenId != -1)
            data.put("childrenId", childrenId);
        if (residenceId != -1)
            data.put("residenceId", residenceId);
        if (carId != -1)
            data.put("carId", carId);
        if (breastId != -1)
            data.put("breastId", breastId);
        if (jobId != -1)
            data.put("jobId", jobId);

        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE; // To change body of implemented methods use File |
        // Settings | File Templates.
    }

}

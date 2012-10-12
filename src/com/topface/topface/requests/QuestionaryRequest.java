package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestionaryRequest extends ApiRequest {
    // Data
    private String service = "questionary";
    public int weight = -1;        // вес пользователя
    public int height = -1;        // рост пользователя
    public int jobid = -1;        // идентификатор рабочей партии пользователя
    public String job;             // описание оригинальной работы пользователя
    public int statusid = -1;      // идентификатор предопределенного статуса пользователя
    public String status;          // описание оригинального статуса пользователя
    public int educationid = -1;   // идентификатор предопределенного уровня образования пользователя
    public int marriageid = -1;   // идентификатор предопределенного семейного положения пользователя
    public int financesid = -1;   // идентификатор предопределенного финансового положения пользователя
    public int characterid = -1;   // идентификатор предопределенной характеристики пользователя
    public int smokingid = -1;   // идентификатор предопределенного отношения к курению пользователя
    public int alcoholid = -1;   // идентификатор предопределенного отношения к алкоголю пользователя
    public int fitnessid = -1;   // идентификатор предопределенного отношения к спорту пользователя
    public int communicationid = -1; // идентификатор предопределенного отношения к коммуникациям пользователя

    public QuestionaryRequest(Context context) {
        super(context);
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            JSONObject data = new JSONObject();
            if (weight != -1)
                data.put("weight", weight);
            if (height != -1)
                data.put("height", height);
            if (height != -1)
                data.put("height", height);

            if (educationid != -1)
                data.put("educationid", educationid);
            if (marriageid != -1)
                data.put("marriageid", marriageid);
            if (financesid != -1)
                data.put("financesid", financesid);
            if (characterid != -1)
                data.put("characterid", characterid);
            if (smokingid != -1)
                data.put("smokingid", smokingid);
            if (alcoholid != -1)
                data.put("alcoholid", alcoholid);
            if (fitnessid != -1)
                data.put("fitnessid", fitnessid);
            if (communicationid != -1)
                data.put("communicationid", communicationid);
      /*
        if(name!=null)
          data.put("name",name);
      */
            root.put("data", data);
        } catch (JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }

}

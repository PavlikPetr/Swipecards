package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;
import android.content.Context;

public class QuestionaryRequest extends ApiRequest {
    // Data
    private String service = "questionary";
    public int jobid = -1; // идентификатор рабочей партии пользователя
    public String job; // описание оригинальной работы пользователя
    public int statusid = -1; // идентификатор предопределенного статуса пользователя
    public String status; // описание оригинального статуса пользователя
    public int educationid = -1; // идентификатор предопределенного уровня образования пользователя
    public int marriageid = -1; // идентификатор предопределенного семейного положения пользователя
    public int financesid = -1; // идентификатор предопределенного финансового положения пользователя
    public int characterid = -1; // идентификатор предопределенной характеристики пользователя
    public int smokingid = -1; // идентификатор предопределенного отношения к курению пользователя
    public int alcoholid = -1; // идентификатор предопределенного отношения к алкоголю пользователя
    public int fitnessid = -1; // идентификатор предопределенного отношения к спорту пользователя
    public int communicationid = -1; // идентификатор предопределенного отношения к коммуникациям пользователя    
    public int weight = -1; // вес пользователя
    public int height = -1; // рост пользователя
    
    public int hairid = -1; // идентификатор цвета воло пользователя
    public int eyeid = -1; // идентификатор цвета глаз пользователя
    public int breastid = -1; // идентификатор рамера груди пользовательницы
    public int childrenid = -1; // идентификатор количества детей пользователя
    public int residenceid = -1; // идентификатор условий проживания пользователя
    public int carid = -1; // идентификатор наличия автомобиля у пользователя
    public String car; // текстовое описание присутствующего автомобиля у пользователя
    public String firstdating; // текстовое описание свидания пользователя
    public String achievements; // текстовое описание достижений пользователя
    //{Array} countries - массив идентификаторов стран, в которых бывал пользователь
    public String restaurants; // описание предпочитаемых ресторанов пользователя
    public String valuables; // описание ценностей пользователя
    public String aspirations; // описание достижений пользователя

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
            
            if(hairid != -1)
                data.put("hairid", hairid);
            if(eyeid != -1)
                data.put("eyeid", eyeid);
            if(childrenid != -1)
                data.put("childrenid", childrenid);
            if(residenceid != -1)
                data.put("residenceid" , residenceid);
            if(carid != -1)
                data.put("carid" ,carid);
            if(car != null)
                data.put("car" ,car);
            if(firstdating != null)
                data.put("firstdating" ,firstdating);
            if(achievements != null)
                data.put("achievements" ,achievements);
//            if(countries  != null)
//                data.put("countries " ,countries );
            if(restaurants != null)
                data.put("restaurants " ,restaurants );
            if(valuables != null)
                data.put("valuables" ,valuables);
            if(aspirations != null)
                data.put("aspirations" ,aspirations);
            
            
            /*if(name!=null)
             * data.put("name",name); */
            root.put("data", data);
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }

}

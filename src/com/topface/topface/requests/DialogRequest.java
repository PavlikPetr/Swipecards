package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;
import android.content.Context;

public class DialogRequest extends ApiRequest {
    // Data
    private String service = "dialogs";
    public int limit; // максимальное количество запрашиваемых диалогов. ОДЗ: 0 < limit <= 50
    public int before; // идентификатор последнего диалога для отображения. В случае отсутствия параметра диалоги возвращаются от последнего
    //---------------------------------------------------------------------------
    public DialogRequest(Context context) {
        super(context);
    }
    //---------------------------------------------------------------------------
    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            JSONObject dataFields = new JSONObject();
            dataFields.put("limit", limit);
            if (before > 0)
                dataFields.put("before", before);
            root.put("data", dataFields);
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
    //---------------------------------------------------------------------------
}

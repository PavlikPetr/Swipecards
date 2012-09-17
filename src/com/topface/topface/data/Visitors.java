package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.Debug;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedList;

public class Visitors extends AbstractData {

    public LinkedList<Visitor> visitors;

    public static Visitors parse(ApiResponse response) {
        Visitors visitors = null;
        try {
            visitors = new Visitors();
            visitors.visitors = new LinkedList<Visitor>();
            JSONArray array = response.mJSONResult.getJSONArray("visitors");
            for (int i = 0; i < array.length(); i++) {
                visitors.visitors.add(
                        Visitor.parseVisitor(array.getJSONObject(i))
                );
            }
        } catch (JSONException e) {
            Debug.error(e);
        }
        return visitors;
    }

}

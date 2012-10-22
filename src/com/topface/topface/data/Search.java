package com.topface.topface.data;

import com.topface.topface.requests.ApiResponse;
import org.json.JSONArray;

import java.util.LinkedList;

public class Search extends LinkedList<SearchUser> {

    public Search(ApiResponse response) {
        this();
        if (response != null && response.jsonResult != null) {
            fillList(response.jsonResult.optJSONArray("users"));
        }
    }

    private void fillList(JSONArray users) {
        if (users != null) {
            for (int i = 0; i < users.length(); i++) {
                add(new SearchUser(users.optJSONObject(i)));
            }
        }
    }

    public Search() {
        super();
    }
}

package com.topface.topface.data;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;

public class Invite extends AbstractData {
    /**
     * результат выполнения запроса
     */
    public boolean completed;

    public static Invite parse(ApiResponse response) {
        Invite invite = new Invite();
        try {
            invite.completed = response.jsonResult.getBoolean("completed");
        } catch (Exception e) {
            Debug.error("Invite wrong response parsing", e);
            invite.completed = false;
        }

        return invite;
    }

    public static boolean isCompleted(ApiResponse response) {
        return parse(response).completed;
    }
}

package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("UnusedDeclaration")
public class ComplainRequest extends ApiRequest {

    public static final String SERVICE_NAME = "moderation.complain";

    public enum ClassNames {WALL_MSG, PHOTO, PRIVATE_MSG, USER, LEADER}

    public enum TypesNames {SWEARING, ERO, FAKE_USER, FAKE_DATA, FAKE_PHOTO, PORN, SPAM}

    private int userId;
    private int complainClass;
    private int complainType;

    private String description;
    private Integer photoId;
    private String feedId;

    public ComplainRequest(Context context, int userId, ClassNames className, TypesNames typeName) {
        super(context);
        this.userId = userId;
        complainClass = className.ordinal() + 1; //ordinal возвращает порядок с нуля
        complainType = typeName.ordinal() + 1;
    }

    public ComplainRequest(Context context, int userId, ClassNames className, TypesNames typeName, int photoId) {
        super(context);
        this.userId = userId;
        complainClass = className.ordinal() + 1;
        complainType = typeName.ordinal() + 1;
        this.photoId = photoId;
    }

    public ComplainRequest(Context context, int userId, ClassNames className, TypesNames typeName, String feedId) {
        super(context);
        this.userId = userId;
        complainClass = className.ordinal() + 1;
        complainType = typeName.ordinal() + 1;
        this.feedId = feedId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPhotoId(Integer photoId) {
        this.photoId = photoId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject object = new JSONObject().put("targetId", userId)
                .put("class", complainClass)
                .put("type", complainType);
        if (description != null) {
            object.put("description", description);
        }
        if (photoId != null) {
            object.put("photoId", photoId);
        }
        if (feedId != null) {
            object.put("feedId", feedId);
        }
        return object;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}

package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class FeedRequest extends ApiRequest {


    // Data
    public int limit;   // максимальное количество запрашиваемых диалогов. ОДЗ: 0 < limit <= 50
    public String to;  // идентификатор последнего диалога для отображения. В случае отсутствия параметра диалоги возвращаются от последнего
    public String from;  // идентификатор последнего диалога для запроса новых сообщений после данного идентификатора
    public boolean unread;  // параметр получения только тех диалогов, в которых есть непрочитанные сообщения
    public int type = -1; // нужен исключительно для избранных - показывает кого подгрузить - избранных или поклонников. Если не указан, подгружаются поклонники
    private FeedService mService;
    //private boolean leave; //Оставить сообщения не прочитанными

    public static enum FeedService {
        DIALOGS, LIKES, MUTUAL, VISITORS, BLACK_LIST, BOOKMARKS, FANS, ADMIRATIONS
    }

    public FeedRequest(FeedService service, Context context) {
        super(context);
        //Костыль для избранных
        if(service == FeedService.BOOKMARKS) {
            type = 0;
        } else {
            type = 1;
        }
        mService = service;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("limit", limit);
        data.put("new", unread);
        //data.put("leave", leave);
        if (to != null) {
            data.put("to", to);
        }

        if (from != null) {
            data.put("from", from);
        }

        if (type > -1) {
            data.put("type", type);
        }

        return data;
    }

    @Override
    public String getServiceName() {
        String service = null;
        switch (mService) {
            case DIALOGS:
                service = "dialog.getList";
                break;
            case LIKES:
                service = "likes.getList";
                service = "feedLikes";
                break;
            case MUTUAL:
                service = "mutual.getList";
                break;
            case VISITORS:
                service = "visitor.getList";
                break;
            case BLACK_LIST:
                service = "blacklist.getList";
                break;
            case FANS:
            case BOOKMARKS:
                service = "bookmark.getList";
                break;
            case ADMIRATIONS:
                service = "feedAdmirations";
                break;
        }
        return service;
    }

}

package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class FeedRequest extends AbstractApiRequest {
    // Data
    public int limit;   // максимальное количество запрашиваемых диалогов. ОДЗ: 0 < limit <= 50
    public int to;  // идентификатор последнего диалога для отображения. В случае отсутствия параметра диалоги возвращаются от последнего
    public int from;  // идентификатор последнего диалога для запроса новых сообщений после данного идентификатора
    public boolean unread;  // параметр получения только тех диалогов, в которых есть непрочитанные сообщения
    private FeedService mService;
    //private boolean leave; //Оставить сообщения не прочитанными

    public static enum FeedService {
        DIALOGS, LIKES, MUTUAL, VISITORS, BLACK_LIST
    }

    public FeedRequest(FeedService service, Context context) {
        super(context);
        mService = service;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("limit", limit);
        data.put("new", unread);
        //data.put("leave", leave);
        if (to > 0) {
            data.put("to", to);
        }

        if (from > 0) {
            data.put("from", from);
        }

        return data;
    }

    @Override
    public String getServiceName() {
        String service = null;
        switch (mService) {
            case DIALOGS:
                service = "dialogs";
                break;
            case LIKES:
                service = "feedLike";
                break;
            case MUTUAL:
                service = "feedSympathy";
                break;
            case VISITORS:
                service = "visitors";
                break;
            case BLACK_LIST:
                service = "blacklistGet";
        }
        return service;
    }

}

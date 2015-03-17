package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.loadcontollers.FeedLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

import org.json.JSONException;
import org.json.JSONObject;

public class FeedRequest extends LimitedApiRequest {


    // Data
    public String to;  // идентификатор последнего диалога для отображения. В случае отсутствия параметра диалоги возвращаются от последнего
    public String from;  // идентификатор последнего диалога для запроса новых сообщений после данного идентификатора
    public boolean unread;  // параметр получения только тех диалогов, в которых есть непрочитанные сообщения

    // признак того, что первый/последний элемент из предыдущего запроса были непрочитанными
    // используется для активации серверной сортировки вида "сначала новые"
    public UnreadStatePair previousUnreadState;

    private FeedService mService;
    public boolean leave; //Оставить сообщения не прочитанными

    public static enum FeedService {
        DIALOGS, LIKES, MUTUAL, VISITORS, BLACK_LIST, BOOKMARKS, FANS, ADMIRATIONS, GEO, PHOTOBLOG
    }

    public static class UnreadStatePair {
        public boolean wasFromInited = false;
        public boolean from = false;
        public boolean to;
    }

    public FeedRequest(FeedService service, Context context) {
        super(context);
        mService = service;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = super.getRequestData();
        data.put("leave", leave);

        // мы же заботимся о трафике - будем отсекать лишние поля в запросе
        boolean needSendPrevious = false;

        if (to != null) {
            data.put("to", to);
            needSendPrevious = true;
        }

        if (from != null) {
            data.put("from", from);
            needSendPrevious = true;
        }

        if (needSendPrevious && previousUnreadState != null) {
            data.put("fromUnread", previousUnreadState.from);
            data.put("toUnread", previousUnreadState.to);
        }

        return data;
    }

    @Override
    protected LoadController getLoadController() {
        return new FeedLoadController();
    }

    public int getLimit() {
        return mLimit;
    }

    public FeedRequest setPreviousUnreadState(UnreadStatePair newPreviousUnreadState) {
        previousUnreadState = newPreviousUnreadState;
        return this;
    }

    @Override
    public String getServiceName() {
        String service = null;
        switch (mService) {
            case DIALOGS:
                service = "dialog.getList";
                break;
            case LIKES:
                service = "like.getList";
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
            case PHOTOBLOG:
                service = "photofeed.getList";
                break;
            case FANS:
                service = "fan.getList";
                break;
            case BOOKMARKS:
                service = "bookmark.getList";
                break;
            case ADMIRATIONS:
                service = "admiration.getList";
                break;
        }
        return service;
    }

}

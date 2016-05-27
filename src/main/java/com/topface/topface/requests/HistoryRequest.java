package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.loadcontollers.ChatLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class HistoryRequest extends LimitedApiRequest {
    // Data
    public static final String service = "dialog.get";
    public int userid; // идентификатор пользователя для получения истории сообщений с ним текущего пользвоателя
    //public int offset; // смещение истории сообщений
    public String to; // идентификатор сообщения до которого будет осуществляться выборка истории
    public String from; //идентификатор сообщения после которого будет осуществляться выборка истории
    public String debug;
    public boolean leave = true; //Оставить сообщения не прочитанными
    private WeakReference<IRequestExecuted> mRequestExecutedWeakReference;

    public HistoryRequest(Context context, int userId) {
        super(context);
        userid = userId;
    }

    public HistoryRequest(Context context, int userId, IRequestExecuted requestExecuted) {
        this(context, userId);
        mRequestExecutedWeakReference = new WeakReference<>(requestExecuted);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = super.getRequestData().put("userId", userid);
        if (to != null) {
            data.put("to", to);
        }
        if (from != null) {
            data.put("from", from);
        }
        if (debug != null) {
            data.put("debug", debug);
        }
        data.put("leave", leave);
        return data;
    }

    @Override
    protected LoadController getLoadController() {
        return new ChatLoadController();
    }

    @Override
    public String getServiceName() {
        return service;
    }

    @Override
    public void exec() {
        if (userid > 0) {
            if (mRequestExecutedWeakReference != null && mRequestExecutedWeakReference.get() != null) {
                mRequestExecutedWeakReference.get().onExecuted();
            }
            super.exec();
        }
    }

    public interface IRequestExecuted {

        void onExecuted();

    }

}

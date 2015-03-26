package com.topface.topface.utils.notifications;

import com.topface.framework.JsonUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.SerializableToJson;
import com.topface.topface.utils.SerializableList;
import com.topface.topface.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class MessageStack extends SerializableList {

    public static final int EMPTY_USER_ID = -1;

    public int getRestMessages() {
        return mRestMessages;
    }

    public void setRestMessages(int restMessages) {
        mRestMessages = restMessages;
    }

    @Override
    public void addFirst(SerializableToJson object) {
        super.addFirst(object);
        if (size() > SHOWED_MESSAGES) {
            removeLast();
            if (mRestMessages > 0) {
                removeLast();
            }
            mRestMessages++;

            //Добавляем строчку "и еще %d"
            add(new Message(Utils.getQuantityString(R.plurals.general_some_more, mRestMessages, mRestMessages), Static.EMPTY, EMPTY_USER_ID));
        }
    }

    public int getAllCount() {
        return mRestMessages + size() + (mRestMessages == 0 ? 0 : -1); //отнимаем один, потому что сайз учитывает строчку "и еще %d"
    }

    public MessageStack(LinkedList<Message> list) {
        addAll(list);
    }

    public MessageStack() {
    }

    private int mRestMessages;
    /**
     * Количество показываемых сообщений в нотификации. Когда пытаемся показать больше,
     * самое старое сообщение удаляется и вместо него ставится фраза
     * осталось %d нотификаций.
     */
    private static final int SHOWED_MESSAGES = 5;

    public static class Message implements SerializableToJson {
        public String mName;
        public String mTitle;
        public int mUserId;

        public Message(String name, String title, int userId) {
            mName = name;
            mTitle = title;
            mUserId = userId;
        }

        public Message() {
        }

        @Override
        public JSONObject toJson() {
            String jsonString = JsonUtils.toJson(new Message(mName, mTitle, mUserId));
            if (jsonString != null) {
                try {
                    return new JSONObject(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        public void fromJSON(String json) {
            if (json != null) {
                Message msg = JsonUtils.fromJson(json, Message.class);
                mName = msg.mName;
                mTitle = msg.mTitle;
                mUserId = msg.mUserId;
            }
        }
    }
}

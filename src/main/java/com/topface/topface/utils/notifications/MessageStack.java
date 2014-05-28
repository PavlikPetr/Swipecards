package com.topface.topface.utils.notifications;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.SerializableToJson;
import com.topface.topface.utils.SerializableList;
import com.topface.topface.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class MessageStack extends SerializableList {

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
            add(new Message(Utils.getQuantityString(R.plurals.general_some_more, mRestMessages, mRestMessages), Static.EMPTY));
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

        public Message(String name, String title) {
            mName = name;
            mTitle = title;
        }

        public Message() {
        }

        @Override
        public JSONObject toJson() {
            JSONObject object = new JSONObject();
            try {
                object.put("name", mName);
                object.put("title", mTitle);
            } catch (JSONException e) {
                Debug.error(e);
            }
            return object;
        }

        @Override
        public void fromJSON(String json) {
            try {
                JSONObject object = new JSONObject(json);
                mName = object.optString("name");
                mTitle = object.optString("title");
            } catch (JSONException e) {
                Debug.error(e);
            }

        }
    }
}

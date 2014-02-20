package com.topface.topface.utils.notifications;

import android.text.Spannable;
import android.text.SpannableString;

import com.topface.topface.R;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class MessageStack extends LinkedList<Spannable>{

    public int getRestMessages() {
        return mRestMessages;
    }
    public void setRestMessages(int restMessages) {
        mRestMessages = restMessages;
    }

    @Override
    public void addFirst(Spannable object) {
        super.addFirst(object);
        if (size() > SHOWED_MESSAGES) {
            mRestMessages++;
            removeLast();
            if (mRestMessages > 0) {
                removeLast();
            }
            add(new SpannableString(Utils.getQuantityString(R.plurals.general_some_more, mRestMessages, mRestMessages)));
        }
    }

    public int getAllCount() {
        return mRestMessages + size();
    }

    public MessageStack(LinkedList<Spannable> list) {
        addAll(list);
    }

    public MessageStack() {}

    private int mRestMessages;
    /**
     * Количество показываемых сообщений в нотификации. Когда пытаемся показать больше,
     * самое старое сообщение удаляется и вместо него ставится фраза
     * осталось %d нотификаций.
     */
    private static final int SHOWED_MESSAGES = 5;
}

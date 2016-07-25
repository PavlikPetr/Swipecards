package com.topface.topface.utils.controllers.chatStubs;

import com.topface.topface.data.History;

import org.jetbrains.annotations.NotNull;

/**
 * Created by ppavlik on 22.07.16.
 * keep all kinds of plugs chatand start it from here too
 */

public class ChatStabsController {

    public static final int NO_BLOCK = -1;
    public static final int MUTUAL_SYMPATHY = 7;
    public static final int LOCK_CHAT = 35;
    public static final int LOCK_MESSAGE_SEND = 36;

    private int mLockType;

    public ChatStabsController() {

    }

    @SuppressWarnings("ConstantConditions")
    public int checkMessageType(@NotNull History msg) {
        mLockType = NO_BLOCK;
        if (msg != null) {
            switch (msg.type) {
                case MUTUAL_SYMPATHY:
                    mLockType = MUTUAL_SYMPATHY;
                    break;

                case LOCK_CHAT:
                    mLockType = LOCK_CHAT;
                    break;

                case LOCK_MESSAGE_SEND:
                    mLockType = LOCK_MESSAGE_SEND;
                    break;

                default:
                    break;
            }
        }
        return mLockType;
    }
}

package com.topface.topface.utils.actionbar;

import com.topface.topface.R;

import java.util.ArrayList;

import static com.topface.topface.utils.actionbar.TopMenu.TopMenuItem.ADD_TO_BLACK_LIST_ACTION;
import static com.topface.topface.utils.actionbar.TopMenu.TopMenuItem.ADD_TO_BOOKMARK_ACTION;
import static com.topface.topface.utils.actionbar.TopMenu.TopMenuItem.COMPLAIN_ACTION;
import static com.topface.topface.utils.actionbar.TopMenu.TopMenuItem.OPEN_CHAT_ACTION;
import static com.topface.topface.utils.actionbar.TopMenu.TopMenuItem.OPEN_PROFILE_FOR_EDITOR_STUB;
import static com.topface.topface.utils.actionbar.TopMenu.TopMenuItem.SEND_ADMIRATION_ACTION;
import static com.topface.topface.utils.actionbar.TopMenu.TopMenuItem.SEND_GIFT_ACTION;
import static com.topface.topface.utils.actionbar.TopMenu.TopMenuItem.SEND_SYMPATHY_ACTION;

public class TopMenu {

    public static enum TopMenuItem {
        SEND_GIFT_ACTION(1, R.string.general_gift),
        SEND_SYMPATHY_ACTION(2, R.string.general_sympathy),
        SEND_ADMIRATION_ACTION(3, R.string.general_delight),
        OPEN_CHAT_ACTION(4, R.string.user_actions_chat),
        ADD_TO_BLACK_LIST_ACTION(5, R.string.black_list_add_short, R.string.black_list_delete),
        COMPLAIN_ACTION(6, R.string.general_complain),
        ADD_TO_BOOKMARK_ACTION(7, R.string.general_bookmarks_add, R.string.general_bookmarks_delete),
        OPEN_PROFILE_FOR_EDITOR_STUB(8, R.string.editor_profile_admin);

        private int mId;
        private int mFirstResourceId;
        private int mSecondResourceId;

        TopMenuItem(int id, int firstResource) {
            this(id, firstResource, firstResource);
        }

        TopMenuItem(int id, int firstResource, int secondResource) {
            mId = id;
            mFirstResourceId = firstResource;
            mSecondResourceId = secondResource;
        }

        public int getId() {
            return mId;
        }

        public int getFirstResourceId() {
            return mFirstResourceId;
        }

        public int getSecondResourceId() {
            return mSecondResourceId;
        }
    }

    public static ArrayList<TopMenuItem> getChatTopMenu() {
        ArrayList<TopMenuItem> result = new ArrayList<>();
        result.add(ADD_TO_BLACK_LIST_ACTION);
        result.add(ADD_TO_BOOKMARK_ACTION);
        result.add(COMPLAIN_ACTION);
        return result;
    }

    public static ArrayList<TopMenuItem> getProfileTopMenu(boolean isEditor) {
        ArrayList<TopMenuItem> result = new ArrayList<>();
        result.add(SEND_SYMPATHY_ACTION);
        result.add(SEND_ADMIRATION_ACTION);
        result.add(OPEN_CHAT_ACTION);
        result.add(SEND_GIFT_ACTION);
        result.add(ADD_TO_BLACK_LIST_ACTION);
        result.add(ADD_TO_BOOKMARK_ACTION);
        result.add(COMPLAIN_ACTION);
        if (isEditor) {
            result.add(OPEN_PROFILE_FOR_EDITOR_STUB);
        }
        return result;
    }

    public static TopMenuItem findTopMenuItemById(int id) {
        for (TopMenuItem item : TopMenuItem.values()) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    public static boolean isCurrentIdTopMenuItem(int itemId) {
        TopMenuItem currentTopMenuItem = findTopMenuItemById(itemId);
        return currentTopMenuItem == null ? false : true;
    }
}

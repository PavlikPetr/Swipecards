package com.topface.topface.utils.actionbar;

import android.content.Intent;

import com.topface.topface.R;
import com.topface.topface.data.FeedUser;

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

    public static final String CURRENT_RESOURCE = "current_resource";

    public static enum TopMenuItem {
        SEND_GIFT_ACTION(1, R.string.black_list_add_short),
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
        result.add(SEND_GIFT_ACTION);
        result.add(SEND_SYMPATHY_ACTION);
        result.add(SEND_ADMIRATION_ACTION);
        result.add(OPEN_CHAT_ACTION);
        result.add(ADD_TO_BLACK_LIST_ACTION);
        result.add(COMPLAIN_ACTION);
        result.add(ADD_TO_BOOKMARK_ACTION);
        if (isEditor) {
            result.add(OPEN_PROFILE_FOR_EDITOR_STUB);
        }
        return result;
    }

    public static int[] getProfileTopMenuItems(FeedUser user, boolean isEditor) {
        return getResourcesIdByTopMenuItemArray(user, getProfileTopMenu(isEditor));
    }

    public static int[] getChatTopMenuItems(FeedUser user) {
        return getResourcesIdByTopMenuItemArray(user, getChatTopMenu());
    }

    private static int[] getResourcesIdByTopMenuItemArray(FeedUser user, ArrayList<TopMenuItem> topMenuItemArray) {
        int[] result = new int[topMenuItemArray.size()];
        int resourceId = 0;
        for (int i = 0; i < topMenuItemArray.size(); i++) {
            TopMenuItem item = topMenuItemArray.get(i);
            switch (topMenuItemArray.get(i)) {
                case ADD_TO_BLACK_LIST_ACTION:
                    if (user != null) {
                        resourceId = user.blocked ? item.getSecondResourceId() : item.getFirstResourceId();
                    }
                    break;
                case ADD_TO_BOOKMARK_ACTION:
                    if (user != null) {
                        resourceId = user.bookmarked ? item.getSecondResourceId() : item.getFirstResourceId();
                    }
                    break;
                default:
                    resourceId = item.getFirstResourceId();
                    break;
            }
            result[i] = resourceId;
        }
        return result;
    }

    public static Integer getCurrentResourceId(int itemId, Intent intent) {
        int currentResource = 0;
        TopMenuItem currentTopMenuItem = findTopMenuItemById(itemId);
        if (currentTopMenuItem != null) {
            if (intent.hasExtra(CURRENT_RESOURCE)) {
                currentResource = intent.getIntExtra(CURRENT_RESOURCE, 0);
            }
        }
        return currentResource;
    }

    public static Integer getNextResourceId(int itemId, Intent intent) {
        TopMenuItem currentTopMenuItem = findTopMenuItemById(itemId);
        if (currentTopMenuItem != null) {
            return getNextResourceId(getCurrentResourceId(itemId, intent), currentTopMenuItem);
        }
        return null;
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

    public static int getNextResourceId(int resourceId, TopMenuItem currentTopMenuItem) {
        return currentTopMenuItem.getFirstResourceId() == resourceId ?
                currentTopMenuItem.getSecondResourceId() :
                currentTopMenuItem.getFirstResourceId();
    }

    public static Integer getItemPositionInChatTopMenu(TopMenuItem topMenuItem) {
        ArrayList<TopMenuItem> topMenuItemArray = getChatTopMenu();
        for (int i = 0; i < topMenuItemArray.size(); i++) {
            if (topMenuItemArray.get(i).getId() == topMenuItem.getId()) {
                return i;
            }
        }
        return null;
    }
}

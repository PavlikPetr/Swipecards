package com.topface.topface.data.leftMenu;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by ppavlik on 06.05.16.
 * ids for all left menu fragments
 */
public class FragmentIdData {

    public static final int VIP_PROFILE = 0;
    public static final int PROFILE = 1;
    public static final int DATING = 2;
    public static final int TABBED_DIALOGS = 3;
    public static final int TABBED_VISITORS = 4;
    public static final int TABBED_LIKES = 5;
    public static final int PHOTO_BLOG = 6;
    public static final int GEO = 9;
    public static final int BONUS = 10;
    public static final int EDITOR = 1000;
    public static final int SETTINGS = 11;
    public static final int INTEGRATION_PAGE = 12;
    public static final int BALLANCE = 13;
    public static final int UNDEFINED = -1;
    public static final int BECOME_VIP = 14;
    public static final int FB_INVITE_FRIENDS = 15;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VIP_PROFILE, PROFILE, DATING, TABBED_DIALOGS, TABBED_VISITORS, TABBED_LIKES, PHOTO_BLOG,
            GEO, BONUS, EDITOR, SETTINGS, INTEGRATION_PAGE, BALLANCE, BECOME_VIP, FB_INVITE_FRIENDS, UNDEFINED})
    public @interface FragmentId {
    }

    @FragmentId
    public static int getFragmentId(int id) {
        switch (id) {
            case VIP_PROFILE:
                return VIP_PROFILE;
            case PROFILE:
                return PROFILE;
            case DATING:
                return DATING;
            case TABBED_DIALOGS:
                return TABBED_DIALOGS;
            case TABBED_VISITORS:
                return TABBED_VISITORS;
            case TABBED_LIKES:
                return TABBED_LIKES;
            case PHOTO_BLOG:
                return PHOTO_BLOG;
            case GEO:
                return GEO;
            case BONUS:
                return BONUS;
            case EDITOR:
                return EDITOR;
            case SETTINGS:
                return SETTINGS;
            case INTEGRATION_PAGE:
                return INTEGRATION_PAGE;
            case BALLANCE:
                return BALLANCE;
            case BECOME_VIP:
                return BECOME_VIP;
            case FB_INVITE_FRIENDS:
                return FB_INVITE_FRIENDS;
            default:
                return UNDEFINED;
        }
    }

    @FragmentId
    public static int getFragmentId(String field, @FragmentId int defaultValue) {
        switch (field) {
            case "VIP_PROFILE":
                return VIP_PROFILE;
            case "PROFILE":
                return PROFILE;
            case "DATING":
                return DATING;
            case "TABBED_DIALOGS":
                return TABBED_DIALOGS;
            case "TABBED_VISITORS":
                return TABBED_VISITORS;
            case "TABBED_LIKES":
                return TABBED_LIKES;
            case "PHOTO_BLOG":
                return PHOTO_BLOG;
            case "GEO":
                return GEO;
            case "BONUS":
                return BONUS;
            case "EDITOR":
                return EDITOR;
            case "SETTINGS":
                return SETTINGS;
            case "INTEGRATION_PAGE":
                return INTEGRATION_PAGE;
            case "BALLANCE":
                return BALLANCE;
            case "BECOME_VIP":
                return BECOME_VIP;
            case "FB_INVITE_FRIENDS":
                return FB_INVITE_FRIENDS;
            case "UNDEFINED":
                return UNDEFINED;
        }
        return defaultValue;
    }
}

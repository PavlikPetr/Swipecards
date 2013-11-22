package com.topface.topface.utils;

import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;

/**
 * Created by kirussell on 20.11.13.
 * static methods for managing resources
 */
public class ResourcesUtils {

    public static int getFragmentNameResId(BaseFragment.FragmentId id) {
        switch (id) {
            case F_VIP_PROFILE:
            case F_PROFILE:
                return R.string.general_profile;
            case F_DATING:
                return R.string.general_dating;
            case F_LIKES:
            case F_LIKES_CLOSINGS:
                return R.string.general_likes;
            case F_ADMIRATIONS:
                return R.string.general_admirations;
            case F_MUTUAL:
            case F_MUTUAL_CLOSINGS:
                return R.string.general_mutual;
            case F_DIALOGS:
                return R.string.general_dialogs;
            case F_VISITORS:
                return R.string.general_visitors;
            case F_BOOKMARKS:
                return R.string.general_bookmarks;
            case F_FANS:
                return R.string.general_fans;
            case F_EDITOR:
                return R.string.editor_menu_admin;
            case F_SETTINGS:
                return R.string.general_settings;
            case F_UNDEFINED:
            default:
                throw new IllegalArgumentException("Illegal fragmentId: do not have resources for this fragment id");
        }
    }
}

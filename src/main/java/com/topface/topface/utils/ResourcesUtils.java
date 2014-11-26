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
            case VIP_PROFILE:
            case PROFILE:
                return R.string.general_profile;
            case DATING:
                return R.string.general_dating;
            case LIKES:
            case LIKES_CLOSINGS:
                return R.string.general_likes;
            case F_TABBED_LIKES:
                return R.string.general_sympathies;
            case ADMIRATIONS:
                return R.string.general_admirations;
            case MUTUAL:
            case MUTUAL_CLOSINGS:
                return R.string.general_mutual;
            case DIALOGS:
                return R.string.settings_messages;
            case VISITORS:
                return R.string.general_visitors;
            case BOOKMARKS:
                return R.string.general_bookmarks;
            case BONUS:
                return R.string.general_bonus;
            case FANS:
                return R.string.general_fans;
            case GEO:
                return R.string.people_nearby;
            case EDITOR:
                return R.string.editor_menu_admin;
            case SETTINGS:
                return R.string.general_settings;
            case UNDEFINED:
            default:
                throw new IllegalArgumentException("Illegal fragmentId: do not have resources for this fragment id");
        }
    }
}

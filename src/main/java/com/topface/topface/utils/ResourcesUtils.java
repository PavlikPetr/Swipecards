package com.topface.topface.utils;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;

/**
 * Created by kirussell on 20.11.13.
 * static methods for managing resources
 */
public class ResourcesUtils {

    public static String getFragmentNameResId(BaseFragment.FragmentId id) {
        int titleId;
        switch (id) {
            case VIP_PROFILE:
            case PROFILE:
                titleId = R.string.general_profile;
                break;
            case DATING:
                titleId = R.string.general_dating;
                break;
            case LIKES_CLOSINGS:
                titleId = R.string.general_likes;
                break;
            case TABBED_LIKES:
                titleId = R.string.general_sympathies;
                break;
            case MUTUAL_CLOSINGS:
                titleId = R.string.general_mutual;
                break;
            case TABBED_DIALOGS:
                titleId = R.string.settings_messages;
                break;
            case TABBED_VISITORS:
                titleId = R.string.general_visitors;
                break;
            case BONUS:
                return CacheProfile.getOptions().bonus.buttonText;
            case GEO:
                titleId = R.string.people_nearby;
                break;
            case EDITOR:
                titleId = R.string.editor_menu_admin;
                break;
            case SETTINGS:
                titleId = R.string.general_settings;
                break;
            case UNDEFINED:
            default:
                throw new IllegalArgumentException("Illegal fragmentId: do not have resources for this fragment id");
        }
        return App.getContext().getString(titleId);
    }
}

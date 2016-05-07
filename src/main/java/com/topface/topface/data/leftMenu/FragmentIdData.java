package com.topface.topface.data.leftMenu;

import android.support.annotation.IntDef;

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

    @IntDef({VIP_PROFILE, PROFILE, DATING, TABBED_DIALOGS, TABBED_VISITORS, TABBED_LIKES, PHOTO_BLOG,
            GEO, BONUS, EDITOR, SETTINGS, INTEGRATION_PAGE, BALLANCE, UNDEFINED})
    public @interface FragmentId {
    }
}

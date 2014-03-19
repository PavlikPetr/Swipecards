package com.topface.topface.robotiumTests;

import android.annotation.TargetApi;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;

import com.jayway.android.robotium.solo.Solo;
import com.topface.topface.R;
import com.topface.topface.ui.NavigationActivity;

@TargetApi(Build.VERSION_CODES.FROYO)
public class MenuItemsOpensTest extends ActivityInstrumentationTestCase2<NavigationActivity> {

    private Solo solo;

    @TargetApi(Build.VERSION_CODES.FROYO)
    public MenuItemsOpensTest() {
        super(NavigationActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testItems() throws Exception {
        try {
            solo.clickOnButton(getActivity().getString(R.string.general_profile));
            solo.clickOnButton(getActivity().getString(R.string.general_dating));
            solo.clickOnButton(getActivity().getString(R.string.general_likes));
            solo.clickOnButton(getActivity().getString(R.string.general_dialogs));
            solo.clickOnButton(getActivity().getString(R.string.general_bookmarks));
            solo.clickOnButton(getActivity().getString(R.string.general_fans));
            solo.clickOnButton(getActivity().getString(R.string.general_visitors));
        } catch (Exception ex) {
            assertFalse(ex.toString(), true);
        }

        assertTrue(true);
    }

    @Override
    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }
}
